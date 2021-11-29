CREATE DOMAIN IF NOT EXISTS "JSONB" AS TEXT;

CREATE SEQUENCE IF NOT EXISTS application_id START WITH 1 MAXVALUE 9999999;

CREATE TABLE IF NOT EXISTS applications
(
    id                              varchar                                     NOT NULL
        CONSTRAINT applications_pkey
            PRIMARY KEY,
    completed_at                    timestamp,
    county                          varchar DEFAULT 'OTHER':: CHARACTER varying NOT NULL,
    time_to_complete                integer DEFAULT 0,
    sentiment                       varchar,
    feedback                        text,
    flow                            varchar,
    application_data                jsonb,
    updated_at                      varchar,
    caf_application_status          varchar,
    ccap_application_status         varchar,
    certain_pops_application_status varchar,
    uploaded_documents_status       varchar,
    doc_upload_email_status         varchar
);

CREATE TABLE IF NOT EXISTS  application_status
(
    application_id      VARCHAR,
    document_type       VARCHAR,
    routing_destination VARCHAR,
    status              VARCHAR,
    created_at          TIMESTAMP,
    updated_at          TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_application_id
    ON applications (id);

CREATE INDEX IF NOT EXISTS application_status_index
  ON application_status (application_id, document_type, routing_destination);

CREATE TABLE IF NOT EXISTS spring_session
(
    primary_id            char(36) NOT NULL
        CONSTRAINT spring_session_pk
            PRIMARY KEY,
    session_id            char(36) NOT NULL,
    creation_time         bigint   NOT NULL,
    last_access_time      bigint   NOT NULL,
    max_inactive_interval integer  NOT NULL,
    expiry_time           bigint   NOT NULL,
    principal_name        varchar(100)
);

CREATE UNIQUE INDEX IF NOT EXISTS spring_session_ix1
    ON spring_session (session_id);

CREATE INDEX IF NOT EXISTS spring_session_ix2
    ON spring_session (expiry_time);

CREATE INDEX IF NOT EXISTS spring_session_ix3
    ON spring_session (principal_name);

CREATE TABLE IF NOT EXISTS spring_session_attributes
(
    session_primary_id char(36)     NOT NULL
        CONSTRAINT spring_session_attributes_fk
            REFERENCES spring_session
            ON DELETE CASCADE,
    attribute_name     varchar(200) NOT NULL,
    attribute_bytes    bytea        NOT NULL,
    CONSTRAINT spring_session_attributes_pk
        PRIMARY KEY (session_primary_id, attribute_name)
);

CREATE TABLE IF NOT EXISTS shedlock
(
    name       VARCHAR(64),
    lock_until TIMESTAMP(3) NULL,
    locked_at  TIMESTAMP(3) NULL,
    locked_by  VARCHAR(255),
    PRIMARY KEY (name)
);