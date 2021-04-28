CREATE DOMAIN IF NOT EXISTS "JSONB" AS text;

CREATE SEQUENCE IF NOT EXISTS application_id START WITH 1 MAXVALUE 9999999;

CREATE TABLE IF NOT EXISTS applications
(
    id               varchar           NOT NULL
        CONSTRAINT applications_pkey
            PRIMARY KEY,
    completed_at     timestamp         NOT NULL,
    county           varchar DEFAULT 'OTHER':: CHARACTER varying NOT NULL,
    time_to_complete integer DEFAULT 0 NOT NULL,
    sentiment        varchar,
    feedback         text,
    flow             varchar,
    application_data jsonb
);

CREATE TABLE IF NOT EXISTS research
(
    spoken_language          varchar,
    written_language         varchar,
    first_name               varchar,
    last_name                varchar,
    date_of_birth            date,
    sex                      varchar,
    phone_number             varchar,
    email                    varchar,
    phone_opt_in             boolean,
    email_opt_in             boolean,
    zip_code                 varchar,
    snap                     boolean,
    cash                     boolean,
    housing                  boolean,
    emergency                boolean,
    has_household            boolean,
    money_made_last30_days   numeric,
    pay_rent_or_mortgage     boolean,
    home_expenses_amount     numeric,
    are_you_working          boolean,
    self_employment          boolean,
    social_security          boolean,
    ssi                      boolean,
    veterans_benefits        boolean,
    unemployment             boolean,
    workers_compensation     boolean,
    retirement               boolean,
    child_or_spousal_support boolean,
    tribal_payments          boolean,
    household_size           integer,
    entered_ssn              boolean,
    flow                     varchar,
    application_id           varchar,
    county                   varchar,
    childcare                boolean
);

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
