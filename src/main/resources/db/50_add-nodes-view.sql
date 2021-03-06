create or replace view query.nodes as
select id,
       name,
       master,
       area,
       vendor,
       model,
       linkto      as link_to,
       (case
            when country = 10 then 'CZ'
            when country = 20 then 'PL'
           end)    as country,
       r_frequency as frequency
from public.network;
