UPDATE services AS s
SET data = CASE
             WHEN substring(d.data, '"devices"\s?:\s?\[\s?\{.*\}\s?\]') IS NOT NULL
             THEN regexp_replace(regexp_replace(('{' || substring(d.data, '"devices"\s?:\s?\[\s?\{.*\}\s?\]') || '}'), '\r?\n', '' , 'g'), '  +', ' ', 'g')
             ELSE '{}'
           END
FROM drafts2 AS d
WHERE s.id = d.entity_id
  AND d.entity_type = 'services'
  AND d.status = 'IMPORTED'
;

select * from services where data != '{}';

ROLLBACK;

commit;


