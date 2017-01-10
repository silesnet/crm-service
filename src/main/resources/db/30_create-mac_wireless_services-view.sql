CREATE OR REPLACE VIEW mac_wireless_services
(
  service_id,
  mac,
  interface
)
AS 
SELECT d.service_id,
       d.mac,
       d.interface
FROM dhcp_wireless d
UNION
SELECT p.service_id,
       p.mac,
       p.interface
FROM pppoe p
where p.mode = 'WIRELESS'
ORDER BY service_id
;

COMMIT;