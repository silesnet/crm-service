create or replace view network_nodes_view as
select id,
       name,
       master,
       area,
       vendor,
       model,
       linkto        as link_to,
       rstp_num_ring,
       'backup/' || id ||
       (case
            when vendor = 'Huawei' then '-current.cfg'
            when vendor = 'TP-Link' then '-Config.cfg'
            when vendor = 'Edge-core' then '-startup1.cfg'
            when vendor = 'H3C' or vendor = 'HP' then '-startup.cfg'
            when vendor = 'Mimosa' then '-mimosa.conf'
            when vendor = 'Siklu' then '-startup-configuration.txt'
            when vendor = 'Ubiquiti' then '-backup.cfg'
            when vendor = 'Mikrotik' then '.backup'
           end)      as backup_path
from network;
