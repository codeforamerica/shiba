ALTER TABLE application_status
    ADD COLUMN filenet_id VARCHAR DEFAULT '';
ALTER TABLE application_statuses_audit
    ADD COLUMN filenet_id VARCHAR DEFAULT '';
