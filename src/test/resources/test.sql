CREATE DOMAIN IF NOT EXISTS "JSONB" AS json;

CREATE SEQUENCE IF NOT EXISTS application_id START WITH 1 MAXVALUE 9999999;

create table applications
(
    id               varchar           not null
        constraint applications_pkey
            primary key,
    completed_at     timestamp         not null,
    county           varchar default 'OTHER':: character varying not null,
    time_to_complete integer default 0 not null,
    sentiment        varchar,
    feedback         text,
    flow             varchar,
    application_data jsonb
);

create table research
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

create table spring_session
(
    primary_id            char(36) not null
        constraint spring_session_pk
            primary key,
    session_id            char(36) not null,
    creation_time         bigint   not null,
    last_access_time      bigint   not null,
    max_inactive_interval integer  not null,
    expiry_time           bigint   not null,
    principal_name        varchar(100)
);

create unique index spring_session_ix1
    on spring_session (session_id);

create index spring_session_ix2
    on spring_session (expiry_time);

create index spring_session_ix3
    on spring_session (principal_name);

create table spring_session_attributes
(
    session_primary_id char(36)     not null
        constraint spring_session_attributes_fk
            references spring_session
            on delete cascade,
    attribute_name     varchar(200) not null,
    attribute_bytes    bytea        not null,
    constraint spring_session_attributes_pk
        primary key (session_primary_id, attribute_name)
);
