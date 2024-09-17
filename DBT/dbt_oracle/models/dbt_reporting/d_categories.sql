{{ config(schema='dbt_reporting') }}
select distinct
  replace(
        replace(
                trim(regexp_substr(categories, '[^|]+', 1, levels.column_value))
            ,'[','')
        ,']','')  as categories
from 
  {{ ref('stg_steam_game_data') }} t,
  table(cast(multiset(select level from dual connect by  level <= length (regexp_replace(categories, '[^,]+'))  + 1) as sys.OdciNumberList)) levels
WHERE 1=1
AND replace(replace(trim(regexp_substr(categories, '[^|]+', 1, levels.column_value)),'[',''),']','') IS NOT NULL
order by 1