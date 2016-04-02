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
           WHEN c.is_active
                AND s.period_from < NOW()
                AND (s.period_to IS NULL
                     OR s.period_to > NOW())
                AND (c.status = ANY (ARRAY[10,30,40,50])) THEN 'ACTIVE'
           WHEN c.is_active
                AND s.period_from < NOW()
                AND (s.period_to IS NULL
                     OR s.period_to > NOW())
                AND c.status = 20
                AND NOW() <= c.lastly_billed THEN 'ACTIVE'
           WHEN c.is_active
                AND s.period_from < NOW()
                AND (s.period_to IS NULL
                     OR s.period_to > NOW())
                AND c.status = 20
                AND c.lastly_billed < NOW() THEN 'SUSPENDED'
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
