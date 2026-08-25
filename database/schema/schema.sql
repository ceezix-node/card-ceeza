-- ==============================================================================
-- CARDCEEZA — COMPLETE PRODUCTION POSTGRESQL SCHEMA DEFINITION
-- Brand: CardCeeza (Trade Gift Cards. Get Paid in NGN.)
-- ==============================================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ==============================================================================
-- 1. ENUMS
-- ==============================================================================

CREATE TYPE user_role_enum AS ENUM (
    'USER',
    'VERIFIER',
    'SUPPORT',
    'FINANCE',
    'ADMIN',
    'SUPER_ADMIN'
);

CREATE TYPE kyc_status_enum AS ENUM (
    'KYC_NOT_STARTED',
    'KYC_PENDING',
    'KYC_VERIFIED',
    'KYC_REJECTED'
);

CREATE TYPE trade_status_enum AS ENUM (
    'DRAFT',
    'SUBMITTED',
    'UNDER_REVIEW',
    'VERIFICATION_REQUIRED',
    'VERIFIED',
    'REJECTED',
    'APPROVED',
    'PAYOUT_PENDING',
    'PAID',
    'CANCELLED',
    'DISPUTED'
);

CREATE TYPE risk_level_enum AS ENUM (
    'LOW',
    'MEDIUM',
    'HIGH',
    'CRITICAL'
);

CREATE TYPE ledger_entry_type_enum AS ENUM (
    'TRADE_CREDIT',
    'WITHDRAWAL_PAYOUT',
    'FEE_DEBIT',
    'REVERSAL_CREDIT',
    'REVERSAL_DEBIT',
    'ADJUSTMENT'
);

CREATE TYPE payout_status_enum AS ENUM (
    'INITIATED',
    'PROCESSING',
    'SUCCESS',
    'FAILED',
    'REVERSED'
);

CREATE TYPE ticket_status_enum AS ENUM (
    'OPEN',
    'IN_PROGRESS',
    'WAITING_FOR_USER',
    'RESOLVED',
    'CLOSED'
);

-- ==============================================================================
-- 2. USERS & PROFILES
-- ==============================================================================

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(32) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role user_role_enum NOT NULL DEFAULT 'USER',
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    two_factor_secret VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_suspended BOOLEAN NOT NULL DEFAULT FALSE,
    suspension_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    avatar_url TEXT,
    date_of_birth DATE,
    bvn_encrypted TEXT,
    nin_encrypted TEXT,
    kyc_status kyc_status_enum NOT NULL DEFAULT 'KYC_NOT_STARTED',
    kyc_submitted_at TIMESTAMPTZ,
    kyc_verified_at TIMESTAMPTZ,
    kyc_rejection_reason TEXT,
    address_line TEXT,
    state VARCHAR(64),
    country VARCHAR(64) DEFAULT 'Nigeria',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    ip_address VARCHAR(45),
    user_agent TEXT,
    device_name VARCHAR(128),
    expires_at TIMESTAMPTZ NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- 3. BANK ACCOUNTS
-- ==============================================================================

CREATE TABLE bank_accounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    bank_name VARCHAR(128) NOT NULL,
    bank_code VARCHAR(16) NOT NULL,
    account_number_encrypted TEXT NOT NULL,
    account_number_last4 VARCHAR(4) NOT NULL,
    account_name VARCHAR(255) NOT NULL,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    recipient_code VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- 4. GIFT CARD CATALOG & RATES
-- ==============================================================================

CREATE TABLE gift_card_types (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(128) NOT NULL,
    brand VARCHAR(128) NOT NULL,
    slug VARCHAR(128) NOT NULL UNIQUE,
    category VARCHAR(64) NOT NULL,
    icon_name VARCHAR(64) NOT NULL,
    min_denomination NUMERIC(12, 2) NOT NULL DEFAULT 10.00,
    max_denomination NUMERIC(12, 2) NOT NULL DEFAULT 2000.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    verification_guide TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE gift_card_rates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    gift_card_type_id UUID NOT NULL REFERENCES gift_card_types(id) ON DELETE RESTRICT,
    region VARCHAR(32) NOT NULL,      -- 'US', 'UK', 'CA', 'EU', 'AU', 'GLOBAL'
    currency VARCHAR(8) NOT NULL,     -- 'USD', 'GBP', 'CAD', 'EUR', 'AUD'
    rate_per_unit NUMERIC(12, 2) NOT NULL, -- e.g. 1450.00 NGN per 1 USD
    platform_fee_ngn NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    min_value NUMERIC(12, 2) NOT NULL DEFAULT 10.00,
    max_value NUMERIC(12, 2) NOT NULL DEFAULT 2000.00,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    effective_until TIMESTAMPTZ,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- 5. TRADES & WORKFLOW
-- ==============================================================================

CREATE TABLE trades (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trade_ref VARCHAR(64) NOT NULL UNIQUE, -- e.g. 'CCZ-2026-000104'
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    gift_card_type_id UUID NOT NULL REFERENCES gift_card_types(id) ON DELETE RESTRICT,
    rate_id UUID REFERENCES gift_card_rates(id) ON DELETE RESTRICT,
    card_name VARCHAR(128) NOT NULL,
    region VARCHAR(32) NOT NULL,
    currency VARCHAR(8) NOT NULL,
    card_value NUMERIC(12, 2) NOT NULL,
    rate_applied NUMERIC(12, 2) NOT NULL,
    gross_ngn NUMERIC(14, 2) NOT NULL,
    fee_ngn NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    net_payout_ngn NUMERIC(14, 2) NOT NULL,
    status trade_status_enum NOT NULL DEFAULT 'SUBMITTED',
    ecode_encrypted TEXT,
    ecode_last4 VARCHAR(8),
    has_card_image BOOLEAN NOT NULL DEFAULT FALSE,
    has_receipt_image BOOLEAN NOT NULL DEFAULT FALSE,
    evidence_image_uri TEXT,
    receipt_image_uri TEXT,
    risk_score INTEGER NOT NULL DEFAULT 0,
    risk_level risk_level_enum NOT NULL DEFAULT 'LOW',
    risk_flags TEXT[],
    assigned_verifier_id UUID REFERENCES users(id),
    rejection_reason TEXT,
    verifier_notes TEXT,
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMPTZ,
    approved_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE trade_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trade_id UUID NOT NULL REFERENCES trades(id) ON DELETE CASCADE,
    actor_id UUID REFERENCES users(id),
    previous_status trade_status_enum,
    new_status trade_status_enum NOT NULL,
    note TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE trade_evidence (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trade_id UUID NOT NULL REFERENCES trades(id) ON DELETE CASCADE,
    file_type VARCHAR(32) NOT NULL, -- 'FRONT_IMAGE', 'BACK_IMAGE', 'PURCHASE_RECEIPT'
    storage_path TEXT NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    mime_type VARCHAR(64) NOT NULL,
    is_malware_scanned BOOLEAN NOT NULL DEFAULT TRUE,
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- 6. WALLET, LEDGER & IDEMPOTENCY
-- ==============================================================================

CREATE TABLE wallets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE RESTRICT,
    cached_balance NUMERIC(14, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(8) NOT NULL DEFAULT 'NGN',
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    lock_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ledger_accounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(32) NOT NULL UNIQUE, -- e.g. '1001_USER_WALLET', '2001_PLATFORM_PAYABLES', '4001_FEE_REVENUE'
    name VARCHAR(128) NOT NULL,
    account_type VARCHAR(32) NOT NULL, -- 'ASSET', 'LIABILITY', 'EQUITY', 'REVENUE', 'EXPENSE'
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    wallet_id UUID NOT NULL REFERENCES wallets(id) ON DELETE RESTRICT,
    trade_id UUID REFERENCES trades(id) ON DELETE SET NULL,
    type ledger_entry_type_enum NOT NULL,
    amount NUMERIC(14, 2) NOT NULL, -- Positive for credit, negative for debit
    balance_after NUMERIC(14, 2) NOT NULL,
    reference_id VARCHAR(128) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE idempotency_keys (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    key VARCHAR(128) NOT NULL UNIQUE,
    action VARCHAR(64) NOT NULL,
    entity_id VARCHAR(128) NOT NULL,
    user_id UUID REFERENCES users(id),
    response_payload JSONB,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- 7. PAYOUTS
-- ==============================================================================

CREATE TABLE payouts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    payout_ref VARCHAR(64) NOT NULL UNIQUE, -- e.g. 'PAY-2026-000412'
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    bank_account_id UUID NOT NULL REFERENCES bank_accounts(id) ON DELETE RESTRICT,
    trade_id UUID REFERENCES trades(id) ON DELETE SET NULL,
    amount_ngn NUMERIC(14, 2) NOT NULL,
    fee_ngn NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    status payout_status_enum NOT NULL DEFAULT 'INITIATED',
    provider VARCHAR(64) NOT NULL DEFAULT 'NIGERIAN_NIP_GATEWAY',
    provider_reference VARCHAR(128),
    failure_reason TEXT,
    idempotency_key VARCHAR(128) NOT NULL UNIQUE,
    initiated_by UUID REFERENCES users(id),
    processed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- 8. AUDIT LOGS, NOTIFICATIONS & SUPPORT
-- ==============================================================================

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    actor_id UUID REFERENCES users(id),
    actor_email VARCHAR(255) NOT NULL,
    actor_role VARCHAR(64) NOT NULL,
    action VARCHAR(128) NOT NULL,
    entity VARCHAR(64) NOT NULL,
    entity_id VARCHAR(128) NOT NULL,
    old_values JSONB,
    new_values JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(64) NOT NULL, -- 'TRADE_STATUS', 'PAYOUT_SUCCESS', 'SECURITY_ALERT'
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    related_entity_type VARCHAR(64),
    related_entity_id VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE support_tickets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticket_ref VARCHAR(64) NOT NULL UNIQUE, -- e.g. 'TCK-2026-00088'
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    trade_id UUID REFERENCES trades(id) ON DELETE SET NULL,
    subject VARCHAR(255) NOT NULL,
    category VARCHAR(64) NOT NULL, -- 'TRADE_ISSUE', 'PAYOUT_ISSUE', 'ACCOUNT_ISSUE'
    priority VARCHAR(32) NOT NULL DEFAULT 'MEDIUM',
    status ticket_status_enum NOT NULL DEFAULT 'OPEN',
    assigned_to UUID REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE support_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ticket_id UUID NOT NULL REFERENCES support_tickets(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES users(id),
    is_staff_reply BOOLEAN NOT NULL DEFAULT FALSE,
    message TEXT NOT NULL,
    attachment_uri TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- 9. PERFORMANCE INDEXES
-- ==============================================================================

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_trades_user_id ON trades(user_id);
CREATE INDEX idx_trades_status ON trades(status);
CREATE INDEX idx_trades_trade_ref ON trades(trade_ref);
CREATE INDEX idx_trade_events_trade_id ON trade_events(trade_id);
CREATE INDEX idx_ledger_entries_user_id ON ledger_entries(user_id);
CREATE INDEX idx_ledger_entries_idempotency ON ledger_entries(idempotency_key);
CREATE INDEX idx_payouts_user_id ON payouts(user_id);
CREATE INDEX idx_payouts_status ON payouts(status);
CREATE INDEX idx_audit_logs_actor ON audit_logs(actor_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity, entity_id);
CREATE INDEX idx_notifications_user_unread ON notifications(user_id, is_read);
CREATE INDEX idx_rates_active ON gift_card_rates(gift_card_type_id, region, is_active);
