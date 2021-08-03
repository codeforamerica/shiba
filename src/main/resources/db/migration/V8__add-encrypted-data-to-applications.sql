ALTER TABLE applications
ADD COLUMN encrypted_data BYTEA;

ALTER TABLE applications
ALTER COLUMN data DROP NOT NULL;