INSERT INTO pppoe
(
  service_id,
  login,
  password,
  mode,
  master,
  mac,
  interface,
  ip,
  ip_class,
  area
)
SELECT r.id AS service_id,
       r.username AS login,
       CASE
         WHEN r.password IS NULL THEN ''
         ELSE r.password
       END AS password,
       CASE
         WHEN r.mode = 0 THEN 'LAN'
         WHEN r.mode = 1 THEN 'WIRELESS'
         WHEN r.mode = 50 THEN '50'
         ELSE ''
       END AS mode,
       CASE
         WHEN r.master IS NULL THEN ''
         ELSE r.master
       END AS master,
       CASE
         WHEN r.mac !~ '^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$' THEN NULL
         ELSE r.mac::macaddr
       END AS mac,
       CASE
         WHEN r.interface IS NULL THEN ''
         ELSE r.interface
       END AS interface,
       CASE
         WHEN r.address = 'public-pl' THEN NULL
         WHEN r.address = 'internal-cz' THEN NULL
         WHEN r.address = '' THEN NULL
         ELSE r.address::inet
       END AS ip,
       CASE
         WHEN r.address = 'public-pl' THEN 'public-pl'
         WHEN r.address = 'internal-cz' THEN 'internal-cz'
         WHEN r.address = '' THEN ''
         ELSE 'static'
       END AS ip_class,
       e.area
FROM radius AS r
INNER JOIN radius_extrainfo AS e ON r.id = e.id
;

UPDATE pppoe
   SET location = r.location
FROM (SELECT id, location FROM radius_extrainfo) AS r
WHERE pppoe.service_id = r.id
AND   r.location IS NOT NULL
;

UPDATE pppoe
   SET area = r.area
FROM (SELECT id, area FROM radius_extrainfo) AS r
WHERE pppoe.service_id = r.id
;
