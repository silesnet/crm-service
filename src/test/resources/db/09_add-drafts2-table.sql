CREATE TABLE drafts2
(
  id serial NOT NULL,
  user_login character varying(50) NOT NULL,
  entity_type character varying(20) NOT NULL,
  entity_spate character varying(20) NOT NULL DEFAULT '',
  entity_id bigint NOT NULL,
  entity_name character varying(50) NOT NULL,
  status character varying(16) NOT NULL DEFAULT 'DRAFT',
  data character varying(5000) NOT NULL DEFAULT '{}'
);