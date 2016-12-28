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