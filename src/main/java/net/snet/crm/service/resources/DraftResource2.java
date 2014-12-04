package net.snet.crm.service.resources;

import com.google.common.base.Function;
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static net.snet.crm.service.utils.Entities.*;
import static net.snet.crm.service.utils.Resources.checkParam;

@Path("/drafts2")
@Produces({"application/json; charset=UTF-8"})
@Consumes(MediaType.APPLICATION_JSON)
public class DraftResource2 {
  private static final Logger
      logger = LoggerFactory.getLogger(DraftResource2.class);
  private static final Function<Map<String, Object>, String>
      getLoginValue = getValueOf("login");

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
        drafts.addAll(
            FluentIterable.from(draftRepository.findByStatus("SUBMITTED"))
                .filter(ownedByOneOf(userSubordinatesPlusOwner(owner))).toSet());
      }
      if ("ROLE_ACCOUNTING".equals(role)) {
        drafts.addAll(draftRepository.findByStatus("APPROVED"));
      }
    }
    drafts.addAll(draftRepository.findByOwnerAndStatus(owner, "DRAFT"));
    if (entityType.isPresent()) {
      filterInPlaceByEntityType(drafts, entityType.get());
    }
    return Response.ok(ImmutableMap.of("drafts", drafts)).build();
  }

  @POST
  public Response createDraft(LinkedHashMap<String, Object> body) {
    final Optional<Map<String, Object>> draftData = fetchNestedMap("drafts", body);
    checkParam(draftData.isPresent(), "cannot create draft, data not sent");
    logger.debug("creating '{}' draft",
        fetchNested("entityType", draftData.get()).get());
    final long draftId = draftRepository.create(draftData.get());
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
    logger.debug("retrieving entity draft '{}/{}'", entityType, entityId);
    return Response
        .ok(ImmutableMap.of("drafts",
            draftRepository.getEntity(entityType, entityId)))
        .build();
  }

  @PUT
  @Path("/{draftId}")
  public Response updateDraft(LinkedHashMap<String, Object> body,
                              @PathParam("entityId") long draftId) {
    final Optional<Map<String, Object>> draftData = fetchNestedMap("drafts", body);
    checkParam(draftData.isPresent(), "can't update draft, data not sent");
    logger.debug("updating '{}' draft '{}'",
        fetchNested("entityType", draftData.get()).get(), draftId);
    draftRepository.update(draftData.get());
    final Map<String, Object> draft = draftRepository.get(draftId);
    return Response
        .ok(ImmutableMap.of("drafts", draft))
        .build();
  }

  private Iterable<String> userRoles(final String user) {
    final Map<String, Object> ownerData = crmRepository.findUserByLogin(user);
    checkParam(ownerData != null, "unknown user '%s'", user);
    assert ownerData != null; // just for intellij not to produce warning
    final String roles = String.valueOf(ownerData.get("roles"));
    return Splitter.on(',').trimResults().split(roles);
  }

  private Set<String> userSubordinatesPlusOwner(final String owner) {
    return FluentIterable
        .from(crmRepository.findUserSubordinates(owner))
        .transform(getLoginValue)
        .append(owner)
        .toSet();
  }

  private Predicate<Map<String, Object>> ownedByOneOf(final Set<String> users) {
    return new Predicate<Map<String, Object>>() {
      @Override
      public boolean apply(final Map<String, Object> draft) {
        final String submittedDraftOwner = String.valueOf(draft.get("owner"));
        return users.contains(submittedDraftOwner);
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
