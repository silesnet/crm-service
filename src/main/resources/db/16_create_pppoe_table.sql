CREATE TABLE pppoe
(
  service_id    BIGINT,
  login         TEXT NOT NULL DEFAULT '',
  password      TEXT NOT NULL DEFAULT '',
  mac           MACADDR DEFAULT NULL,
  mode          TEXT NOT NULL DEFAULT '',
  master        TEXT NOT NULL DEFAULT '',
  interface     TEXT NOT NULL DEFAULT '',
  ip            INET DEFAULT NULL
);
