CREATE OR REPLACE VIEW pppoe_services
(
  service_id,
  login,
  password,
  mode,
  master,
  mac,
  interface,
  rate,
  ip,
  status
)
AS 

SELECT p.service_id,
       p.login,
       p.password,
       p.mode,
       p.master,
       p.mac,
       p.interface,
       (s.uplink || 'M/' || s.downlink || 'M') AS rate,
       CASE
           WHEN p.ip IS NULL THEN p.ip_class
           ELSE host(p.ip)
       END AS ip,
       s.status
FROM pppoe p
LEFT JOIN service_connections s ON p.service_id = s.service_id
ORDER BY service_id
;