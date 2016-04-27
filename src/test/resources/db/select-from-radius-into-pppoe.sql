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
  ip_class
)
SELECT id AS service_id,
       username AS login,
       CASE
         WHEN password IS NULL THEN ''
         ELSE password
       END AS password,
       CASE
         WHEN mode = 0 THEN 'LAN'
         WHEN mode = 1 THEN 'WIRELESS'
         WHEN mode = 50 THEN '50'
         ELSE ''
       END AS mode,
       CASE
         WHEN master IS NULL THEN ''
         ELSE master
       END AS master,
       CASE
         WHEN mac !~ '^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$' THEN NULL
         ELSE mac::macaddr
       END AS mac,
       CASE
         WHEN interface IS NULL THEN ''
         ELSE interface
       END AS interface,
       CASE
         WHEN address = 'public-pl' THEN NULL
         WHEN address = 'internal-cz' THEN NULL
         WHEN address = '' THEN NULL
         ELSE address::inet
       END AS ip,
       CASE
         WHEN address = 'public-pl' THEN 'public-pl'
         WHEN address = 'internal-cz' THEN 'internal-cz'
         WHEN address = '' THEN ''
         ELSE 'static'
       END AS ip_class
FROM radius
;

UPDATE pppoe
   SET location = r.location
FROM (SELECT id, location FROM radius_extrainfo) AS r
WHERE pppoe.service_id = r.id
AND   r.location IS NOT NULL
;
