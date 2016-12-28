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

-- Column id is associated with sequence public.events_id_seq

ALTER TABLE public.events
   ADD CONSTRAINT events_pkey
   PRIMARY KEY (id);

COMMIT;
