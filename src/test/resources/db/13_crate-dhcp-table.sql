CREATE TABLE dhcp
(
  service_id    BIGINT,
  network_id    INT NOT NULL,
  port          SMALLINT NOT NULL,
  location_id   INT,
  ip            TEXT NOT NULL DEFAULT 'AUTO',
  t_switch      TEXT NOT NULL DEFAULT '',
  a_street      TEXT,
  a_descriptive_number TEXT,
  a_orientation_number TEXT,
  a_flat      TEXT,
  a_town      TEXT NOT NULL DEFAULT 'Èeský Tìšín',
  a_postal_code TEXT NOT NULL DEFAULT '73701',
  a_country   TEXT NOT NULL DEFAULT 'CZ',
  a_location2 TEXT NOT NULL DEFAULT ''
);
