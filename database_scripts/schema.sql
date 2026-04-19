
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(50),
    role VARCHAR(50),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    date_created TIMESTAMP,
    date_update TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_phone_number ON users(phone_number);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);

CREATE TABLE IF NOT EXISTS wallets (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(50),
    date_created TIMESTAMP,
    date_update TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_wallets_user_id ON wallets(user_id);
CREATE INDEX IF NOT EXISTS idx_wallets_status ON wallets(status);

CREATE TABLE IF NOT EXISTS kyc_details (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    bvn VARCHAR(255) UNIQUE,
    nin VARCHAR(255) UNIQUE,
    status VARCHAR(50),
    date_created TIMESTAMP,
    date_update TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_kyc_user_id ON kyc_details(user_id);
CREATE INDEX IF NOT EXISTS idx_kyc_bvn ON kyc_details(bvn);
CREATE INDEX IF NOT EXISTS idx_kyc_nin ON kyc_details(nin);
CREATE INDEX IF NOT EXISTS idx_kyc_status ON kyc_details(status);

CREATE TABLE IF NOT EXISTS loans (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    interest_rate DECIMAL(5, 2) NOT NULL,
    duration_in_days INTEGER NOT NULL,
    status VARCHAR(50),
    repayment_schedule TEXT,
    balance_due DECIMAL(19, 2),
    idempotency_key VARCHAR(255) UNIQUE,
    date_disbursed TIMESTAMP,
    date_created TIMESTAMP,
    date_update TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_loans_user_id ON loans(user_id);
CREATE INDEX IF NOT EXISTS idx_loans_status ON loans(status);
CREATE INDEX IF NOT EXISTS idx_loans_idempotency_key ON loans(idempotency_key);

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    wallet_id BIGINT NOT NULL,
    type VARCHAR(50),
    amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(50),
    reference_number VARCHAR(255) NOT NULL UNIQUE,
    timestamp TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_wallet_id ON transactions(wallet_id);
CREATE INDEX IF NOT EXISTS idx_transactions_reference_number ON transactions(reference_number);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON transactions(status);

CREATE TABLE IF NOT EXISTS otp_details (
    id BIGINT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    otp VARCHAR(255) NOT NULL,
    type VARCHAR(50),
    expiry_date TIMESTAMP,
    date_created TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_otp_email ON otp_details(email);
CREATE INDEX IF NOT EXISTS idx_otp_email_type ON otp_details(email, type);
CREATE INDEX IF NOT EXISTS idx_otp_expiry_date ON otp_details(expiry_date);
