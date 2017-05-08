DROP TABLE IF EXISTS municipalities;
CREATE TABLE municipalities
(
  municipality_id SERIAL PRIMARY KEY,
  municipality_fk TEXT NOT NULL CHECK(trim(municipality_fk) != ''),
  name TEXT NOT NULL CHECK (trim(name) != ''),
  country CHAR(2) NOT NULL CHECK(length(country) = 2),
  label TEXT,
  lexems TSVECTOR
);

DROP TABLE IF EXISTS districts;
CREATE TABLE districts
(
  district_id SERIAL PRIMARY KEY,
  district_fk TEXT NOT NULL CHECK(trim(district_fk) != ''),
  municipality_id INTEGER,
  name TEXT NOT NULL CHECK (trim(name) != ''),
  label TEXT,
  lexems TSVECTOR,
  municipality_fk TEXT NOT NULL CHECK(trim(municipality_fk) != '')
);

DROP TABLE IF EXISTS streets;
CREATE TABLE streets
(
  street_id SERIAL PRIMARY KEY,
  street_fk TEXT NOT NULL CHECK(trim(street_fk) != ''),
  municipality_id INTEGER,
  name TEXT NOT NULL CHECK (trim(name) != ''),
  label TEXT,
  lexems TSVECTOR,
  municipality_fk TEXT NOT NULL CHECK(trim(municipality_fk) != '')
);

DROP TABLE IF EXISTS addresses;
CREATE TABLE addresses
(
  address_id SERIAL PRIMARY KEY,
  address_fk TEXT NOT NULL CHECK(trim(address_fk) != ''),
  municipality_id INTEGER,
  postal_code TEXT NOT NULL CHECK(postal_code ~ '^\d+$'),
  district_id INTEGER,
  street_id INTEGER,
  place_id INTEGER,
  descriptive_no TEXT,
  orientation_no TEXT,
  registration_no TEXT,
  label TEXT,
  lexems TSVECTOR,
  municipality_fk TEXT NOT NULL CHECK(trim(municipality_fk) != ''),
  district_fk TEXT,
  street_fk TEXT
);

DROP TABLE IF EXISTS places;
CREATE TABLE places 
(
  place_id   SERIAL PRIMARY KEY,
  address_fk TEXT,
  epsg_code TEXT CHECK(epsg_code ~ '^\d+$'),
  epsg_cord TEXT CHECK(epsg_cord ~ '^-?\d+\.\d+ -?\d+\.\d+$'),
  gps_cord TEXT NOT NULL CHECK(gps_cord ~ '^-?\d+\.\d+ -?\d+\.\d+$')
);
