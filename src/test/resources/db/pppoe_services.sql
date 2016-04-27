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
       CASE
          WHEN p.mode='LAN' THEN 0
          WHEN p.mode='WIRELESS' THEN 1
          WHEN p.mode='50' THEN 50
       END AS mode,
       p.master,
       p.mac,
       p.interface,
       (s.uplink || 'M/' || s.downlink || 'M') AS rate,
       CASE
           WHEN p.ip IS NULL THEN p.ip_class
           ELSE host(p.ip)
       END AS ip,
       CASE
          WHEN p.mode='50' THEN 50
          WHEN s.status='ACTIVE' THEN 1
          WHEN s.status='SUSPENDED' THEN 3
          WHEN s.status='DEBTOR' THEN 2
       END AS status
FROM pppoe p
LEFT JOIN service_connections s ON p.service_id = s.service_id
ORDER BY service_id
;