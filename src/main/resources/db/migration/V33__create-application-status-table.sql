CREATE TABLE document_status
(
    application_id      VARCHAR,
    document_type       VARCHAR,
    routing_destination VARCHAR,
    status              VARCHAR,
    created_at          TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at          TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE INDEX application_status_index
    ON document_status (application_id, document_type, routing_destination);

-- See V32 migration
-- Previously we were making separate db calls for updating each document status, and we were making
--  many calls to update the `updated_at` column on every post request.
-- Now we update all of the document statuses during `applicationRepository.save()` and the
--  `updated_at` column gets updated via a db trigger whenever we save to the `applications` table.
CREATE TRIGGER set_timestamp_on_application_status
    BEFORE UPDATE
    ON document_status
    FOR EACH ROW
EXECUTE PROCEDURE trigger_set_timestamp();

-- Remove deprecated table
DROP TABLE IF EXISTS research;