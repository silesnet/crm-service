package net.snet.crm.service.resources;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import net.snet.crm.service.dao.DraftRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static net.snet.crm.service.utils.Entities.*;

@Path("/drafts2")
@Produces({"application/json; charset=UTF-8"})
@Consumes(MediaType.APPLICATION_JSON)
public class DraftResource2 {
  public static final Logger logger = LoggerFactory.getLogger(DraftResource2.class);

  @Context
  private UriInfo uriInfo;
  private DraftRepository draftRepository;

  public DraftResource2(DraftRepository draftRepository) {
    this.draftRepository = draftRepository;
  }

  @POST
  public Response createDraft(LinkedHashMap<String, Object> body) {
    final Optional<Map<String, Object>> draftData =
        fetchNestedMap("drafts", body);
    checkArgument(draftData.isPresent(), "cannot create draft, data not sent");
    logger.debug("creating '{}' draft",
        fetchNested("entityType", draftData.get()).get());
    final long draftId = draftRepository.createDraft(draftData.get());
    final Map<String, Object> draft = draftRepository.get(draftId);
    return Response
        .created(uriInfo.getRequestUriBuilder().path("{id}").build(draftId))
        .entity(ImmutableMap.of("drafts", draft))
        .build();
  }

  @GET
  @Path("/{draftId}")
  public Response retrieveDraft(@PathParam("draftId") long draftId) {
    logger.debug("retrieving draft '{}'", draftId);
    return Response
        .ok(ImmutableMap.of("drafts", draftRepository.get(draftId)))
        .build();
  }

  @GET
  @Path("/{entityType}/{entityId}")
  public Response retrieveDraftByType(@PathParam("entityType") String entityType,
                                      @PathParam("entityId") long entityId) {
    logger.debug("retrieving draft by type '{}/{}'", entityType, entityId);
    return Response
        .ok(ImmutableMap.of("drafts",
            draftRepository.getByType(entityType, entityId)))
        .build();
  }

}
