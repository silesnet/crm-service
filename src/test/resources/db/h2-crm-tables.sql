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
    old_id bigint
);

CREATE TABLE services_info (
  service_id bigint NOT NULL,
  status character varying(16) NOT NULL DEFAULT 'DRAFT',
  other_info character varying(1024)
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
)