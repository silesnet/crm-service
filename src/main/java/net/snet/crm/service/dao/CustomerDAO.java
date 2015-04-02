package net.snet.crm.service.dao;

import net.snet.crm.service.bo.Customer;
import net.snet.crm.service.bo.CustomerSearch;
import net.snet.crm.service.mapper.CustomerMapper;
import net.snet.crm.service.mapper.CustomerSearchMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.Iterator;

public interface CustomerDAO {

    @SqlQuery("SELECT * FROM customers WHERE id = :id")
    @RegisterMapper(CustomerMapper.class)
    Customer findById(@Bind("id") long id);

    @SqlQuery("SELECT id, name, supplementary_name, street, city, postal_code, country FROM " +
        "customers WHERE lower(translate(name , :fromChars, :toChars)) LIKE :query LIMIT 20")
    @RegisterMapper(CustomerSearchMapper.class)
    Iterator<CustomerSearch> getCustomersByName(@Bind("query") String query,
                                                @Bind("fromChars") String fromChars,
                                                @Bind("toChars") String toChars);

    @SqlQuery("SELECT id, name, supplementary_name, street, city, postal_code, country FROM " +
        "customers WHERE country=:country AND lower(translate(name , :fromChars, :toChars)) LIKE " +
        ":query LIMIT 20")
    @RegisterMapper(CustomerSearchMapper.class)
    Iterator<CustomerSearch> getCustomersByNameAndCountry(@Bind("query") String query,
                                                @Bind("fromChars") String fromChars,
                                                @Bind("toChars") String toChars,
                                                @Bind("country") long countryId);

    void close();
}


