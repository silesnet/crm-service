CREATE TABLE radius
(
   id         integer        NOT NULL,
   username   varchar(40),
   password   varchar(40),
   mode       integer        NOT NULL,
   master     varchar(40),
   mac        varchar(40),
   interface  varchar(40),
   rate       varchar(40),
   address    varchar(40),
   status     integer        NOT NULL
);

ALTER TABLE radius
   ADD CONSTRAINT radius_pkey
   PRIMARY KEY (id);

ALTER TABLE radius
   ADD CONSTRAINT radius_username_key UNIQUE (username);
