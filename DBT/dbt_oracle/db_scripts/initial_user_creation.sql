create user dbt_user identified by R1ttmanmead01;
grant DWROLE to dbt_user;
alter user dbt_user quota unlimited on data;

create user dbt_staging identified by R1ttmanmead01;
grant DWROLE to dbt_staging;
alter user dbt_staging quota unlimited on data;

create user dbt_reporting identified by R1ttmanmead01;
grant DWROLE to dbt_reporting;
alter user dbt_reporting quota unlimited on data;


GRANT create any view to dbt_user;
GRANT create any table to dbt_user;

GRANT create any view to dbt_reporting;
GRANT create any table to dbt_reporting;

GRANT create any view to dbt_staging;
GRANT create any table to dbt_staging;