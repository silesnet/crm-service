CREATE TABLE ap_wireless
(
   id          bigserial NOT NULL,
   name        text      NOT NULL,
   mac         macaddr   NOT NULL,
   interface   text      NOT NULL
);

ALTER TABLE events
   ADD CONSTRAINT ap_wireless_pkey
   PRIMARY KEY (id);

COMMIT;
