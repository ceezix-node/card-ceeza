-- ============================================================================
-- CARDCEEZA — Production Database Seeds
-- Brand: CardCeeza (Trade Gift Cards. Get Paid in NGN.)
-- ============================================================================

-- 1. Insert Initial System Roles
INSERT INTO roles (name, description, permissions) VALUES
('SUPER_ADMIN', 'Full unrestricted platform access', '["*"]'::jsonb),
('ADMIN', 'Platform administration, user management, and rate configuration', '["users.read","users.manage","rates.manage","trades.read","trades.manage","audit.read"]'::jsonb),
('VERIFIER', 'Trade verification queue, card validity inspection, risk review', '["trades.read","trades.verify","evidence.read"]'::jsonb),
('FINANCE', 'Payout execution, bank settlement, ledger audit', '["payouts.manage","ledger.read","wallets.manage"]'::jsonb),
('SUPPORT', 'Customer support ticketing, in-app messaging assistance', '["support.manage","users.read"]'::jsonb),
('USER', 'Standard gift card trader', '["trades.create","trades.read_own","wallet.read_own","bank_accounts.manage"]'::jsonb)
ON CONFLICT (name) DO NOTHING;

-- 2. Insert Supported Gift Card Types
INSERT INTO gift_card_types (name, brand, slug, category, icon_drawable, min_denomination, max_denomination, is_active) VALUES
('Apple Gift Card', 'Apple', 'apple-gift-card', 'Tech & Apps', 'ic_card_apple', 25.00, 2000.00, true),
('Steam Wallet Card', 'Valve', 'steam-wallet-card', 'Gaming', 'ic_card_steam', 20.00, 1000.00, true),
('Amazon Gift Card', 'Amazon', 'amazon-gift-card', 'E-Commerce', 'ic_card_amazon', 25.00, 2000.00, true),
('Google Play Card', 'Google', 'google-play-card', 'Digital', 'ic_card_googleplay', 15.00, 500.00, true),
('Razer Gold PIN', 'Razer', 'razer-gold-pin', 'Gaming', 'ic_card_razer', 10.00, 1000.00, true),
('Xbox Gift Card', 'Microsoft', 'xbox-gift-card', 'Gaming', 'ic_card_xbox', 25.00, 500.00, true),
('PlayStation Network', 'Sony', 'playstation-network', 'Gaming', 'ic_card_playstation', 25.00, 500.00, true),
('Nike Gift Card', 'Nike', 'nike-gift-card', 'Fashion', 'ic_card_nike', 50.00, 1000.00, true),
('Sephora Gift Card', 'Sephora', 'sephora-gift-card', 'Beauty', 'ic_card_sephora', 50.00, 1000.00, true),
('Walmart Gift Card', 'Walmart', 'walmart-gift-card', 'Retail', 'ic_card_walmart', 50.00, 1500.00, true),
('eBay Gift Card', 'eBay', 'ebay-gift-card', 'Marketplace', 'ic_card_ebay', 25.00, 1000.00, true)
ON CONFLICT (name) DO NOTHING;

-- 3. Insert Initial Live Rates for Gift Cards
DO $$
DECLARE
    apple_id UUID;
    steam_id UUID;
    amazon_id UUID;
    gplay_id UUID;
    razer_id UUID;
    xbox_id UUID;
    psn_id UUID;
    nike_id UUID;
    sephora_id UUID;
    walmart_id UUID;
    ebay_id UUID;
BEGIN
    SELECT id INTO apple_id FROM gift_card_types WHERE slug = 'apple-gift-card';
    SELECT id INTO steam_id FROM gift_card_types WHERE slug = 'steam-wallet-card';
    SELECT id INTO amazon_id FROM gift_card_types WHERE slug = 'amazon-gift-card';
    SELECT id INTO gplay_id FROM gift_card_types WHERE slug = 'google-play-card';
    SELECT id INTO razer_id FROM gift_card_types WHERE slug = 'razer-gold-pin';
    SELECT id INTO xbox_id FROM gift_card_types WHERE slug = 'xbox-gift-card';
    SELECT id INTO psn_id FROM gift_card_types WHERE slug = 'playstation-network';
    SELECT id INTO nike_id FROM gift_card_types WHERE slug = 'nike-gift-card';
    SELECT id INTO sephora_id FROM gift_card_types WHERE slug = 'sephora-gift-card';
    SELECT id INTO walmart_id FROM gift_card_types WHERE slug = 'walmart-gift-card';
    SELECT id INTO ebay_id FROM gift_card_types WHERE slug = 'ebay-gift-card';

    -- Insert active rates
    INSERT INTO gift_card_rates (gift_card_type_id, region, currency, rate_per_unit, minimum_value, maximum_value, platform_fee_ngn) VALUES
    (apple_id, 'US', 'USD', 1430.00, 25.00, 2000.00, 0.00),
    (apple_id, 'UK', 'GBP', 1780.00, 25.00, 1500.00, 0.00),
    (apple_id, 'CA', 'CAD', 1050.00, 25.00, 2000.00, 0.00),
    (steam_id, 'US', 'USD', 1460.00, 20.00, 1000.00, 0.00),
    (steam_id, 'UK', 'GBP', 1810.00, 20.00, 1000.00, 0.00),
    (steam_id, 'EU', 'EUR', 1530.00, 20.00, 1000.00, 0.00),
    (amazon_id, 'US', 'USD', 1380.00, 25.00, 2000.00, 0.00),
    (amazon_id, 'UK', 'GBP', 1720.00, 25.00, 1500.00, 0.00),
    (gplay_id, 'US', 'USD', 1340.00, 15.00, 500.00, 0.00),
    (razer_id, 'US', 'USD', 1480.00, 10.00, 1000.00, 0.00),
    (xbox_id, 'US', 'USD', 1320.00, 25.00, 500.00, 0.00),
    (psn_id, 'US', 'USD', 1330.00, 25.00, 500.00, 0.00),
    (nike_id, 'US', 'USD', 1310.00, 50.00, 1000.00, 0.00),
    (sephora_id, 'US', 'USD', 1290.00, 50.00, 1000.00, 0.00),
    (walmart_id, 'US', 'USD', 1350.00, 50.00, 1500.00, 0.00),
    (ebay_id, 'US', 'USD', 1360.00, 25.00, 1000.00, 0.00);

    -- Insert core ledger chart of accounts
    INSERT INTO ledger_accounts (account_code, account_name, account_type) VALUES
    ('LIABILITY:USER_WALLETS', 'User Wallet Balances (NGN)', 'LIABILITY'),
    ('ASSET:SETTLEMENT_BANK', 'NIP Settlement Main Float Account', 'ASSET'),
    ('REVENUE:EXCHANGE_SPREAD', 'Card Trading Spread Margin', 'REVENUE'),
    ('REVENUE:PLATFORM_FEES', 'Platform Processing Fee Revenue', 'REVENUE'),
    ('EXPENSE:PAYOUT_CHARGES', 'NIBSS Interbank Transfer Fees', 'EXPENSE')
    ON CONFLICT (account_code) DO NOTHING;
END $$;
