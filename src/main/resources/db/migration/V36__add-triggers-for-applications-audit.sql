create or replace function fn_applications_audit() returns trigger
as
$applications_audit$

begin
    if (TG_OP = 'DELETE') THEN
        insert into applications_audit SELECT 'D', now(), user, OLD.*;
    elsif (TG_OP = 'UPDATE') THEN
        insert into applications_audit SELECT 'U', now(), user, NEW.*;
    elsif (TG_OP = 'INSERT') THEN
       insert into applications_audit SELECT 'I', now(), user, NEW.*;
    end if;
    return null;
end;

$applications_audit$
    language plpgsql;

create trigger tr_applications_audit_trigger
    after insert or update or delete on applications
    for each row
execute procedure fn_applications_audit();