package net.snet.crm.service.dao;

import net.snet.crm.service.bo.Service;
import net.snet.crm.service.mapper.ServiceMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.Iterator;

@RegisterMapper(ServiceMapper.class)
public interface ServiceDAO {

    @SqlQuery("SELECT id FROM services WHERE customer_id = :customer_id")
    Iterator<Service> findContractsByCustomerId(@Bind("customer_id") long customer_id);

    void close();
}


