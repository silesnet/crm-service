package net.snet.crm.service.dao;

import net.snet.crm.service.bo.Router;
import net.snet.crm.service.mapper.RouterMapper;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.Iterator;

@RegisterMapper(RouterMapper.class)
public interface RouterDAO {

    @SqlQuery("SELECT * FROM core_routers")
    Iterator<Router> allRouters();

    void close();
}


