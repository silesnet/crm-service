-- remove obsolete columns
ALTER TABLE services
  DROP COLUMN replace_id,
  DROP COLUMN old_id;

-- add addresses and places columns
ALTER TABLE services
  ADD COLUMN address_id INTEGER,
  ADD COLUMN place_id INTEGER;

