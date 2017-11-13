CREATE OR REPLACE FUNCTION normalize_space(term TEXT)
RETURNS TEXT
IMMUTABLE
LANGUAGE SQL
AS $$
SELECT
  REGEXP_REPLACE(
    TRIM(term),
    '\s+',
    ' ',
    'g'
  );
$$;

CREATE OR REPLACE FUNCTION normalize_characters(term TEXT)
RETURNS TEXT
IMMUTABLE
LANGUAGE SQL
AS $$
SELECT
  REGEXP_REPLACE(
    TRANSLATE(
      LOWER(term),
      'áąäåčćďđéěęëíľłňńóöôřšśťúůüýžżź.,;:!?+-*/=\|&()<>{}' || E'\t\r\n',  -- replace accent letters and separators
      'aaaaccddeeeeillnnooorsstuuuyzzz                       '
    ),
    '[^a-z0-9 ]',                                                          -- ignore everything but lower letters and numbers
    '',
    'g'
  );
$$;

CREATE OR REPLACE FUNCTION normalize_text(term TEXT)
RETURNS TEXT
IMMUTABLE
LANGUAGE SQL
AS $$
SELECT
  normalize_space(
    normalize_characters(term)
  );
$$;

-- use '$1' instead of 'query' for older version of pgSQL
CREATE OR REPLACE FUNCTION address_query(query TEXT)
RETURNS TSQUERY
IMMUTABLE
LANGUAGE SQL
AS $$
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
$$;

-- use '$1' instead of 'query' for older version of pgSQL
CREATE OR REPLACE FUNCTION prefix_query(query TEXT)
RETURNS TSQUERY
IMMUTABLE
LANGUAGE SQL
AS $$
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
$$;
