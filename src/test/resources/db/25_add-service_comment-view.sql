CREATE OR REPLACE VIEW service_comments
(
    todo_id,
    customer_id,
    service_id,
    "user",
    date,
    comment
)
AS
SELECT t.id
       , t.registry::bigint
       , NULL::bigint
       , c.technician
       , c.added_time
       , c.comment
FROM pltodo_comment AS c
  INNER JOIN pltodo AS t ON c.pltodo_id = t.id
WHERE t.registry IS NOT NULL
ORDER BY t.registry ASC
         , c.added_time DESC
;