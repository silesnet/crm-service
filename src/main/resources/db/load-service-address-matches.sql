
-- http://stackoverflow.com/questions/8910494/how-to-update-selected-rows-with-values-from-a-csv-file-in-postgres

-- import address matches into temporary table
CREATE TEMP TABLE tmp_address_matches (
  service_id bigint,
  address_fk text,
  gps_cord text
);

COPY tmp_address_matches FROM '{{matches}}' WITH csv header delimiter '|';

-- link matches to services.address_id
UPDATE services s
SET address_id=a.address_id
FROM tmp_address_matches m
  INNER JOIN addresses a using(address_fk)
WHERE s.id=m.service_id
;

-- add manual places
INSERT INTO places (gps_cord)
SELECT gps_cord FROM tmp_address_matches m WHERE m.gps_cord IS NOT NULL
;

-- link matching places to services.place_id
UPDATE services s
SET place_id=p.place_id
FROM tmp_address_matches m
  INNER JOIN places p ON m.gps_cord=p.gps_cord
WHERE
  s.id=m.service_id
  AND m.gps_cord IS NOT NULL
;

-- drop temporary table
DROP TABLE tmp_address_matches;

-- link address places to services.place_id, where applicable
UPDATE services s
SET place_id=a.place_id
FROM addresses a
  INNER JOIN places p using(place_id)
WHERE
  s.place_id is NULL
  AND s.address_id IS NOT NULL
  AND s.address_id=a.address_id
;

-- test select service with address and gps cords
SELECT s.id, s.name, c.name, a.label, p.gps_cord FROM services s
  INNER JOIN customers c ON s.customer_id=c.id
  LEFT JOIN places p using(place_id)
  LEFT JOIN addresses a using(address_id)
WHERE
  c.is_active
  AND c.name = 'Sikora Richard'  
