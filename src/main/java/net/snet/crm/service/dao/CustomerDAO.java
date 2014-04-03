package net.snet.crm.service.dao;

import net.snet.crm.service.bo.CustomerSearch;
import net.snet.crm.service.mapper.CustomerMapper;
import net.snet.crm.service.bo.Customer;
import net.snet.crm.service.mapper.CustomerSearchMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.Iterator;

public interface CustomerDAO {

    @SqlQuery("SELECT * FROM customers WHERE id = :id")
    @RegisterMapper(CustomerMapper.class)
    Customer findById(@Bind("id") long id);

    @SqlQuery("SELECT id, name, supplementary_name FROM customers WHERE lower(translate(name , :fromChars, :toChars)) LIKE :query")
    @RegisterMapper(CustomerSearchMapper.class)
    Iterator<CustomerSearch> getCustomersByName(@Bind("query") String query, @Bind("fromChars") String fromChars, @Bind("toChars") String toChars);

    void close();
}


