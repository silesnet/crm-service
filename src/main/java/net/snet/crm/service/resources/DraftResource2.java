package net.snet.crm.service.resources;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.snet.crm.service.bo.Draft;
import net.snet.crm.service.dao.CrmRepository;
import net.snet.crm.service.dao.DraftRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static net.snet.crm.service.utils.Entities.*;

@Path("/drafts2")
@Produces({"application/json; charset=UTF-8"})
@Consumes(MediaType.APPLICATION_JSON)
public class DraftResource2 {
  public static final Logger logger = LoggerFactory.getLogger(DraftResource2.class);
  private final CrmRepository crmRepository;

  @Context
  private UriInfo uriInfo;
  private DraftRepository draftRepository;

  public DraftResource2(DraftRepository draftRepository, CrmRepository crmRepository) {
    this.draftRepository = draftRepository;
    this.crmRepository = crmRepository;
  }

  @GET
  public Response retrieveDraftsByOwner(@QueryParam("owner")
                                        final Optional<String> ownerParam) {
    if (!ownerParam.isPresent()) {
      throw new WebApplicationException(
          new IllegalArgumentException("'owner' query parameter not provided"),
          400);
    }
    final String owner = ownerParam.get();
    logger.debug("retrieving drafts by owner '{}'", owner);
    final LinkedHashSet<Map<String, ?>> drafts = Sets.newLinkedHashSet();
    final Map<String, Object> ownerData = crmRepository.findUserByLogin(owner);
    checkNotNull(ownerData, "unknown user '%s'", owner);
    final String roles = ownerData.get("roles").toString();
    for (String role : Splitter.on(',').trimResults().split(roles)) {
      if ("ROLE_TECH_ADMIN".equals(role)) {
        final List<Map<String, Object>> subordinatesData =
            crmRepository.findUserSubordinates(owner);
        final Set<Object> subordinates = Sets.newHashSet();
        for (Map<String, Object> subordinate : subordinatesData) {
          subordinates.add(subordinate.get("login").toString());
        }
        final List<Map<String, Object>> submitted = draftRepository.findDraftsByStatus("SUBMITTED");
        for (Map<String, Object> draft : submitted) {
          final Object submittedDraftOwner = draft.get("owner");
          if (subordinates.contains(submittedDraftOwner) || owner.equals(submittedDraftOwner)) {
            drafts.add(draft);
          }
        }
      }
      if ("ROLE_ACCOUNTING".equals(role)) {
        drafts.addAll(draftRepository.findDraftsByStatus("APPROVED"));
      }
    }
    drafts.addAll(draftRepository.findDraftsByOwnerAndStatus(owner, "DRAFT"));
    return Response.ok(ImmutableMap.of("drafts", drafts)).build();
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
