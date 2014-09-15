-- add agreements.status column

ALTER TABLE agreements ADD COLUMN status character varying(16) NOT NULL DEFAULT 'DRAFT';

UPDATE agreements
SET status = 'ACTIVE'
WHERE customer_id IS NOT NULL;

UPDATE agreements
SET status = 'AVAILABLE'
WHERE customer_id IS NULL
  OR customer_id = -1;

