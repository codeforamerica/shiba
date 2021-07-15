-- Uncomment for Oracle
create table applications
(
    id varchar2(100) not null constraint applications_pkey primary key,
    completed_at timestamp,
    county varchar2(100) default 'OTHER' not null,
    time_to_complete integer default 0,
    sentiment varchar2(100),
    feedback varchar2(4000),
    flow varchar2(100),
    application_data blob constraint ensure_json check (application_data IS JSON),
    updated_at timestamp,
    caf_application_status varchar2(100),
    ccap_application_status varchar2(100),
    uploaded_documents_status varchar2(100)
);

-- Uncomment for Postgres
-- create table applications
-- (
--     id varchar not null constraint applications_pkey primary key,
--     completed_at timestamp,
--     county varchar default 'OTHER'::character varying not null,
--     time_to_complete integer default 0,
--     sentiment varchar,
--     feedback text,
--     flow varchar,
--     application_data jsonb,
--     updated_at timestamp,
--     caf_application_status varchar,
--     ccap_application_status varchar,
--     uploaded_documents_status varchar
-- );

create sequence application_id maxvalue 9999999;