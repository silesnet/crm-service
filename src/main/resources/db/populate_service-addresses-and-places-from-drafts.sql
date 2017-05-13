CREATE TEMP TABLE tmp_draft_addresses (
  service_id bigint,
  service_name text,
  customer_name text,
  address_fk text,
  address_place text,
  place text,
  address_id bigint,
  place_id bigint
);


INSERT INTO tmp_draft_addresses(service_id, service_name, customer_name, address_fk, address_place, place)
SELECT s.id
       , s.name
       , c.name
       , substring(d.data, '"address_id" ?: ?"([^"]+)",?') AS address_fk
       , translate(substring(d.data, '"address_place" ?: ?"([^"]+)",?'), ',', '') AS address_place
       , translate(substring(d.data, '"place" ?: ?"([^"]+)",?'), ',', '') AS place
FROM services s
  INNER JOIN customers c ON s.customer_id = c.id
  LEFT JOIN drafts2 d ON s.id = d.entity_id
WHERE s.place_id IS NULL
AND   s.id < 100000000
AND   d.data IS NOT NULL
AND   substring(d.data, '"place" ?: ?"([^"]+)",?') IS NOT NULL


UPDATE tmp_draft_addresses t
   SET address_id = a.address_id
FROM addresses a
WHERE t.address_fk = a.address_fk


UPDATE tmp_draft_addresses t
   SET place_id = a.place_id
FROM addresses a
WHERE t.address_fk IS NOT NULL
AND   t.address_place = t.place
AND   t.address_fk = a.address_fk


INSERT INTO places
(
  gps_cord
)
SELECT DISTINCT (place)
FROM tmp_draft_addresses
WHERE place_id IS NULL


UPDATE tmp_draft_addresses t
   SET place_id = p.place_id
FROM places p
WHERE t.place_id IS NULL
AND   t.place = p.gps_cord


UPDATE services s
   SET address_id = t.address_id
       , place_id = t.place_id
FROM tmp_draft_addresses t
WHERE s.id = t.service_id


DROP TABLE tmp_draft_addresses;
