package net.snet.crm.service.resources;

import net.snet.crm.infrastructure.addresses.PlaceRepository;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/places")
@Produces( {"application/json; charset=UTF-8"})
@Consumes(MediaType.APPLICATION_JSON)
public class PlaceResource
{
  final PlaceRepository places;

  public PlaceResource(PlaceRepository places)
  {
    this.places = places;
  }

  @GET
  @Path("/{placeId}")
  public Response findById(@PathParam("serviceId") long placeId)
  {
    return Response.ok(places.findById(placeId)).build();
  }

}
