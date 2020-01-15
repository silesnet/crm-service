select *
from network_nodes_view
where to_tsvector('english',
                  name || ' ' || coalesce(master, '') || ' ' || coalesce(vendor, '') || ' ' || coalesce("linkTo", '') ||
                  ' ' || coalesce(area, '')) @@ to_tsquery('english', 'huawei');
