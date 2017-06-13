CREATE OR REPLACE FUNCTION normalize_text(term TEXT) RETURNS TEXT AS $$
BEGIN
  RETURN lower(translate(trim(regexp_replace(term, '\s+', ' ', 'g')), 'ÁĄÄČĆĎÉĚĘËÍŁŇŃÓÖŘŠŚŤÚŮÜÝŽŻŹáąäčćďéěęëíłňńóöřšśťúůüýžżź.-,;:&+?/', 'aaaccdeeeeilnnoorsstuuuyzzzaaaccdeeeeilnnoorsstuuuyzzz'));
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION address_query(query TEXT) RETURNS TSQUERY AS $$
  SELECT to_tsquery(string_agg(
    CASE
      WHEN t.alias = 'asciiword' OR t.alias = 'numword' THEN p.token || ':*'
      WHEN t.alias = 'blank' THEN ' & '
      ELSE p.token
    END,''))
  FROM
    ts_parse ('default', normalize_text(query)) p
    , ts_token_type ('default') t
  WHERE
    p.tokid = t.tokid;
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION prefix_query(query TEXT) RETURNS TSQUERY AS $$
  SELECT to_tsquery(string_agg(
    CASE
      WHEN t.alias != 'blank' THEN p.token || ':*'
      ELSE ' & '
    END,''))
  FROM
    ts_parse ('default', normalize_text(query)) p
    , ts_token_type ('default') t
  WHERE
    p.tokid = t.tokid;
$$ LANGUAGE SQL;