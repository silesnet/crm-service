package net.snet.crm.service.resources;

import net.snet.crm.infrastructure.messaging.MessagingService;
import net.snet.crm.infrastructure.messaging.SmsMessage;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Map;

@Path("/messages")
@Produces({"application/json; charset=UTF-8"})
@Consumes(MediaType.APPLICATION_JSON)
public class MessagingResource {
  private final MessagingService messagingService;

  public MessagingResource(MessagingService messagingService) {
    this.messagingService = messagingService;
  }

  @POST
  @Path("sms")
  public Response sendSms(Map<String, String> request) {
    final SmsMessage sms = new SmsMessage(request.get("number"), request.get("text"));
    messagingService.send(sms);
    return Response.created(URI.create("/messages")).build();
  }
}
