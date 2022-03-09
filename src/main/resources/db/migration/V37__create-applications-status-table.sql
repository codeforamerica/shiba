CREATE TABLE application_statuses_audit
(
    op CHAR(1) NOT NULL,
    op_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    op_by VARCHAR NOT NULL,
    application_id VARCHAR NOT NULL,
    document_type VARCHAR NOT NULL,
    routing_destination VARCHAR NOT NULL,
    status VARCHAR NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
)