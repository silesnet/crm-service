CREATE OR REPLACE VIEW dhcp_wireless_services
(
  service_id,
  master,
  mac,
  interface,
  rate,
  ip,
  status
)
AS 

SELECT d.service_id,
       d.master,
       d.mac,
       d.interface,
       (s.uplink || 'M/' || s.downlink || 'M') AS rate,
       CASE
           WHEN d.ip IS NULL THEN d.ip_class
           ELSE host(d.ip)
       END AS ip,
       CASE
          WHEN s.status='ACTIVE' THEN 1
          WHEN s.status='SUSPENDED' THEN 3
          WHEN s.status='DEBTOR' THEN 2
       END AS status
FROM dhcp_wireless d
LEFT JOIN service_connections s ON d.service_id = s.service_id

UNION

SELECT id AS service_id,
       NULL AS master,
       mac,
       interface,
       NULL AS rate,
       NULL AS ip,
       50 AS status
FROM ap_wireless

ORDER BY service_id
;

COMMIT;