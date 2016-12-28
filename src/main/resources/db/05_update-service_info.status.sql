ALTER TABLE services_info
   ALTER COLUMN status TYPE character varying(16);
ALTER TABLE services_info
   ALTER COLUMN status SET DEFAULT 'DRAFT';
ALTER TABLE services_info
   ALTER COLUMN status SET NOT NULL;