CREATE INDEX municipalities_search_idx ON municipalities USING gin(lexems);

CREATE INDEX districts_search_idx ON districts USING gin(lexems);

CREATE INDEX streets_search_idx ON streets USING gin(lexems);

CREATE INDEX addresses_search_idx ON addresses USING gin(lexems);
