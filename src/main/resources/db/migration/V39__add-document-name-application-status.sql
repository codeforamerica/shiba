ALTER TABLE application_status
    ADD COLUMN document_name VARCHAR;
    
ALTER TABLE application_statuses_audit
    ADD COLUMN document_name VARCHAR;
