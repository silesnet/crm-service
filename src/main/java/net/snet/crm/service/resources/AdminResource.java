package net.snet.crm.service.resources;

import com.google.common.collect.ImmutableMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/status")
public class AdminResource
{

  private final String version;

  public AdminResource(String version) {
    this.version = version;
  }

  @GET
  @Path("/version")
  @Produces({"application/json; charset=UTF-8"})
  public Response version() {
    return Response.ok(ImmutableMap.of("version", version)).build();
  }

}
