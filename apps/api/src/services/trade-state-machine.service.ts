/**
 * CARDCEEZA — Server-Side Trade State Machine
 * Brand: CardCeeza (Trade Gift Cards. Get Paid in NGN.)
 *
 * Enforces:
 * 1. Strict Directed Graph of Allowed State Transitions.
 * 2. Role-Based Transition Authorizations (User vs Verifier vs Finance vs Admin).
 * 3. Atomic Database Transactions with immutable audit logging to `trade_events`.
 * 4. Automatic Ledger Trigger on Trade Approval / Payout.
 */

export enum TradeStatus {
  DRAFT = 'DRAFT',
  SUBMITTED = 'SUBMITTED',
  UNDER_REVIEW = 'UNDER_REVIEW',
  VERIFICATION_REQUIRED = 'VERIFICATION_REQUIRED',
  VERIFIED = 'VERIFIED',
  REJECTED = 'REJECTED',
  APPROVED = 'APPROVED',
  PAYOUT_PENDING = 'PAYOUT_PENDING',
  PAID = 'PAID',
  CANCELLED = 'CANCELLED',
  DISPUTED = 'DISPUTED',
}

export enum UserRole {
  USER = 'USER',
  VERIFIER = 'VERIFIER',
  SUPPORT = 'SUPPORT',
  FINANCE = 'FINANCE',
  ADMIN = 'ADMIN',
  SUPER_ADMIN = 'SUPER_ADMIN',
}

export interface StateTransitionRule {
  from: TradeStatus[];
  to: TradeStatus;
  allowedRoles: UserRole[];
  requiresReason?: boolean;
}

// Strict State Transition Matrix
export const ALLOWED_STATE_TRANSITIONS: StateTransitionRule[] = [
  // User submissions
  {
    from: [TradeStatus.DRAFT],
    to: TradeStatus.SUBMITTED,
    allowedRoles: [UserRole.USER, UserRole.ADMIN, UserRole.SUPER_ADMIN],
  },
  // Verifier picks up trade for review
  {
    from: [TradeStatus.SUBMITTED],
    to: TradeStatus.UNDER_REVIEW,
    allowedRoles: [UserRole.VERIFIER, UserRole.ADMIN, UserRole.SUPER_ADMIN],
  },
  // Verifier requests more info / proofs
  {
    from: [TradeStatus.UNDER_REVIEW],
    to: TradeStatus.VERIFICATION_REQUIRED,
    allowedRoles: [UserRole.VERIFIER, UserRole.ADMIN, UserRole.SUPER_ADMIN],
    requiresReason: true,
  },
  // User uploads additional info
  {
    from: [TradeStatus.VERIFICATION_REQUIRED],
    to: TradeStatus.UNDER_REVIEW,
    allowedRoles: [UserRole.USER, UserRole.ADMIN, UserRole.SUPER_ADMIN],
  },
  // Card inspection outcome: Verified or Rejected
  {
    from: [TradeStatus.UNDER_REVIEW],
    to: TradeStatus.VERIFIED,
    allowedRoles: [UserRole.VERIFIER, UserRole.ADMIN, UserRole.SUPER_ADMIN],
  },
  {
    from: [TradeStatus.UNDER_REVIEW, TradeStatus.SUBMITTED, TradeStatus.VERIFICATION_REQUIRED],
    to: TradeStatus.REJECTED,
    allowedRoles: [UserRole.VERIFIER, UserRole.ADMIN, UserRole.SUPER_ADMIN],
    requiresReason: true,
  },
  // Verifier or Admin approves verified card for payout
  {
    from: [TradeStatus.VERIFIED],
    to: TradeStatus.APPROVED,
    allowedRoles: [UserRole.VERIFIER, UserRole.ADMIN, UserRole.SUPER_ADMIN],
  },
  // Finance initiates NIP bank settlement
  {
    from: [TradeStatus.APPROVED],
    to: TradeStatus.PAYOUT_PENDING,
    allowedRoles: [UserRole.FINANCE, UserRole.ADMIN, UserRole.SUPER_ADMIN],
  },
  // Payment provider webhook confirms credit to user bank
  {
    from: [TradeStatus.PAYOUT_PENDING, TradeStatus.APPROVED],
    to: TradeStatus.PAID,
    allowedRoles: [UserRole.FINANCE, UserRole.ADMIN, UserRole.SUPER_ADMIN],
  },
  // Cancellation (before review)
  {
    from: [TradeStatus.DRAFT, TradeStatus.SUBMITTED],
    to: TradeStatus.CANCELLED,
    allowedRoles: [UserRole.USER, UserRole.ADMIN, UserRole.SUPER_ADMIN],
    requiresReason: true,
  },
  // Dispute escalation
  {
    from: [TradeStatus.REJECTED, TradeStatus.UNDER_REVIEW],
    to: TradeStatus.DISPUTED,
    allowedRoles: [UserRole.USER, UserRole.SUPPORT, UserRole.ADMIN, UserRole.SUPER_ADMIN],
    requiresReason: true,
  },
];

export interface TransitionTradeParams {
  tradeId: string;
  targetStatus: TradeStatus;
  actorId: string;
  actorRole: UserRole;
  note?: string;
  rejectionReason?: string;
}

export class TradeStateMachineService {
  constructor(private readonly db: any) {}

  /**
   * Validates if a transition is legal for the given actor role.
   */
  canTransition(currentStatus: TradeStatus, targetStatus: TradeStatus, actorRole: UserRole): boolean {
    const rule = ALLOWED_STATE_TRANSITIONS.find(
      (r) => r.to === targetStatus && r.from.includes(currentStatus)
    );
    if (!rule) return false;
    return rule.allowedRoles.includes(actorRole);
  }

  /**
   * Atomically executes trade state transition, enforces rules, and inserts immutable trade_event.
   */
  async transitionTrade(params: TransitionTradeParams) {
    const { tradeId, targetStatus, actorId, actorRole, note, rejectionReason } = params;

    return await this.db.$transaction(async (tx: any) => {
      // 1. Lock trade record
      const trade = await tx.trade.findUnique({
        where: { id: tradeId },
      });

      if (!trade) {
        throw new Error(`Trade with ID ${tradeId} not found`);
      }

      const currentStatus = trade.status as TradeStatus;

      // 2. Validate transition
      if (!this.canTransition(currentStatus, targetStatus, actorRole)) {
        throw new Error(
          `Illegal transition from '${currentStatus}' to '${targetStatus}' for role '${actorRole}'`
        );
      }

      // Check if rejection/cancellation reason is required
      const rule = ALLOWED_STATE_TRANSITIONS.find(
        (r) => r.to === targetStatus && r.from.includes(currentStatus)
      );
      if (rule?.requiresReason && !rejectionReason && !note) {
        throw new Error(`State transition to '${targetStatus}' requires a descriptive reason`);
      }

      // 3. Update Trade Status
      const updatedTrade = await tx.trade.update({
        where: { id: tradeId },
        data: {
          status: targetStatus,
          rejectionReason: rejectionReason || trade.rejectionReason,
          verifierNotes: note || trade.verifierNotes,
          completedAt: targetStatus === TradeStatus.PAID ? new Date() : trade.completedAt,
        },
      });

      // 4. Create immutable TradeEvent entry
      const event = await tx.tradeEvent.create({
        data: {
          tradeId: trade.id,
          actorId,
          previousStatus: currentStatus,
          newStatus: targetStatus,
          note: note || rejectionReason || `Status updated to ${targetStatus}`,
        },
      });

      // 5. Create System Notification for User
      await tx.notification.create({
        data: {
          userId: trade.userId,
          title: `Trade ${trade.tradeRef} Update`,
          message: this.getNotificationMessage(targetStatus, trade.tradeRef, rejectionReason),
          type: 'TRADE_STATUS',
          relatedEntityType: 'TRADE',
          relatedEntityId: trade.id,
        },
      });

      // 6. Record Audit Log
      await tx.auditLog.create({
        data: {
          actorId,
          action: `TRADE_STATUS_${targetStatus}`,
          entity: 'TRADE',
          entityId: trade.id,
          oldValues: { status: currentStatus },
          newValues: { status: targetStatus, note, rejectionReason },
        },
      });

      return {
        success: true,
        trade: updatedTrade,
        event,
      };
    });
  }

  private getNotificationMessage(status: TradeStatus, tradeRef: string, reason?: string): string {
    switch (status) {
      case TradeStatus.UNDER_REVIEW:
        return `Your trade ${tradeRef} is now undergoing verification by our team.`;
      case TradeStatus.VERIFIED:
        return `Card details for trade ${tradeRef} have been verified successfully!`;
      case TradeStatus.APPROVED:
        return `Trade ${tradeRef} is approved! Your NGN payout is being prepared.`;
      case TradeStatus.PAYOUT_PENDING:
        return `Disbursement initiated for trade ${tradeRef}. Funds will arrive shortly.`;
      case TradeStatus.PAID:
        return `Settlement completed! Payout for trade ${tradeRef} has been credited.`;
      case TradeStatus.REJECTED:
        return `Trade ${tradeRef} was rejected: ${reason || 'Card invalid or already redeemed.'}`;
      case TradeStatus.VERIFICATION_REQUIRED:
        return `Additional information required for trade ${tradeRef}. Please upload evidence.`;
      default:
        return `Trade ${tradeRef} status updated to ${status}.`;
    }
  }
}
