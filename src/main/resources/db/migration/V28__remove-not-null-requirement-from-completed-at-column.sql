ALTER TABLE applications
ALTER COLUMN completed_at DROP NOT NULL;

ALTER TABLE applications
ALTER COLUMN time_to_complete DROP NOT NULL;