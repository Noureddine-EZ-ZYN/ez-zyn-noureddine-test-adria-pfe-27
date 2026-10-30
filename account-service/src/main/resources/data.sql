-- Jeu de données de démonstration (identifiants fixes, rechargés à chaque démarrage)
INSERT INTO account (id, customer_reference, status, currency, balance, version) VALUES
    ('11111111-1111-1111-1111-111111111111', 'CUST-001', 'ACTIVE',  'EUR', 500, 0),
    ('22222222-2222-2222-2222-222222222222', 'CUST-002', 'ACTIVE',  'EUR', 200, 0),
    ('33333333-3333-3333-3333-333333333333', 'CUST-003', 'ACTIVE',  'USD', 300, 0),
    ('44444444-4444-4444-4444-444444444444', 'CUST-004', 'BLOCKED', 'EUR', 100, 0);
