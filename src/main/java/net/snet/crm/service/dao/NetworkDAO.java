package net.snet.crm.service.dao;

import net.snet.crm.service.bo.Network;
import net.snet.crm.service.mapper.NetworkMapper;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.Iterator;

@RegisterMapper(NetworkMapper.class)
public interface NetworkDAO {

    @SqlQuery("SELECT DISTINCT ON (master) * FROM network AS n1 WHERE EXISTS (SELECT 1 FROM network AS n2 WHERE n1.master = n2.name) ORDER BY master")
    Iterator<Network> allMasters();

    @SqlQuery("SELECT DISTINCT ON (ssid) * FROM network WHERE ssid IS NOT NULL ORDER BY ssid")
    Iterator<Network> allSsids();

    void close();
}
