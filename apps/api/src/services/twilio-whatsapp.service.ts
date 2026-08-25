/**
 * CARDCEEZA — Twilio WhatsApp Messaging Service
 * Brand: CardCeeza (Trade Gift Cards. Get Paid in NGN.)
 * 
 * Supports:
 * 1. Dispatching direct WhatsApp replies from Admin/Support dashboard via Twilio Messaging API.
 * 2. Receiving inbound customer WhatsApp messages via webhook.
 * 3. Appending outbound & inbound messages to `support_messages` table for unified ticket audit threads.
 */

export interface SendWhatsAppMessageParams {
  toPhoneNumber: string;
  ticketId?: string;
  ticketRef?: string;
  tradeRef?: string;
  senderId: string;
  senderName: string;
  messageBody: string;
}

export interface TwilioWhatsAppResult {
  success: boolean;
  messageSid?: string;
  status: 'SENT' | 'QUEUED' | 'FAILED' | 'MOCK_DELIVERED';
  recipient: string;
  error?: string;
}

export class TwilioWhatsAppService {
  private readonly accountSid: string;
  private readonly authToken: string;
  private readonly fromWhatsAppNumber: string;
  private readonly isConfigured: boolean;

  constructor(private readonly db: any) {
    this.accountSid = process.env.TWILIO_ACCOUNT_SID || '';
    this.authToken = process.env.TWILIO_AUTH_TOKEN || '';
    // Format: "whatsapp:+14155238886" or your verified Twilio WhatsApp Business number
    this.fromWhatsAppNumber = process.env.TWILIO_WHATSAPP_FROM || 'whatsapp:+14155238886';
    this.isConfigured = Boolean(this.accountSid && this.authToken && !this.accountSid.includes('placeholder'));
  }

  /**
   * Formats raw phone number to E.164 Twilio WhatsApp format (defaulting to Nigeria +234).
   */
  static formatToWhatsAppE164(phone: string): string {
    const digitsOnly = phone.replace(/[^0-9]/g, '');
    let normalized = digitsOnly;
    if (digitsOnly.startsWith('234')) {
      normalized = digitsOnly;
    } else if (digitsOnly.startsWith('0')) {
      normalized = `234${digitsOnly.substring(1)}`;
    } else if (digitsOnly.length === 10) {
      normalized = `234${digitsOnly}`;
    }
    return `whatsapp:+${normalized}`;
  }

  /**
   * Sends a WhatsApp message using Twilio REST API and records it in support_messages.
   */
  async sendMessage(params: SendWhatsAppMessageParams): Promise<TwilioWhatsAppResult> {
    const { toPhoneNumber, ticketId, ticketRef, tradeRef, senderId, senderName, messageBody } = params;
    const formattedTo = TwilioWhatsAppService.formatToWhatsAppE164(toPhoneNumber);

    let prefix = `*CardCeeza Support* 🛡️\n`;
    if (ticketRef) prefix += `Ticket: #${ticketRef}\n`;
    if (tradeRef) prefix += `Trade: #${tradeRef}\n`;
    const fullBody = `${prefix}\n${messageBody}\n\n_Trade Gift Cards. Get Paid in NGN._`;

    try {
      let messageSid = `mock_wa_${Date.now()}_${Math.random().toString(36).substring(7)}`;
      let status: 'SENT' | 'QUEUED' | 'MOCK_DELIVERED' = 'MOCK_DELIVERED';

      if (this.isConfigured) {
        const endpoint = `https://api.twilio.com/2010-04-01/Accounts/${this.accountSid}/Messages.json`;
        const authHeader = Buffer.from(`${this.accountSid}:${this.authToken}`).toString('base64');

        const formData = new URLSearchParams();
        formData.append('From', this.fromWhatsAppNumber);
        formData.append('To', formattedTo);
        formData.append('Body', fullBody);

        const response = await fetch(endpoint, {
          method: 'POST',
          headers: {
            Authorization: `Basic ${authHeader}`,
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          body: formData.toString(),
        });

        const data: any = await response.json();
        if (!response.ok) {
          throw new Error(data.message || `Twilio error code ${data.code}`);
        }
        messageSid = data.sid;
        status = 'SENT';
      }

      // Record in database support thread if ticketId is provided
      if (ticketId && this.db) {
        await this.db.supportMessage.create({
          data: {
            ticketId,
            senderId,
            isStaffReply: true,
            message: `[WhatsApp Sent to ${formattedTo}]\n${messageBody}`,
            attachmentUri: messageSid,
          },
        });

        // Update ticket status to IN_PROGRESS or WAITING_FOR_USER
        await this.db.supportTicket.update({
          where: { id: ticketId },
          data: {
            status: 'WAITING_FOR_USER',
            updatedAt: new Date(),
          },
        });
      }

      return {
        success: true,
        messageSid,
        status,
        recipient: formattedTo,
      };
    } catch (error: any) {
      return {
        success: false,
        status: 'FAILED',
        recipient: formattedTo,
        error: error.message || 'Failed to dispatch Twilio WhatsApp message',
      };
    }
  }

  /**
   * Handles incoming Twilio WhatsApp Webhook for bidirectional thread sync.
   */
  async handleInboundWebhook(payload: any): Promise<{ received: boolean; ticketId?: string }> {
    const fromWhatsApp = payload.From; // e.g. "whatsapp:+2348012345678"
    const body = payload.Body || '';
    const cleanPhone = fromWhatsApp?.replace('whatsapp:', '').replace('+', '') || '';

    if (!cleanPhone || !this.db) {
      return { received: true };
    }

    // Find user by phone number
    const user = await this.db.user.findFirst({
      where: {
        OR: [
          { phoneNumber: { contains: cleanPhone.substring(cleanPhone.length - 10) } },
          { phoneNumber: cleanPhone },
        ],
      },
    });

    if (user) {
      // Find the most recent open or active ticket
      const activeTicket = await this.db.supportTicket.findFirst({
        where: {
          userId: user.id,
          status: { in: ['OPEN', 'IN_PROGRESS', 'WAITING_FOR_USER'] },
        },
        orderBy: { updatedAt: 'desc' },
      });

      if (activeTicket) {
        await this.db.supportMessage.create({
          data: {
            ticketId: activeTicket.id,
            senderId: user.id,
            isStaffReply: false,
            message: `[WhatsApp Inbound]\n${body}`,
          },
        });

        await this.db.supportTicket.update({
          where: { id: activeTicket.id },
          data: {
            status: 'IN_PROGRESS',
            updatedAt: new Date(),
          },
        });

        return { received: true, ticketId: activeTicket.id };
      }
    }

    return { received: true };
  }
}
