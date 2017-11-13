ALTER TABLE services
  ADD COLUMN lexems TSVECTOR;

CREATE INDEX services_search_idx ON services USING gin(lexems);

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
       || c.lexems
       || ad.lexems
       || to_tsvector((a.id % 100000)::text)
       || CASE WHEN p.interface IS NOT NULL THEN to_tsvector(p.interface) ELSE '' END
       || CASE WHEN d.interface IS NOT NULL THEN to_tsvector(d.interface) ELSE '' END
       || CASE WHEN p.location IS NOT NULL THEN to_tsvector(p.location) ELSE '' END
       || CASE WHEN c.phone IS NOT NULL THEN to_tsvector(TRANSLATE(c.phone, ' ', '')) ELSE '' END
       AS lexems
    FROM services AS ss
      INNER JOIN customers AS c ON ss.customer_id = c.id
      INNER JOIN agreements AS a ON ss.id/100 = a.id
      LEFT JOIN pppoe AS p ON ss.id = p.service_id
      LEFT JOIN dhcp_wireless AS d ON ss.id = d.service_id
      LEFT JOIN addresses AS ad ON ss.address_id=ad.address_id
  ) AS service_lexems
  WHERE service_lexems.id=s.id AND s.id = service_id;
END;
$$;


CREATE OR REPLACE FUNCTION calculate_service_lexems_from_customer()
RETURNS TRIGGER
VOLATILE
LANGUAGE plpgsql
AS $$
BEGIN
  EXECUTE 'SELECT calculate_service_lexems(s.id) FROM services s INNER JOIN customers c ON s.customer_id=c.id WHERE c.id = $1' USING NEW.id;
  RETURN NEW;
END
$$;

DROP TRIGGER IF EXISTS calculate_service_lexems_from_customer_trg ON customers;

CREATE TRIGGER calculate_service_lexems_from_customer_trg AFTER INSERT OR UPDATE
  ON customers FOR EACH ROW EXECUTE PROCEDURE calculate_service_lexems_from_customer();


CREATE OR REPLACE FUNCTION calculate_service_lexems_from_pppoe()
RETURNS TRIGGER
VOLATILE
LANGUAGE plpgsql
AS $$
BEGIN
  EXECUTE 'SELECT calculate_service_lexems(s.id) FROM services s WHERE s.id = $1' USING NEW.service_id;
  RETURN NEW;
END
$$;

DROP TRIGGER IF EXISTS calculate_service_lexems_from_pppoe_trg ON pppoe;

CREATE TRIGGER calculate_service_lexems_from_pppoe_trg AFTER INSERT OR UPDATE
  ON pppoe FOR EACH ROW EXECUTE PROCEDURE calculate_service_lexems_from_pppoe();


CREATE OR REPLACE FUNCTION calculate_service_lexems_from_dhcp_wireless()
RETURNS TRIGGER
VOLATILE
LANGUAGE plpgsql
AS $$
BEGIN
  EXECUTE 'SELECT calculate_service_lexems(s.id) FROM services s WHERE s.id = $1' USING NEW.service_id;
  RETURN NEW;
END
$$;

DROP TRIGGER IF EXISTS calculate_service_lexems_from_dhcp_wireless_trg ON dhcp_wireless;

CREATE TRIGGER calculate_service_lexems_from_pppoe_trg AFTER INSERT OR UPDATE
  ON dhcp_wireless FOR EACH ROW EXECUTE PROCEDURE calculate_service_lexems_from_dhcp_wireless();


CREATE OR REPLACE FUNCTION calculate_service_lexems_from_address()
RETURNS TRIGGER
VOLATILE
LANGUAGE plpgsql
AS $$
BEGIN
  EXECUTE 'SELECT calculate_service_lexems(s.id) FROM services s INNER JOIN addresses a ON s.address_id=a.address_id WHERE a.address_id = $1' USING NEW.address_id;
  RETURN NEW;
END
$$;

DROP TRIGGER IF EXISTS calculate_service_lexems_from_address_trg ON addresses;

CREATE TRIGGER calculate_service_lexems_from_address_trg AFTER INSERT OR UPDATE
  ON addresses FOR EACH ROW EXECUTE PROCEDURE calculate_service_lexems_from_address();

-- update service lexems by hand, may take long time ()
-- SELECT calculate_service_lexems(id) FROM services;