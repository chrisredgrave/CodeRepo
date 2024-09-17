{{ config(schema='dbt_reporting') }}
select distinct
  replace(
        replace(
                trim(regexp_substr(SUPPORTED_LANGUAGES, '[^|]+', 1, levels.column_value))
            ,'[','')
        ,']','')  as SUPPORTED_LANGUAGES
from 
  {{ ref('stg_steam_game_data') }} t,
  table(cast(multiset(select level from dual connect by  level <= length (regexp_replace(SUPPORTED_LANGUAGES, '[^,]+'))  + 1) as sys.OdciNumberList)) levels
WHERE 1=1
AND trim(regexp_substr(SUPPORTED_LANGUAGES, '[^|]+', 1, levels.column_value)) NOT LIKE '%\r%'
AND trim(regexp_substr(SUPPORTED_LANGUAGES, '[^|]+', 1, levels.column_value)) NOT LIKE '%amp;%'
AND trim(regexp_substr(SUPPORTED_LANGUAGES, '[^|]+', 1, levels.column_value)) NOT LIKE '%/b%'
AND replace(replace(trim(regexp_substr(SUPPORTED_LANGUAGES, '[^|]+', 1, levels.column_value)),'[',''),']','') IS NOT NULL
order by 1