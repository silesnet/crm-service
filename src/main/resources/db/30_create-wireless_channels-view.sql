CREATE OR REPLACE VIEW wireless_channels
(
  mac,
  interface
)
AS 
SELECT d.mac,
       d.interface
FROM dhcp_wireless d
UNION
SELECT p.mac,
       p.interface
FROM pppoe p
where p.mode = 'WIRELESS'
UNION
SELECT a.mac,
       a.interface
FROM ap_wireless a
ORDER BY interface, mac
;

COMMIT;