create or replace function fn_application_statuses_audit() returns trigger
as
$application_statuses_audit$

begin
    if (TG_OP = 'DELETE') THEN
        insert into application_statuses_audit SELECT 'D', now(), user, OLD.*;
    elsif (TG_OP = 'UPDATE') THEN
        insert into application_statuses_audit SELECT 'U', now(), user, NEW.*;
    elsif (TG_OP = 'INSERT') THEN
        insert into application_statuses_audit SELECT 'I', now(), user, NEW.*;
    end if;
    return null;
end;

$application_statuses_audit$
    language plpgsql;

create trigger tr_application_statuses_audit_trigger
    after insert or update or delete on application_status
    for each row
execute procedure fn_application_statuses_audit();