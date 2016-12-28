ALTER TABLE pppoe ADD COLUMN ip_class TEXT NOT NULL DEFAULT 'static';

UPDATE pppoe
   SET ip_class = 'public-pl'
WHERE ip IS NULL
AND   service_id > 20000000;

UPDATE pppoe
   SET ip_class = 'internal-cz'
WHERE ip IS NULL
AND   service_id < 20000000;
