package net.snet.crm.service.resources;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import net.snet.crm.service.dao.CrmRepository;
import net.snet.crm.service.dao.DraftRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static net.snet.crm.service.utils.Entities.*;
import static net.snet.crm.service.utils.Resources.*;

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
  public Response retrieveDraftsByOwner(
      @QueryParam("owner") final Optional<String> ownerParam,
      @QueryParam("entityType") final Optional<String> entityType) {
    checkParam(ownerParam.isPresent(), "'owner' query parameter is mandatory");
    final String owner = ownerParam.get();
    logger.debug("retrieving drafts by owner '{}'", owner);
    final Set<Map<String, Object>> drafts = Sets.newLinkedHashSet();
    for (String role : userRoles(owner)) {
      if ("ROLE_TECH_ADMIN".equals(role)) {
        final Set<String> subordinatesPlusOwner = userSubordinatesPlusOwner(owner);
        final List<Map<String, Object>> submittedDrafts =
            draftRepository.findDraftsByStatus("SUBMITTED");
        drafts.addAll(
            FluentIterable.from(submittedDrafts)
                .filter(ownedByOneOf(subordinatesPlusOwner)).toSet());
      }
      if ("ROLE_ACCOUNTING".equals(role)) {
        drafts.addAll(draftRepository.findDraftsByStatus("APPROVED"));
      }
    }
    drafts.addAll(draftRepository.findDraftsByOwnerAndStatus(owner, "DRAFT"));
    if (entityType.isPresent()) {
      filterInPlaceByEntityType(drafts, entityType.get());
    }
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

  private Iterable<String> userRoles(final String user) {
    final Map<String, Object> ownerData = crmRepository.findUserByLogin(user);
    checkParam(ownerData != null, "unknown user '%s'", user);
    final String roles = String.valueOf(ownerData.get("roles"));
    return Splitter.on(',').trimResults().split(roles);
  }

  private Set<String> userSubordinatesPlusOwner(final String owner) {
    final List<Map<String, Object>> subordinatesData =
        crmRepository.findUserSubordinates(owner);
    final Set<String> subordinates = Sets.newHashSet();
    for (Map<String, Object> subordinate : subordinatesData) {
      subordinates.add(String.valueOf(subordinate.get("login")));
    }
    subordinates.add(owner);
    return subordinates;
  }

  private Predicate<Map<String, Object>> ownedByOneOf(final Set<String> subordinates) {
    return new Predicate<Map<String, Object>>() {
      @Override
      public boolean apply(final Map<String, Object> draft) {
        final String submittedDraftOwner = String.valueOf(draft.get("owner"));
        return subordinates.contains(submittedDraftOwner);
      }
    };
  }

  private void filterInPlaceByEntityType(final Set<Map<String, Object>> drafts,
                                         @Nonnull final String entityType) {
    final Iterator<Map<String, Object>> draftsIterator = drafts.iterator();
    while (draftsIterator.hasNext()) {
      Map<String, Object> draft = draftsIterator.next();
      if (!entityType.equals(draft.get("entityType"))) {
        draftsIterator.remove();
      }
    }
  }

}
