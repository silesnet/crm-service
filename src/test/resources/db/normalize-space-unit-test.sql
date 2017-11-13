WITH fixtures(input, expected, comment) AS
(
  VALUES
    ('a  b   c    d' , 'a b c d', 'multiple spaces')
  , (E'a\tb\rc\nd \r\n e', 'a b c d e', 'tab and new line')
  , (' a ', 'a', 'leading and trailing spaces')
),
test AS (
  SELECT
    input,
    regexp_replace(trim(input), '\s+', ' ', 'g') AS normalized,
    expected,
    comment
  FROM fixtures
)
SELECT comment, expected=normalized AS passed, input, normalized FROM test
-- WHERE expected!=normalized
