package net.snet.crm.service.resources;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import net.snet.crm.domain.model.agreement.AgreementRepository;
import net.snet.crm.domain.model.draft.Draft;
import net.snet.crm.domain.model.network.NetworkRepository;
import net.snet.crm.domain.model.network.NetworkService;
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

import static net.snet.crm.domain.model.draft.Draft.Entity.SERVICES;
import static net.snet.crm.service.utils.Entities.*;
import static net.snet.crm.service.utils.Resources.checkParam;

@Path("/drafts2")
@Produces({"application/json; charset=UTF-8"})
@Consumes(MediaType.APPLICATION_JSON)
public class DraftResource2 {
  private static final Logger logger = LoggerFactory.getLogger(DraftResource2.class);
  private static final Function<Map<String, Object>, String> getLoginValue = getValueOf("login");
  private static final String AUTH_DHCP = "1";

  private final CrmRepository crmRepository;
  @Context
  private UriInfo uriInfo;
  private DraftRepository draftRepository;
  private AgreementRepository agreementRepository;
  private NetworkRepository networkRepository;
  private NetworkService networkService;

  public DraftResource2(
      DraftRepository draftRepository,
      CrmRepository crmRepository,
      AgreementRepository agreementRepository,
      NetworkRepository networkRepository,
      NetworkService networkService)
  {
    this.draftRepository = draftRepository;
    this.crmRepository = crmRepository;
    this.agreementRepository = agreementRepository;
    this.networkRepository = networkRepository;
    this.networkService = networkService;
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
                .filter(ownedByOneOf(subordinatesOrSelf(owner))).toSet());
      }
      if ("ROLE_ACCOUNTING".equals(role)) {
        String ownerOperationCountry = userOperationCountry(owner);
        drafts.addAll(
            FluentIterable.from(draftRepository.findByStatus("APPROVED"))
                .filter(draftCountryOf(ownerOperationCountry)).toList());
        drafts.addAll(draftRepository.findByOwnerAndStatus(owner, "SUBMITTED"));
      }
      if ("ROLE_NETWORK_ADMIN".equals(role)) {
        drafts.addAll(FluentIterable.from(draftRepository.findByStatus("DRAFT")).toList());
        drafts.addAll(FluentIterable.from(draftRepository.findByStatus("SUBMITTED")).toList());
      }
    }
    drafts.addAll(draftRepository.findByOwnerAndStatus(owner, "DRAFT"));
    if (entityType.isPresent()) {
      filterInPlaceByEntityType(drafts, entityType.get());
    }
    return Response.ok(ImmutableMap.of("drafts", drafts)).build();
  }

  private Predicate<? super Map<String, Object>> draftCountryOf(final String operationCountry) {
    final String countryPrefix = "CZ".equals(operationCountry) ? "1" : "2";
    return new Predicate<Map<String, Object>>() {
      @Override
      public boolean apply(Map<String, Object> draft) {
        final String spate = "" + draft.get("entitySpate") + "X";
        return spate.startsWith(countryPrefix);
      }
    };
  }

  @POST
  public Response createDraft(LinkedHashMap<String, Object> body) {
    final Optional<Map<String, Object>> draftData = optionalMapOf("drafts", body);
    checkParam(draftData.isPresent(), "cannot create draft, data not sent");
    logger.debug("creating '{}' draft",
        optionalOf("entityType", draftData.get()).get());
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
    return Response
        .ok(ImmutableMap.of("drafts", draftRepository.get(draftId)))
        .build();
  }

  @GET
  @Path("/{entityType}/{entityId}")
  public Response retrieveDraftByType(@PathParam("entityType") String entityType,
                                      @PathParam("entityId") long entityId) {
    return Response
        .ok(ImmutableMap.of("drafts",
            draftRepository.getEntity(entityType, entityId)))
        .build();
  }

  @PUT
  @Path("/{draftId}")
  public Response updateDraft(LinkedHashMap<String, Object> body,
                              @PathParam("draftId") long draftId) {
    logger.debug("PUT /drafts2 '{}'", body);
    final Optional<Map<String, Object>> draftData = optionalMapOf("drafts", body);
    checkParam(draftData.isPresent(), "can't update draft, data not sent");
    logger.debug("updating draft '{}'", draftId);
    final Map<String, Object> originalDraft = draftRepository.get(draftId);
    if ("IMPORTED".equals(originalDraft.get("status"))) {
      throw new WebApplicationException(new IllegalStateException("trying to updated IMPORTED " +
          "draft '" + draftId + "'"));
    }
    draftRepository.update(draftId, draftData.get());
    logger.debug("draft '{}' updated", draftId);
    final Map<String, Object> draft = draftRepository.get(draftId);
    populateDhcp(originalDraft, draft);
    return Response
        .ok(ImmutableMap.of("drafts", draft))
        .build();
  }

  private void populateDhcp(Map<String, Object> originalDraftMap, Map<String, Object> currentDraftMap) {
    final Draft originalDraft = new Draft(originalDraftMap);
    final Draft currentDraft = new Draft(currentDraftMap);
    if (SERVICES.equals(originalDraft.entity()) && SERVICES.equals(currentDraft.entity())) {
      final ValueMap original = valueMapOf(originalDraft.data());
      final ValueMap current = valueMapOf(currentDraft.data());
      final String originalAuth = original.get("auth_type").asStringOr("0");
      final String currentAuth = current.get("auth_type").asStringOr("0");
      if (AUTH_DHCP.equals(originalAuth)) {
        final int originalSwitchId = original.get("auth_a").asIntegerOr(-1);
        final int originalPort = original.get("auth_b").asIntegerOr(-1);
        if (AUTH_DHCP.equals(currentAuth)) {
          final int currentSwitchId = current.get("auth_a").asIntegerOr(-1);
          final int currentPort = current.get("auth_b").asIntegerOr(-1);
          if (originalSwitchId != currentSwitchId || originalPort != currentPort) {
            disableDhcp(originalSwitchId, originalPort);
          }
        }
        else {
          disableDhcp(originalSwitchId, originalPort);
        }
      }
      if (AUTH_DHCP.equals(currentAuth)) {
        final int switchId = current.get("auth_a").asIntegerOr(-1);
        final int port = current.get("auth_b").asIntegerOr(-1);
        enableDhcp(currentDraft.entityId(), switchId, port);
      }
    }
  }

  @DELETE
  @Path("/{draftId}")
  public Response deleteDraft(@PathParam("draftId") long draftId) {
    final Draft original = new Draft(draftRepository.get(draftId));
    if (SERVICES.equals(original.entity())) {
      final ValueMap service = valueMapOf(original.data());
      final String authentication = service.get("auth_type").asStringOr("0");
      if (AUTH_DHCP.equals(authentication)) {
        final int switchId = service.get("auth_a").asIntegerOr(-1);
        final int port = service.get("auth_b").asIntegerOr(-1);
        disableDhcp(switchId, port);
      }
    }
    draftRepository.delete(draftId);
    return Response.noContent().build();
  }

  @POST
  @Path("/{draftId}")
  public Response importServiceDraft(@PathParam("draftId") final long draftId) {
    logger.debug("import service draft '{}'", draftId);
    final Draft serviceDraft = new Draft(draftRepository.get(draftId));
    final Optional<Draft> customerDraft = customerDraftOf(serviceDraft.links());
    final Optional<Draft> agreementDraft = agreementDraftOf(serviceDraft.links());
    agreementRepository.addService(customerDraft, agreementDraft, serviceDraft);
    logger.info("service draft '{}' was imported", draftId);
    return Response.created(uriInfo.getRequestUri()).entity(ImmutableMap.of()).build();
  }

  private void disableDhcp(int switchId, int port) {
    if (switchId != -1 && port != -1) {
      networkRepository.disableDhcp(switchId, port);
      final Object switchName = networkRepository.findDevice(switchId).get("name");
      if (switchName != null) {
        networkService.disableSwitchPort(switchName.toString(), port);
      }
    }
  }

  private void enableDhcp(long serviceId, int switchId, int port) {
    if (switchId != -1 && port != -1) {
      networkRepository.enableDhcp(serviceId, switchId, port);
      final Object switchName = networkRepository.findDevice(switchId).get("name");
      if (switchName != null) {
        networkService.enableSwitchPort(switchName.toString(), port);
      }
    }
  }

  private Optional<Draft> customerDraftOf(final Map<String, String> links) {
    final String customerId = links.get("drafts.customers");
    if (customerId == null) return Optional.absent();
    final Draft draft = new Draft(draftRepository.getEntity("customers", Long.valueOf(customerId)));
    return Optional.of(draft);
  }

  private Optional<Draft> agreementDraftOf(final Map<String, String> links) {
    final String agreementId = links.get("drafts.agreements");
    if (agreementId == null) return Optional.absent();
    return Optional.of(new Draft(draftRepository.getEntity("agreements", Long.valueOf(agreementId))));
  }

  private Iterable<String> userRoles(final String user) {
    final Map<String, Object> userData = crmRepository.findUserByLogin(user);
    checkParam(userData != null, "unknown user '%s'", user);
    assert userData != null; // just for intellij not to produce warning
    final String roles = String.valueOf(userData.get("roles"));
    return Splitter.on(',').trimResults().split(roles);
  }

  private String userOperationCountry(final String user) {
    final Map<String, Object> userData = crmRepository.findUserByLogin(user);
    checkParam(userData != null, "unknown user '%s'", user);
    assert userData != null; // just for intellij not to produce warning
    return String.valueOf(userData.get("operation_country"));
  }

  private Set<String> subordinatesOrSelf(final String owner) {
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
