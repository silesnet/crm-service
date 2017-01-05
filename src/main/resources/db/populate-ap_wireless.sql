-- SELECT *
-- FROM (SELECT mac
--       FROM pppoe p
--       WHERE MODE = '50'
--       GROUP BY mac
--       HAVING count(*) > 1) d
--   LEFT JOIN pppoe p ON d.mac = p.mac
-- WHERE p.mode = '50'
--

INSERT INTO ap_wireless
(
  name
  , mac
  , interface
)
SELECT login
       , p.mac
       , p.interface
FROM pppoe p
WHERE p.mode='50'

COMMIT;
