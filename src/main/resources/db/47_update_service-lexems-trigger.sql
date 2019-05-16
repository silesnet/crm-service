select *
from pg_trigger
where not tgisinternal
order by tgname;

drop trigger if exists calculate_service_lexems_from_service_trg ON customers;

CREATE OR REPLACE FUNCTION service_lexems(in BIGINT, out tsvector)
AS
$$
SELECT to_tsvector(s.id::text)
           || coalesce(c.lexems, '')
           || coalesce(ad.lexems, '')
           || to_tsvector((a.id % 100000)::text)
           || coalesce(to_tsvector(p.interface), '')
           || coalesce(to_tsvector(d.interface), '')
           || coalesce(to_tsvector(p.location), '')
           || coalesce(to_tsvector(TRANSLATE(c.phone, ' ', '')), '')
FROM services AS s
         INNER JOIN customers AS c ON s.customer_id = c.id
         INNER JOIN agreements AS a ON s.id / 100 = a.id
         LEFT JOIN pppoe AS p ON s.id = p.service_id
         LEFT JOIN dhcp_wireless AS d ON s.id = d.service_id
         LEFT JOIN addresses AS ad ON s.address_id = ad.address_id
where s.id = $1;
$$ LANGUAGE sql;

CREATE OR REPLACE FUNCTION update_service_lexems()
    RETURNS TRIGGER
    VOLATILE
    LANGUAGE plpgsql
AS
$$
BEGIN
    execute 'update services set lexems = service_lexems($1) where id = $1' using NEW.id;
    return NEW;
END
$$;

CREATE TRIGGER update_service_lexems_trg AFTER INSERT OR UPDATE OF id, address_id, customer_id
    ON services FOR EACH ROW EXECUTE PROCEDURE update_service_lexems();

-- update services set lexems = service_lexems(id);
