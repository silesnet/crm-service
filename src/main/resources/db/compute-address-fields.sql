UPDATE municipalities SET
  label = name || ', ' || country,
  lexems = to_tsvector(normalize_text(name || ', ' || country));
 
ALTER TABLE municipalities
  ALTER COLUMN label SET NOT NULL,
  ALTER COLUMN lexems SET NOT NULL;


UPDATE districts d SET
  municipality_id = m.municipality_id,
  label = (
    CASE
      WHEN d.name = m.name THEN m.label
      ELSE m.name || ' - ' || d.name || ', ' || m.country
    END
   ),
  lexems = to_tsvector(normalize_text(
    CASE
      WHEN d.name = m.name THEN m.label
      ELSE m.name || ' - ' || d.name || ', ' || m.country
    END
  ))
FROM municipalities m
WHERE d.municipality_fk = m.municipality_fk;

ALTER TABLE districts
  DROP COLUMN municipality_fk,
  ALTER COLUMN label SET NOT NULL,
  ALTER COLUMN lexems SET NOT NULL;

  
UPDATE streets s SET
  municipality_id = m.municipality_id,
  label = (
    CASE
      WHEN s.name = m.name THEN m.label
      ELSE s.name || ', ' || m.label
    END
   ),
  lexems = to_tsvector(normalize_text(
    CASE
      WHEN s.name = m.name THEN m.label
      ELSE s.name || ', ' || m.label
    END
  ))
FROM municipalities m
WHERE s.municipality_fk = m.municipality_fk;

ALTER TABLE streets
  DROP COLUMN municipality_fk,
  ALTER COLUMN label SET NOT NULL,
  ALTER COLUMN lexems SET NOT NULL;


UPDATE addresses a SET
  municipality_id = m.municipality_id,
  district_id = d.district_id,
  street_id = s.street_id,
  place_id = p.place_id,
  label = (
    CASE
      WHEN s.street_id IS NOT NULL THEN
        s.name
      ELSE
        CASE
          WHEN (a.district_id IS NOT NULL AND d.name != m.name) THEN
            d.name
          ELSE
            m.name
        END
    END
    || ' ' ||
    CASE
      WHEN a.registration_no IS NOT NULL THEN
        'ev.č ' || a.registration_no
      ELSE
        concat_ws('/', a.descriptive_no, a.orientation_no)
    END
    || ', ' ||
    CASE
      WHEN m.country='CZ' THEN
        left(a.postal_code, 3) || ' ' || right(a.postal_code, 2)
      WHEN m.country='PL' THEN
        left(a.postal_code, 2) || '-' || right(a.postal_code, 3)
      ELSE
        a.postal_code
    END
    || ' ' ||
    m.label
  ),
  lexems = to_tsvector(normalize_text(
    CASE
      WHEN s.street_id IS NOT NULL THEN
        s.name
      ELSE
        CASE
          WHEN (a.district_id IS NOT NULL AND d.name != m.name) THEN
            d.name
          ELSE
            m.name
        END
    END
    || ' ' ||
    CASE
      WHEN a.registration_no IS NOT NULL THEN
        'ev.č ' || a.registration_no
      ELSE
        concat_ws(' ', a.descriptive_no, a.orientation_no) -- DIFF: has to be separate after normalization
    END
    || ', ' || a.postal_code || ' ' || m.label -- DIFF: postal code should not be formatted
  ))
FROM addresses aa
  INNER JOIN municipalities m USING (municipality_fk)
  LEFT JOIN districts d USING (district_fk)
  LEFT JOIN streets s USING (street_fk)
  LEFT JOIN places p USING (address_fk)
WHERE a.address_id=aa.address_id;

ALTER TABLE addresses
  DROP COLUMN municipality_fk,
  DROP COLUMN district_fk,
  DROP COLUMN street_fk,
  ALTER COLUMN label SET NOT NULL,
  ALTER COLUMN lexems SET NOT NULL;

