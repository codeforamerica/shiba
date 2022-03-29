ALTER TABLE application_status
    ADD COLUMN document_name VARCHAR DEFAULT '';
    
ALTER TABLE application_statuses_audit
    ADD COLUMN document_name VARCHAR DEFAULT '';
