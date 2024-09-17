{{ config(schema='dbt_reporting') }}
select distinct
  replace(
        replace(
                trim(regexp_substr(publishers, '[^|]+', 1, levels.column_value))
            ,'[','')
        ,']','')  as publishers
from 
  {{ ref('stg_steam_game_data') }} t,
  table(cast(multiset(select level from dual connect by  level <= length (regexp_replace(publishers, '[^,]+'))  + 1) as sys.OdciNumberList)) levels
WHERE 1=1
AND replace(replace(trim(regexp_substr(publishers, '[^|]+', 1, levels.column_value)),'[',''),']','')
order by 1