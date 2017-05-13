ALTER TABLE customers
  ADD COLUMN lexems TSVECTOR;

UPDATE customers
   SET lexems = to_tsvector(normalize_text(name));

CREATE INDEX customers_search_idx ON customers USING gin(lexems);
