CREATE OR REPLACE FUNCTION alphanumeric(term TEXT)
RETURNS TEXT
IMMUTABLE
LANGUAGE SQL
AS $$
SELECT
  REGEXP_REPLACE(
    TRANSLATE(
      LOWER(term),
      'áąäåčćďđéěęëíľłňńóöôřšśťúůüýžżź',  -- replace accent letters and separators
      'aaaaccddeeeeillnnooorsstuuuyzzz'
    ),
    '[^a-z0-9]',                         -- ignore everything but lower letters and numbers
    '',
    'g'
  );
$$;

CREATE OR REPLACE FUNCTION calculate_service_lexems(service_id BIGINT)
RETURNS void
VOLATILE
LANGUAGE plpgsql
AS $$
BEGIN
  UPDATE services AS s
  SET lexems = service_lexems.lexems
  FROM (
    SELECT
       ss.id
       , to_tsvector(ss.id::text)
       || COALESCE(c.lexems, '')
       || COALESCE(ad.lexems, '')
       || to_tsvector(((ss.id / 100) % 100000)::text)
       || to_tsvector(alphanumeric(COALESCE(p.interface, '')))
       || to_tsvector(alphanumeric(COALESCE(d.interface, '')))
       || to_tsvector(alphanumeric(COALESCE(p.mac::text, '')))
       || to_tsvector(alphanumeric(COALESCE(d.mac::text, '')))
       || to_tsvector(alphanumeric(COALESCE(p.location, '')))
       || to_tsvector(regexp_replace(phone, '[^0-9,]', '', 'g'))
       AS lexems
    FROM services AS ss
      INNER JOIN customers AS c ON ss.customer_id = c.id
      LEFT JOIN pppoe AS p ON ss.id = p.service_id
      LEFT JOIN dhcp_wireless AS d ON ss.id = d.service_id
      LEFT JOIN addresses AS ad ON ss.address_id=ad.address_id
  ) AS service_lexems
  WHERE service_lexems.id=s.id AND s.id = service_id;
END;
$$;