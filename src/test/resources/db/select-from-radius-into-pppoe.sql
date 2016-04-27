INSERT INTO pppoe
(
  service_id,
  login,
  password,
  mode,
  master,
  mac,
  interface,
  ip
)
SELECT id AS service_id,
       username AS login,
       password,
       CASE
         WHEN mode = 0 THEN 'LAN'
         WHEN mode = 1 THEN 'WIRELESS'
         ELSE ''
       END AS mode,
       CASE
         WHEN master IS NULL THEN ''
         ELSE master
       END AS master,
       CASE
         WHEN mac = '' THEN NULL
         ELSE mac::macaddr
       END AS mac,
       CASE
         WHEN interface IS NULL THEN ''
         ELSE interface
       END AS interface,
       CASE
         WHEN address = 'public-pl' THEN NULL
         WHEN address = '' THEN NULL
         ELSE address::inet
       END AS ip
FROM radius
WHERE status != 50
AND   id NOT IN (20555501,20018401,20148502,20067701,20190501,20098401,20045401,20145001,20200601,20174501,10249701)
;

UPDATE pppoe
   SET location = r.location
FROM (SELECT id, location FROM radius_extrainfo) AS r
WHERE pppoe.service_id = r.id
AND   r.location IS NOT NULL
;

