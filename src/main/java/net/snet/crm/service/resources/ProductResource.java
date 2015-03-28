package net.snet.crm.service.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import net.snet.crm.service.bo.Product;
import net.snet.crm.service.dao.ProductDAO;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.*;

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
  public Map<String, Object> getAllProducts(@QueryParam("country") final Optional<String> country) {
    LOGGER.debug("product called");
    final HashMap<String, Object> productsMap = new HashMap<String, Object>();
    Iterator<Product> productIterator = productDAO.allProducts();
    List<Product> products = new ArrayList<Product>();
    if (country.isPresent()) {
      Iterators.addAll(products, Iterators.filter(productIterator, new Predicate<Product>() {
        @Override
        public boolean apply(Product product) {
          return country.get().equals(product.getCountry());
        }
      }));
    } else {
      Iterators.addAll(products, productIterator);
    }
    productsMap.put("products", products);
    return productsMap;
  }
}

