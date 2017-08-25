CREATE OR REPLACE FUNCTION normalize_text(term TEXT) RETURNS TEXT AS $$
BEGIN
  RETURN
    lower(
      trim(
        regexp_replace(
          translate(
            term,
            'ÁĄÄČĆĎÉĚĘËÍŁŇŃÓÖŘŠŚŤÚŮÜÝŽŻŹáąäčćďéěęëíłňńóöřšśťúůüýžżź/.,;&+?:-',
            'aaaccdeeeeilnnoorsstuuuyzzzaaaccdeeeeilnnoorsstuuuyzzz       '
          ),
          '\s+', ' ', 'g')));
END;
$$ LANGUAGE PLPGSQL;

-- use '$1' instead of 'query' for older version of pgSQL
CREATE OR REPLACE FUNCTION address_query(query TEXT) RETURNS TSQUERY AS $$
  SELECT to_tsquery('simple', string_agg(
    CASE
      WHEN t.alias = 'asciiword' OR t.alias = 'numword' THEN p.token || ':*'
      WHEN t.alias = 'blank' THEN ' & '
      ELSE p.token
    END,''))
  FROM
    ts_parse ('default', normalize_text(query)) p
    , ts_token_type('default') t
  WHERE
    p.tokid = t.tokid;
$$ LANGUAGE SQL;

-- use '$1' instead of 'query' for older version of pgSQL
CREATE OR REPLACE FUNCTION prefix_query(query TEXT) RETURNS TSQUERY AS $$
  SELECT to_tsquery('simple', string_agg(
    CASE
      WHEN t.alias = 'asciiword' OR t.alias = 'numword' OR t.alias = 'uint' THEN  p.token || ':*'
      WHEN t.alias = 'blank' THEN ' & '
      ELSE p.token
    END,''))
  FROM
    ts_parse ('default', normalize_text(query)) p
    , ts_token_type('default') t
  WHERE
    p.tokid = t.tokid;
$$ LANGUAGE SQL;
