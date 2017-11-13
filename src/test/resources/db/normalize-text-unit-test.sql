WITH fixtures(input, expected, comment) AS
(
  VALUES
    ('áąäåčćďđéěęëíľłňńóöôřšśťúůüýžżź', 'aaaaccddeeeeillnnooorsstuuuyzzz', 'lower accents')
  , ('ÁĄÄÅČĆĎĐÉĚĘËÍĽŁŇŃÓÖÔŘŠŚŤÚŮÜÝŽŻŹ', 'aaaaccddeeeeillnnooorsstuuuyzzz', 'upper accents')
  , ('a.,;:!?+-*/\|&()<>{}z=' , 'a z', 'separators')
  , (E'a\tb\rc\nd', 'a b c d', 'tab and new line')
  , (' a ', 'a', 'leading and trailing spaces')
  , ('a  b   c  d e', 'a b c d e', 'double spaces')
  , ('a"''%$@#z', 'az', 'ignored characters')
  , ('0 10 20 09', '0 10 20 09', 'numbers')
  , ('0.1', '0 1', 'decimal numbers')
  , ('(2+3)*5=10', '2 3 5 10', 'mathematic equation')
  , ('123e1f', '123e1f', 'mathematic notation real number') 
),
test AS (
  SELECT
    input,
    normalize_text(input) as normalized,
    expected,
    comment
  FROM fixtures
)
SELECT comment, expected=normalized AS passed, input, normalized FROM test
-- WHERE expected!=normalized
