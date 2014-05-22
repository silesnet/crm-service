package net.snet.crm.service.dao;

import net.snet.crm.service.bo.Product;
import net.snet.crm.service.mapper.ProductMapper;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.Iterator;

@RegisterMapper(ProductMapper.class)
public interface ProductDAO {

    @SqlQuery("SELECT * FROM products ORDER BY name")
    Iterator<Product> allProducts();

    void close();
}


