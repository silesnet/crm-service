DELETE
FROM pppoe p USING services s
WHERE p.service_id = s.id
AND   s.name LIKE '%max min.';

COMMIT;
