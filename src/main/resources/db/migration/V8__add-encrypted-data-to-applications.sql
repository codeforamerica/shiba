ALTER TABLE applications
ADD encrypted_data CLOB;

ALTER TABLE applications
DROP COLUMN data;