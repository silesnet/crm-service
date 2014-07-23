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
    variable integer
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
  customer_id bigint
);
