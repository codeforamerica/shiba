CREATE TABLE applications_audit
(
    op CHAR(1) NOT NULL,
    op_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    op_by VARCHAR NOT NULL,
    application_id VARCHAR NOT NULL,
    completed_at TIMESTAMP WITHOUT TIME ZONE,
    county VARCHAR NOT NULL DEFAULT 'OTHER',
    time_to_complete INTEGER DEFAULT 0,
    sentiment VARCHAR,
    feedback TEXT,
    flow VARCHAR,
    application_data jsonb,
    updated_at timestamp,
    caf_application_status VARCHAR,
    ccap_application_status VARCHAR,
    uploaded_documents_status VARCHAR,
    doc_upload_email_status VARCHAR,
    certain_pops_application_status VARCHAR
)