COPY municipalities(municipality_fk, name, country)
  FROM '{{input_folder}}/municipalities.csv'
  WITH csv header delimiter '|'
;

COPY districts(district_fk, municipality_fk, name)
  FROM '{{input_folder}}/districts.csv'
  WITH csv header delimiter '|'
;

COPY streets(street_fk, municipality_fk, name)
  FROM '{{input_folder}}/streets.csv'
  WITH csv header delimiter '|'
;

COPY addresses(address_fk, municipality_fk, postal_code, district_fk, street_fk, descriptive_no, orientation_no, registration_no)
  FROM '{{input_folder}}/addresses.csv'
  WITH csv header delimiter '|'
;

COPY places(address_fk, epsg_code, epsg_cord, gps_cord)
  FROM '{{input_folder}}/places.csv'
  WITH csv header delimiter '|'
;
