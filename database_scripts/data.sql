-- Sample Test Data for KOINS Wallet & Loan Management System

-- IMPORTANT: The password for both users is 'password123'
-- Hash used: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIvi

-- 1. Insert Users
INSERT INTO users (id, full_name, email, phone_number, password_hash, status, role, email_verified, date_created, date_update)
VALUES 
(1001, 'Super Admin', 'admin@koins.com', '+2348000000001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIvi', 'ACTIVE', 'ADMIN', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1002, 'John Doe', 'johndoe@example.com', '+2348000000002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIvi', 'ACTIVE', 'USER', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 2. Insert Wallets
INSERT INTO wallets (id, user_id, balance, currency, status, date_created, date_update)
VALUES 
(2001, 1001, 1500000.00, 'NGN', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2002, 1002, 50000.00, 'NGN', 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 3. Insert KYC Details
INSERT INTO kyc_details (id, user_id, bvn, nin, status, date_created, date_update)
VALUES 
(3001, 1001, '12345678901', 'NIN123456789', 'VERIFIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3002, 1002, '10987654321', 'NIN987654321', 'VERIFIED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 4. Insert Sample Loan (for John Doe)
-- John has 50k balance, so max loan is 150k. He took a 100k loan.
INSERT INTO loans (id, user_id, amount, interest_rate, duration_in_days, status, repayment_schedule, balance_due, idempotency_key, date_disbursed, date_created, date_update)
VALUES 
(4001, 1002, 100000.00, 5.00, 30, 'DISBURSED', '[{"installment": 1, "dueDate": "2026-05-19", "amountDue": 105000.00}]', 105000.00, 'loan-req-12345', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 5. Insert Transactions
-- Admin funding wallet
INSERT INTO transactions (id, user_id, wallet_id, type, amount, status, reference_number, timestamp)
VALUES 
(5001, 1001, 2001, 'CREDIT', 1500000.00, 'SUCCESSFUL', 'FUND-ADM-001', CURRENT_TIMESTAMP);

-- John Doe funding wallet
INSERT INTO transactions (id, user_id, wallet_id, type, amount, status, reference_number, timestamp)
VALUES 
(5002, 1002, 2002, 'CREDIT', 50000.00, 'SUCCESSFUL', 'FUND-USR-001', CURRENT_TIMESTAMP);

-- John Doe loan disbursement
INSERT INTO transactions (id, user_id, wallet_id, type, amount, status, reference_number, timestamp)
VALUES 
(5003, 1002, 2002, 'LOAN_DISBURSEMENT', 100000.00, 'SUCCESSFUL', 'DISB-4001', CURRENT_TIMESTAMP);
