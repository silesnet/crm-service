CREATE OR REPLACE VIEW service_connections
(
  service_id,
  name,
  downlink,
  uplink,
  status
)
AS 

SELECT s.id AS service_id,
       s.name,
       s.download AS downlink,
       s.upload AS uplink,
       CASE
           WHEN s.status != 'INHERIT_FROM_CUSTOMER' THEN s.status
           WHEN NOT c.is_active THEN 'SUSPENDED'
           WHEN NOW() < s.period_from OR (s.period_to IS NOT NULL AND s.period_to + '1 day' < NOW()) THEN 'SUSPENDED'
           WHEN c.status = ANY (ARRAY[10, 30, 40, 50]) THEN 'ACTIVE'
           WHEN c.status = 20 AND NOW() <= c.lastly_billed THEN 'ACTIVE'
           WHEN c.status = 20 THEN 'SUSPENDED'
           ELSE 'DEBTOR'
       END AS status
FROM services AS s
LEFT JOIN customers AS c ON s.customer_id = c.id
WHERE s.download IS NOT NULL
  AND s.upload IS NOT NULL

UNION

SELECT entity_id AS service_id,
       SUBSTRING(data FROM '"product_name" ?: ?"([^"]*)",?') AS name,
       SUBSTRING(data FROM '"downlink" ?: ?"([0-9]+)",?')::INT AS downlink,
       SUBSTRING(data FROM '"uplink" ?: ?"([0-9]+)",?')::INT AS uplink,
       'ACTIVE' AS status
FROM drafts2
WHERE entity_type = 'services'
  AND status != 'IMPORTED'

ORDER BY service_id
;
