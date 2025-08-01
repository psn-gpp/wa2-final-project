-- import.sql
INSERT INTO availability (id, type) VALUES (0, 'available') ON CONFLICT (id) DO NOTHING;
INSERT INTO availability (id, type) VALUES (1, 'rented') ON CONFLICT (id) DO NOTHING;

INSERT INTO category (id, category) VALUES (0, 'category1') ON CONFLICT (id) DO NOTHING;
INSERT INTO category (id, category) VALUES (1, 'category2') ON CONFLICT (id) DO NOTHING;
INSERT INTO category (id, category) VALUES (2, 'category3') ON CONFLICT (id) DO NOTHING;

INSERT INTO engine (id, type) VALUES (0, 'petrol') ON CONFLICT (id) DO NOTHING;
INSERT INTO engine (id, type) VALUES (1, 'diesel') ON CONFLICT (id) DO NOTHING;
INSERT INTO engine (id, type) VALUES (2, 'electric') ON CONFLICT (id) DO NOTHING;
INSERT INTO engine (id, type) VALUES (3, 'hybrid') ON CONFLICT (id) DO NOTHING;

INSERT INTO transmission (id, type) VALUES (0, 'manual') ON CONFLICT (id) DO NOTHING;
INSERT INTO transmission (id, type) VALUES (1, 'automatic') ON CONFLICT (id) DO NOTHING;

INSERT INTO drivetrain (id, type) VALUES (0, 'FWD') ON CONFLICT (id) DO NOTHING;
INSERT INTO drivetrain (id, type) VALUES (1, 'RWD') ON CONFLICT (id) DO NOTHING;
INSERT INTO drivetrain (id, type) VALUES (2, 'AWD') ON CONFLICT (id) DO NOTHING;

INSERT INTO infotainment (id, type) VALUES (0, 'radio') ON CONFLICT (id) DO NOTHING;
INSERT INTO infotainment (id, type) VALUES (1, 'USB') ON CONFLICT (id) DO NOTHING;
INSERT INTO infotainment (id, type) VALUES (2, 'Bluetooth') ON CONFLICT (id) DO NOTHING;

INSERT INTO safety_features (id, feature) VALUES (0, 'feature1') ON CONFLICT (id) DO NOTHING;
INSERT INTO safety_features (id, feature) VALUES (1, 'feature2') ON CONFLICT (id) DO NOTHING;
INSERT INTO safety_features (id, feature) VALUES (2, 'feature3') ON CONFLICT (id) DO NOTHING;

INSERT INTO status (id, status) VALUES (0, 'APPROVED') ON CONFLICT (id) DO NOTHING;
INSERT INTO status (id, status) VALUES (1, 'REJECTED') ON CONFLICT (id) DO NOTHING;
INSERT INTO status (id, status) VALUES (2, 'PENDING') ON CONFLICT (id) DO NOTHING;
INSERT INTO status (id, status) VALUES (3, 'ON_COURSE') ON CONFLICT (id) DO NOTHING;
INSERT INTO status (id, status) VALUES (4, 'TERMINATED') ON CONFLICT (id) DO NOTHING;
INSERT INTO status (id, status) VALUES (5, 'PAYED') ON CONFLICT (id) DO NOTHING;
INSERT INTO status (id, status) VALUES (6, 'PAYMENT_REFUSED') ON CONFLICT (id) DO NOTHING;



