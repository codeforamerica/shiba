CREATE TABLE application_status
(
    id                  VARCHAR NOT NULL PRIMARY KEY,
    application_id      VARCHAR,
    document_type       VARCHAR,
    routing_destination VARCHAR,
    status              VARCHAR,
    created_at          TIMESTAMP WITHOUT TIME ZONE,
    updated_at          TIMESTAMP WITHOUT TIME ZONE
);

CREATE INDEX application_status_index
    ON application_status (application_id, document_type, routing_destination);