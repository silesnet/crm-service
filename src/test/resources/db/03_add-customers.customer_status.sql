-- add customers.status column
ALTER TABLE customers ADD COLUMN customer_status character varying(16) NOT NULL DEFAULT 'DRAFT';

UPDATE customers
SET customer_status = 'ACTIVE'
WHERE is_active;

UPDATE customers
SET customer_status = 'SUSPENDED'
WHERE NOT is_active;
