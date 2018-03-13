alter table services add column location varchar(32);

-- copy location from draft to services
with service_locations (service_id, location)
as (
    select
        d.entity_id,
        substring(d.data from '"location_flat" *?: *?"([^"]*)",?')
    from
        drafts2 d
    where
        entity_type = 'services'
)
update
    services s
set
    location = l.location
from
    service_locations l
where
    s.id = l.service_id
    and l.location is not null
    and l.location <> ''
;