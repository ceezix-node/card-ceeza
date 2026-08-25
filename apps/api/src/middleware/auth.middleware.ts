/**
 * CARDCEEZA — Authentication & Role-Based Access Control (RBAC) Middleware
 * Brand: CardCeeza (Trade Gift Cards. Get Paid in NGN.)
 */

import { Request, Response, NextFunction } from 'express';
import * as jwt from 'jsonwebtoken';

export enum UserRole {
  USER = 'USER',
  VERIFIER = 'VERIFIER',
  SUPPORT = 'SUPPORT',
  FINANCE = 'FINANCE',
  ADMIN = 'ADMIN',
  SUPER_ADMIN = 'SUPER_ADMIN',
}

export interface AuthenticatedUser {
  id: string;
  email: string;
  role: UserRole;
  isVerified: boolean;
  kycStatus: string;
  sessionId: string;
}

declare global {
  namespace Express {
    interface Request {
      user?: AuthenticatedUser;
    }
  }
}

const JWT_SECRET = process.env.JWT_SECRET || 'cardceeza-super-secret-jwt-key-2026-ngn';

/**
 * Authentication Middleware: Validates Bearer JWT or HTTP-only auth cookie.
 */
export const authenticate = async (req: Request, res: Response, next: NextFunction) => {
  try {
    let token: string | undefined;

    // 1. Check Authorization header
    const authHeader = req.headers.authorization;
    if (authHeader && authHeader.startsWith('Bearer ')) {
      token = authHeader.split(' ')[1];
    } else if (req.cookies && req.cookies.cardceeza_token) {
      // 2. Check HTTP-only cookie
      token = req.cookies.cardceeza_token;
    }

    if (!token) {
      return res.status(401).json({
        success: false,
        error: {
          code: 'UNAUTHORIZED',
          message: 'Access token required. Please sign in to CardCeeza.',
        },
      });
    }

    // Verify JWT
    const decoded = jwt.verify(token, JWT_SECRET) as any;

    if (!decoded || !decoded.id || !decoded.role) {
      return res.status(401).json({
        success: false,
        error: {
          code: 'INVALID_TOKEN',
          message: 'Malformed session token. Please log in again.',
        },
      });
    }

    // Attach user payload to request
    req.user = {
      id: decoded.id,
      email: decoded.email,
      role: decoded.role as UserRole,
      isVerified: Boolean(decoded.isVerified),
      kycStatus: decoded.kycStatus || 'KYC_NOT_STARTED',
      sessionId: decoded.sessionId || 'session_default',
    };

    next();
  } catch (error: any) {
    if (error.name === 'TokenExpiredError') {
      return res.status(401).json({
        success: false,
        error: {
          code: 'TOKEN_EXPIRED',
          message: 'Session expired. Please sign in again.',
        },
      });
    }
    return res.status(401).json({
      success: false,
      error: {
        code: 'AUTHENTICATION_FAILED',
        message: 'Invalid or forged authentication token.',
      },
    });
  }
};

/**
 * Role-Based Access Control (RBAC) Middleware:
 * Enforces route-level role permissions (e.g., ADMIN, VERIFIER, FINANCE).
 */
export const requireRoles = (...allowedRoles: UserRole[]) => {
  return (req: Request, res: Response, next: NextFunction) => {
    if (!req.user) {
      return res.status(401).json({
        success: false,
        error: {
          code: 'UNAUTHORIZED',
          message: 'Authentication is required to access this resource.',
        },
      });
    }

    const userRole = req.user.role;

    // SUPER_ADMIN has global clearance
    if (userRole === UserRole.SUPER_ADMIN) {
      return next();
    }

    if (!allowedRoles.includes(userRole)) {
      return res.status(403).json({
        success: false,
        error: {
          code: 'FORBIDDEN_INSUFFICIENT_PERMISSIONS',
          message: `Your role (${userRole}) is not authorized to perform this action. Required: [${allowedRoles.join(', ')}]`,
        },
      });
    }

    next();
  };
};

/**
 * Ensures user is accessing their own resource or is an administrator.
 */
export const requireSelfOrAdmin = (userIdParamName: string = 'userId') => {
  return (req: Request, res: Response, next: NextFunction) => {
    if (!req.user) {
      return res.status(401).json({
        success: false,
        error: { code: 'UNAUTHORIZED', message: 'Authentication required' },
      });
    }

    const targetUserId = req.params[userIdParamName] || req.body[userIdParamName];
    const isOwner = req.user.id === targetUserId;
    const isAdmin = [UserRole.ADMIN, UserRole.SUPER_ADMIN].includes(req.user.role);

    if (!isOwner && !isAdmin) {
      return res.status(403).json({
        success: false,
        error: {
          code: 'FORBIDDEN_RESOURCE_ACCESS',
          message: 'You can only view or modify your own account resources.',
        },
      });
    }

    next();
  };
};
