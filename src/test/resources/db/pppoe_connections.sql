SELECT p.service_id,
       p.login,
       p.password,
       p.mac,
       p.mode,
       p.master,
       p.interface,
       CASE
           WHEN p.ip IS NULL THEN p.ip_class
           ELSE host(p.ip)
       END AS ip,
       (s.downlink || 'M/' || s.uplink || 'M') AS rate,
       s.status
FROM pppoe p
LEFT JOIN service_connections s ON p.service_id = s.service_id
ORDER BY service_id
;