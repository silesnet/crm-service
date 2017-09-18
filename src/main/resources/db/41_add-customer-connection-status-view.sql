CREATE OR REPLACE VIEW customer_connection_status
(
  customer_id,
  active_status,
  suspended_status,
  debtor_status
)
AS 
SELECT s.customer_id
       , JSON_OBJECT(ARRAY_AGG(s.id::TEXT), ARRAY_AGG(c.status))
       , CASE
         WHEN REPLACE(STRING_AGG(c.status,''),'ACTIVE','') = '' THEN 'ALL_ACTIVE'
         WHEN REPLACE(STRING_AGG(c.status,''),'ACTIVE','') != STRING_AGG(c.status,'') THEN 'SOME_ACTIVE'
         ELSE 'NONE_ACTIVE'
       END AS active_status
       , CASE
         WHEN REPLACE(STRING_AGG(c.status,''),'SUSPENDED','') = '' THEN 'ALL_SUSPENDED'
         WHEN REPLACE(STRING_AGG(c.status,''),'SUSPENDED','') != STRING_AGG(c.status,'') THEN 'SOME_SUSPENDED'
         ELSE 'NONE_SUSPENDED'
       END AS suspended_status
       , CASE
         WHEN REPLACE(STRING_AGG(c.status,''),'DEBTOR','') = '' THEN 'ALL_DEBTOR'
         WHEN REPLACE(STRING_AGG(c.status,''),'DEBTOR','') != STRING_AGG(c.status,'') THEN 'SOME_DEBTOR'
         ELSE 'NONE_DEBTOR'
       END AS debtor_status
FROM services s
  INNER JOIN service_connections c ON s.id = c.service_id
GROUP BY s.customer_id
ORDER BY s.customer_id;
