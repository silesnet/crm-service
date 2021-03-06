CREATE TABLE customers (
    id bigint NOT NULL,
    history_id bigint NOT NULL,
    public_id character varying(20) NOT NULL,
    name character varying(80) NOT NULL,
    supplementary_name character varying(40),
    street character varying(40),
    city character varying(40),
    postal_code character varying(10),
    country integer,
    email character varying(50),
    dic character varying(20),
    contract_no character varying(50),
    connection_spot character varying(100),
    inserted_on timestamp NOT NULL,
    frequency integer,
    lastly_billed timestamp ,
    is_billed_after boolean,
    deliver_by_email boolean,
    deliver_copy_email character varying(100),
    deliver_by_mail boolean,
    is_auto_billing boolean,
    info character varying(150),
    contact_name character varying(50),
    phone character varying(60),
    is_active boolean,
    status integer,
    shire_id bigint,
    format integer,
    deliver_signed boolean,
    symbol character varying(20),
    updated timestamp,
    synchronized timestamp,
    account_no character varying(26),
    bank_no character varying(4),
    variable integer,
    customer_status character varying(16) NOT NULL DEFAULT 'DRAFT'
);

CREATE TABLE audit_items (
  id bigint NOT NULL,
  history_id bigint NOT NULL,
  history_type_label_id bigint,
  user_id bigint,
  time_stamp timestamp NOT NULL,
  field_name character varying(255) NOT NULL,
  old_value character varying(255),
  new_value character varying(255)
);

CREATE TABLE agreements (
  id bigint NOT NULL,
  country character varying(2) NOT NULL,
  customer_id bigint,
  status character varying(16) NOT NULL DEFAULT 'DRAFT'
);

CREATE TABLE services (
    id bigint NOT NULL,
    customer_id bigint,
    period_from timestamp NOT NULL,
    period_to timestamp,
    name character varying(70) NOT NULL,
    price integer NOT NULL,
    frequency integer,
    download integer,
    upload integer,
    is_aggregated boolean,
    info character varying(150),
    replace_id bigint,
    additionalname character varying(50),
    bps character(1),
    old_id bigint,
    status TEXT NOT NULL DEFAULT 'ACTIVE',
    data TEXT NOT NULL DEFAULT '{}',
    address_id INTEGER,
    place_id INTEGER,
    location VARCHAR(32),
    dph boolean,
    product_id integer
);

CREATE TABLE connections (
  service_id bigint NOT NULL,
  auth_type character varying(10),
  auth_name character varying(32),
  auth_value character varying(32),
  downlink smallint,
  uplink smallint,
  area character varying(32),
  is_public_ip boolean,
  ip character varying(15),
  master_router character varying(32),
  ssid character varying(32),
  sa_mac character varying(30),
  other_info character varying(1024)
);

CREATE TABLE drafts
(
  id serial NOT NULL,
  type character varying(20),
  user_id character varying(50),
  data character varying(5000),
  status character varying(16) NOT NULL DEFAULT 'DRAFT'
);

CREATE TABLE drafts2
(
  id serial NOT NULL,
  entity_type character varying(20) NOT NULL,
  entity_spate character varying(20) NOT NULL DEFAULT '',
  entity_id bigint NOT NULL,
  entity_name character varying(50) NOT NULL,
  status character varying(16) NOT NULL DEFAULT 'DRAFT',
  owner character varying(50) NOT NULL,
  data character varying(5000) NOT NULL DEFAULT '{}'
);

CREATE TABLE draft_links
(
  draft_id bigint NOT NULL,
  entity character varying(40) NOT NULL,
  entity_id bigint NOT NULL
);

CREATE TABLE users
(
  id bigint NOT NULL,
  login character varying(255) NOT NULL,
  passwd character varying(255),
  name character varying(255) NOT NULL,
  roles character varying(255),
  key character varying(255),
  reports_to bigint NOT NULL DEFAULT 0,
  operation_country character(2) NOT NULL DEFAULT 'CZ',
  full_name character varying(255) NOT NULL DEFAULT ''
);

CREATE TABLE network (
    id integer NOT NULL,
    name character varying(50) NOT NULL,
    info character varying(500),
    active boolean,
    type integer,
    mode integer,
    wds boolean,
    frequency integer,
    vendor character varying(50),
    azimuth character varying(50),
    ssid character varying(50),
    auth character varying(50),
    polarization integer,
    country integer,
    norm character varying(50),
    area character varying(50),
    aggregation boolean,
    width character varying(50),
    antenna character varying(50),
    power character varying(50),
    master character varying(50),
    linkto character varying(50),
    hardware character varying(50),
    tdma boolean,
    latitude character varying(50),
    longitude character varying(50),
    r_updatetime timestamp,
    r_firmware character varying(50),
    r_platform character varying(50),
    r_signal character varying(50),
    r_ccq character varying(50),
    r_txrate character varying(50),
    r_rxrate character varying(50),
    r_quality character varying(50),
    r_capacity character varying(50),
    r_lanspeed character varying(50),
    r_txpower character varying(50),
    monitoring character varying(50),
    traceroute text,
    ping character varying(50),
    r_frequency integer
);

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
  a_town      TEXT NOT NULL DEFAULT '�esk� T��n',
  a_postal_code TEXT NOT NULL DEFAULT '73701',
  a_country   TEXT NOT NULL DEFAULT 'CZ',
  a_location2 TEXT NOT NULL DEFAULT ''
);

CREATE TABLE pppoe
(
  service_id    BIGINT,
  login         TEXT NOT NULL DEFAULT '',
  password      TEXT NOT NULL DEFAULT '',
  mac           TEXT DEFAULT NULL,
  mode          TEXT NOT NULL DEFAULT '',
  master        TEXT NOT NULL DEFAULT '',
  interface     TEXT NOT NULL DEFAULT '',
  ip            TEXT DEFAULT NULL,
  ip_class      TEXT NOT NULL DEFAULT 'static',
  area          TEXT NOT NULL DEFAULT ''
);

CREATE TABLE radlogip
(
   username  varchar(40),
   address   varchar(40),
   date      timestamp
);


CREATE VIEW service_connections
(
  service_id,
  name,
  downlink,
  uplink,
  status
)
AS

SELECT s.id AS service_id,
       s.name,
       s.download AS downlink,
       s.upload AS uplink,
       CASE
           WHEN NOT c.is_active THEN 'SUSPENDED'
           WHEN s.status != 'INHERIT_FROM_CUSTOMER' THEN s.status
           WHEN CURRENT_TIMESTAMP() < s.period_from OR (s.period_to IS NOT NULL AND s.period_to < CURRENT_TIMESTAMP()) THEN 'SUSPENDED'
           WHEN c.status IN (10, 30, 40, 50) THEN 'ACTIVE'
           WHEN c.status = 20 AND CURRENT_TIMESTAMP() <= c.lastly_billed THEN 'ACTIVE'
           WHEN c.status = 20 THEN 'SUSPENDED'
           ELSE 'DEBTOR'
       END AS status
FROM services AS s
LEFT JOIN customers AS c ON s.customer_id = c.id
WHERE s.download IS NOT NULL
  AND s.upload IS NOT NULL

UNION

SELECT entity_id AS service_id,
       '' AS name,
       100 AS downlink,
       100 AS uplink,
       'ACTIVE' AS status
FROM drafts2
WHERE entity_type = 'services'
  AND status != 'IMPORTED'

ORDER BY service_id;


CREATE VIEW service_drafts
(
    agreement_id,
    agreement,
    customer_id,
    customer_name,
    street,
    city,
    customer_info,
    service_id,
    service_name,
    service_download,
    service_upload,
    service_price,
    service_info
)
AS

SELECT CASE WHEN a.id IS NOT NULL THEN a.id ELSE da.entity_id END AS agreement_id,
       CASE WHEN a.id IS NOT NULL THEN a.id % 100000 ELSE da.entity_id % 100000 END AS agreement,
       CASE WHEN c.id IS NOT NULL THEN c.id ELSE dc.entity_id END AS customer_id,
       CASE WHEN c.id IS NOT NULL THEN c.name ELSE dc.entity_name END AS customer_name,
       '' AS street,
       '' AS city,
       c.info AS customer_info,
       d.entity_id AS service_id,
       '' AS service_name,
       100 AS service_download,
       100 AS service_upload,
       200 AS service_price,
       '' AS service_info
FROM drafts2 AS d
  LEFT JOIN draft_links AS la
         ON la.draft_id = d.id
        AND la.entity = 'agreements'
  LEFT JOIN draft_links AS lda
         ON lda.draft_id = d.id
        AND lda.entity = 'drafts.agreements'
  LEFT JOIN draft_links AS lc
         ON lc.draft_id = d.id
        AND lc.entity = 'customers'
  LEFT JOIN draft_links AS ldc
         ON ldc.draft_id = d.id
        AND ldc.entity = 'drafts.customers'
  LEFT JOIN agreements AS a ON la.entity_id = a.id
  LEFT JOIN drafts2 AS da ON lda.entity_id = da.entity_id
  LEFT JOIN customers AS c ON lc.entity_id = c.id
  LEFT JOIN drafts2 AS dc ON ldc.entity_id = dc.entity_id
WHERE d.entity_type = 'services'
AND   d.status != 'IMPORTED'
ORDER BY d.id
;

CREATE TABLE products (
    id integer NOT NULL,
    name character varying(100),
    downlink integer,
    uplink integer,
    price integer,
    channel character varying(100),
    is_dedicated boolean,
    priority integer,
    country character(2) NOT NULL DEFAULT 'CZ'
);

CREATE TABLE commands
(
   id           bigserial     NOT NULL,
   command      varchar(50)   NOT NULL,
   entity       varchar(30),
   entity_id    bigint,
   data         varchar,
   status       varchar(20)   NOT NULL,
   inserted_on  timestamp     NOT NULL,
   started_on   timestamp,
   finished_on  timestamp
);

CREATE TABLE events
(
   id           bigserial     NOT NULL,
   event        varchar(50)   NOT NULL,
   entity       varchar(30),
   entity_id    bigint,
   data         varchar,
   command_id   bigint,
   happened_on  timestamp     NOT NULL
);

CREATE SEQUENCE audit_item_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 425462
  CACHE 1;

CREATE TABLE municipalities
(
  municipality_id SERIAL PRIMARY KEY,
  municipality_fk TEXT NOT NULL CHECK(trim(municipality_fk) != ''),
  name TEXT NOT NULL CHECK (trim(name) != ''),
  country CHAR(2) NOT NULL CHECK(length(country) = 2),
  label TEXT,
  lexems TEXT
);

CREATE TABLE districts
(
  district_id SERIAL PRIMARY KEY,
  district_fk TEXT NOT NULL CHECK(trim(district_fk) != ''),
  municipality_id INTEGER,
  name TEXT NOT NULL CHECK (trim(name) != ''),
  label TEXT,
  lexems TEXT,
  municipality_fk TEXT NOT NULL CHECK(trim(municipality_fk) != '')
);

CREATE TABLE streets
(
  street_id SERIAL PRIMARY KEY,
  street_fk TEXT NOT NULL CHECK(trim(street_fk) != ''),
  municipality_id INTEGER,
  name TEXT NOT NULL CHECK (trim(name) != ''),
  label TEXT,
  lexems TEXT,
  municipality_fk TEXT NOT NULL CHECK(trim(municipality_fk) != '')
);

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
  lexems TEXT,
  municipality_fk TEXT NOT NULL CHECK(trim(municipality_fk) != ''),
  district_fk TEXT,
  street_fk TEXT
);

CREATE TABLE places
(
  place_id   SERIAL PRIMARY KEY,
  address_fk TEXT,
  epsg_code TEXT CHECK(epsg_code ~ '^\d+$'),
  epsg_cord TEXT CHECK(epsg_cord ~ '^-?\d+\.\d+ -?\d+\.\d+$'),
  gps_cord TEXT NOT NULL CHECK(gps_cord ~ '^-?\d+\.\d+ -?\d+\.\d+$')
);
