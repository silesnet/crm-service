ALTER TABLE users ADD COLUMN reports_to bigint NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN operation_country character(2) NOT NULL DEFAULT 'CZ';
