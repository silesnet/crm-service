UPDATE municipalities SET
  label = name || ', ' || country,
  lexems = to_tsvector('simple', normalize_text(name || ', ' || country));


UPDATE districts d SET
  municipality_id = m.municipality_id,
  label = (
    CASE
      WHEN d.name = m.name THEN m.label
      ELSE m.name || ' - ' || d.name || ', ' || m.country
    END
   ),
  lexems = to_tsvector('simple', normalize_text(
    CASE
      WHEN d.name = m.name THEN m.label
      ELSE m.name || ' - ' || d.name || ', ' || m.country
    END
  ))
FROM municipalities m
WHERE d.municipality_id = m.municipality_id;


UPDATE streets s SET
  municipality_id = m.municipality_id,
  label = (
    CASE
      WHEN s.name = m.name THEN m.label
      ELSE s.name || ', ' || m.label
    END
   ),
  lexems = to_tsvector('simple', normalize_text(
    CASE
      WHEN s.name = m.name THEN m.label
      ELSE s.name || ', ' || m.label
    END
  ))
FROM municipalities m
WHERE s.municipality_id = m.municipality_id;


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
  lexems = to_tsvector('simple', normalize_text(
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
  INNER JOIN municipalities m USING (municipality_id)
  LEFT JOIN districts d USING (district_id)
  LEFT JOIN streets s USING (street_id)
  LEFT JOIN places p USING (address_fk)
WHERE a.address_id=aa.address_id;
