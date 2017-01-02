INSERT INTO dhcp_wireless
(
  service_id
  , mac
  , interface
  , master
  , ip
  , ip_class
)
SELECT p.service_id
       , p.mac
       , p.interface
       , p.master
       , p.ip
       , p.ip_class
FROM pppoe p
  INNER JOIN services s ON p.service_id = s.id
WHERE s.name LIKE '%max min.';

COMMIT;

