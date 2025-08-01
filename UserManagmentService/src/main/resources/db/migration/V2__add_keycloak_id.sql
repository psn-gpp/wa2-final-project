ALTER TABLE generic_user
    ADD keycloak_id VARCHAR(255);

ALTER TABLE customer
    ADD CONSTRAINT uc_customer_drivinglicense UNIQUE (driving_license);

ALTER TABLE generic_user
    ADD CONSTRAINT uc_genericuser_email UNIQUE (email);

ALTER TABLE generic_user
    ADD CONSTRAINT uc_genericuser_phone UNIQUE (phone);

ALTER TABLE role
    ADD CONSTRAINT uc_role_namerole UNIQUE (name_role);