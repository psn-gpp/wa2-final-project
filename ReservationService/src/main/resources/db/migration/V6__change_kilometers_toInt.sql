ALTER TABLE vehicle
    DROP COLUMN kilometers;

ALTER TABLE vehicle
    ADD kilometers INTEGER NOT NULL;