CREATE TABLE dhcp_wireless
(
   service_id  bigint    NOT NULL,
   mac         macaddr   UNIQUE,
   interface   text      DEFAULT ''::text NOT NULL,
   master      text      DEFAULT ''::text NOT NULL,
   area        text      DEFAULT ''::text NOT NULL,
   location    text      DEFAULT ''::text NOT NULL,
   ip          inet,
   ip_class    text      DEFAULT 'static'::text NOT NULL
);

ALTER TABLE dhcp_wireless
   ADD CONSTRAINT dhcp_wireless_service_id_key UNIQUE(service_id);

COMMIT;
