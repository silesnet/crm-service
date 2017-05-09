package net.snet.crm.service.resources;

import com.google.common.base.Optional;
import net.snet.crm.domain.shared.data.MapData;
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
  public Response findByQuery(
      @QueryParam("q") Optional<String> query,
      @QueryParam("fk") Optional<String> addressFk)
  {
    if (query.isPresent())
    {
      return Response.ok(addresses.findByQuery(query.get())).build();
    }
    if (addressFk.isPresent())
    {
      return Response.ok(addresses.findByFk(addressFk.get())).build();
    }
    return Response.ok(MapData.EMPTY.asMap()).build();
  }

  @GET
  @Path("/{addressId}")
  public Response findById(@PathParam("addressId") long addressId)
  {
    return Response.ok(addresses.findById(addressId)).build();
  }
}
