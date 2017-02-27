package net.snet.crm.service.resources;

import net.snet.crm.infrastructure.addresses.AddressRepository;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/addresses")
@Produces( {"application/json; charset=UTF-8"})
@Consumes(MediaType.APPLICATION_JSON)
public class AddressResource
{
  private final AddressRepository addresses;

  public AddressResource(AddressRepository addresses)
  {
    this.addresses = addresses;
  }

  @GET
  public Response findByQuery(@QueryParam("q") String query)
  {
    return Response.ok(addresses.findByQuery(query)).build();
  }
}
