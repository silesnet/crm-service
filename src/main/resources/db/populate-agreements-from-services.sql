-- populate agreements from services

INSERT INTO agreements (id, country, customer_id, status)
SELECT DISTINCT (id / 100), 'CZ', customer_id, 'ACTIVE'
FROM services
WHERE id < 20000000
ORDER BY (id / 100);

INSERT INTO agreements (id, country, customer_id, status)
SELECT DISTINCT (id / 100), 'PL', customer_id, 'ACTIVE'
FROM services
WHERE id > 20000000
  AND id < 100000000
ORDER BY (id / 100);