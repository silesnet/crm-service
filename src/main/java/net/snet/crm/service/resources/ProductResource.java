package net.snet.crm.service.resources;

import com.codahale.metrics.annotation.Timed;
import net.snet.crm.service.bo.Product;
import net.snet.crm.service.dao.ProductDAO;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Path("/products")
public class ProductResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductResource.class);

    private ProductDAO productDAO;

    public ProductResource(DBI dbi) {
        this.productDAO = dbi.onDemand(ProductDAO.class);
    }

    @GET
    @Produces({"application/json; charset=UTF-8"})
    @Timed(name = "get-requests")
    public Map<String, Object> getAllProducts() {
        LOGGER.debug("product called");

        final HashMap<String, Object> productsMap = new HashMap<String, Object>();

        Iterator<Product> products = productDAO.allProducts();

        productsMap.put("products", products);

        return productsMap;
    }
}

