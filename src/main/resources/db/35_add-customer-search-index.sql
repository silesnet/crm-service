ALTER TABLE customers
  ADD COLUMN lexems TSVECTOR;

UPDATE customers
   SET lexems = to_tsvector(normalize_text(name));

CREATE INDEX customers_search_idx ON customers USING gin(lexems);

CREATE OR REPLACE FUNCTION calculate_customer_lexems () RETURNS TRIGGER AS '
BEGIN
  NEW.lexems = to_tsvector(normalize_text(NEW.name));
  RETURN NEW;
END;
' LANGUAGE plpgsql;

CREATE TRIGGER calculate_customer_lexems_trg BEFORE INSERT OR UPDATE
  ON customers FOR EACH ROW EXECUTE PROCEDURE calculate_customer_lexems ();