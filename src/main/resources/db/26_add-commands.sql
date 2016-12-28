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

-- Column id is associated with sequence public.commands_id_seq

ALTER TABLE public.commands
   ADD CONSTRAINT commands_pkey
   PRIMARY KEY (id);

COMMIT;
