CREATE OR REPLACE VIEW dhcp_services
(
  service_id,
  switch,
  port,
  download,
  upload,
  ip,
  status,
  master
)
AS 
 
SELECT d.service_id,
       CASE
           WHEN d.network_id > 0 THEN n.name::text
           ELSE d.t_switch
       END AS switch,
       d.port,
       s.downlink AS download,
       s.uplink AS upload,
       d.ip,
       s.status,
       CASE
           WHEN d.network_id > 0 THEN n.master
           ELSE NULL::text
       END AS master
FROM dhcp AS d
LEFT JOIN service_connections s ON d.service_id = s.service_id
LEFT JOIN network n ON d.network_id = n.id
WHERE d.service_id IS NOT NULL
ORDER BY service_id
;