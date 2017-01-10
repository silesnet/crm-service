CREATE TABLE ap_wireless
(
   id          bigserial NOT NULL,
   name        text      NOT NULL UNIQUE,
   mac         macaddr   UNIQUE,
   interface   text      NOT NULL
);

ALTER TABLE ap_wireless
   ADD CONSTRAINT ap_wireless_pkey
   PRIMARY KEY (id);

COMMIT;
