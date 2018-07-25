package net.snet.crm.service.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.HandleCallback;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/products")
public class ProductResource {

  private final DBI dbi;

  public ProductResource(DBI dbi) {
    this.dbi = dbi;
  }

  @GET
  @Produces({"application/json; charset=UTF-8"})
  @Timed(name = "get-requests")
  public Response getAllProducts(@QueryParam("country") final String country) {
    return Response.ok(
      ImmutableMap.of(
        "products", fetchProductsByCountry(country)
      )
    ).build();
  }

  private List<Map<String, Object>> fetchProductsByCountry(final String country)
  {
    return dbi.withHandle(new HandleCallback<List<Map<String, Object>>>()
    {
      @Override
      public List<Map<String, Object>> withHandle(Handle handle) throws Exception
      {
        return handle.createQuery(
          "SELECT id, name, price, channel, can_change_price FROM products WHERE country=:country " +
            "AND now() BETWEEN active_from AND active_to ORDER BY position"
         )
        .bind("country", country)
        .list();
      }
    });
  }

}

