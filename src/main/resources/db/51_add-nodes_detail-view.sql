create or replace view query.nodes_detail as
with recursive
    links_path as (
        select distinct linkto as name, '/' || linkto::text as path
        from public.network n
        where not exists(select name from public.network where name = n.linkto)
        union
        select n.name, l.path || '/' || n.name
        from public.network n
                 inner join links_path l on n.linkto = l.name
    ),
    links as (
        select name, left(path, - (length(name) + 1)) as path
        from links_path
    )
select n.id,
       (case
            when n.country = 10 then 'CZ'
            when n.country = 20 then 'PL'
           end)                                                              as country,
       n.name,
       (case
            when n.type = 10 then 'OTHER'
            when n.type = 20 then 'ROUTER'
            when n.type = 30 and n.mode is null then 'BRIDGE'
            when n.type = 30 and n.mode = 10 then 'BRIDGE-AP'
            when n.type = 30 and n.mode = 20 then 'BRIDGE-BR'
            when n.type = 30 and n.mode = 30 then 'BRIDGE-STATION'
            when n.type = 40 then 'SWITCH'
            when n.type = 50 then 'CONVERTER'
           end)                                                              as type,
       n.master,
       n.linkto                                                              as link_to,
       n.area,
       n.vendor,
       n.model,
       n.info,
       upper(n.monitoring)                                                   as monitoring,
       l.path,
       n.ping,  -- no edit
       (n.type = 30 and n.mode is not null and (n.mode = 10 or n.mode = 20)) as is_wireless,
       (case
            when n.polarization = 10 then 'HORIZONTAL'
            when n.polarization = 20 then 'VERTICAL'
            when n.polarization = 30 then 'DUAL'
           end)                                                              as polarization,
       n.width,
       n.norm,
       n.tdma,
       n.aggregation,
       n.ssid,
       n.frequency,
       n.power,
       n.antenna,
       n.wds,
       (case
            when n.auth = '10' then 'NONE'
            when n.auth = '20' then 'BOTH'
            when n.auth = '30' then 'MAC ACL'
            when n.auth = '40' then 'RADIUS'
           end)                                                              as authentication,
       n.azimuth,
       n.active -- wifi end
from public.network n
         inner join links l on l.name = n.name
