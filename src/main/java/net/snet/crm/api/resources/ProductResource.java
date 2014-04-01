package net.snet.crm.api.resources;

import com.yammer.dropwizard.jersey.params.LongParam;
import com.yammer.metrics.annotation.Timed;
import net.snet.crm.api.dao.ProductDao;
import net.snet.crm.api.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/products")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProductResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(RegionResource.class);

	private final ProductDao productDao;

	public ProductResource(ProductDao productDao) {
		this.productDao = productDao;
	}

	@GET
    @Produces({"application/json; charset=UTF-8"})
	@Timed(name = "get-requests")
	public Map<String, Object> findAllProducts() {
		final HashMap<String, Object> products = new HashMap<>();
		final List<Product> allProducts = productDao.findAllProducts();
		products.put("products", allProducts);
		return products;
	}

    @GET
    @Path("/{productId}")
    @Produces({"application/json; charset=UTF-8"})
    @Timed(name = "get-requests")
    public Map<String, Object> returnProduct(@PathParam("productId") LongParam productId) {

        final HashMap<String, Object> products = new HashMap<>();
        final List<Product> product = productDao.getProductById(productId.get().intValue());

        if (product.isEmpty()) {
            throw new WebApplicationException(Response.status(404).build());
        }

        products.put("products", product);
        return products;
    }

    @DELETE
    @Path("/{productId}")
    @Timed(name = "get-requests")
    public void deleteProduct(@PathParam("productId") LongParam productId) {

        int retCode = productDao.deleteProductById(productId.get().intValue());

        if (retCode == 0) {
            throw new WebApplicationException(Response.status(404).build());
        }
    }
}
