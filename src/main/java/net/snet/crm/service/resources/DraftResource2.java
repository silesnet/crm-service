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
import net.snet.crm.domain.shared.data.Data;
import net.snet.crm.domain.shared.data.MapData;
import net.snet.crm.infrastructure.network.access.*;
import net.snet.crm.infrastructure.network.access.action.NoAction;
import net.snet.crm.service.dao.CrmRepository;
import net.snet.crm.service.dao.DraftRepository;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.TransactionCallback;
import org.skife.jdbi.v2.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.*;

import static net.snet.crm.domain.model.draft.Draft.Entity.SERVICES;
import static net.snet.crm.service.utils.Entities.*;
import static net.snet.crm.service.utils.Resources.checkParam;

@Path("/drafts2")
@Produces({"application/json; charset=UTF-8"})
@Consumes(MediaType.APPLICATION_JSON)
public class DraftResource2
{
  private static final Logger logger = LoggerFactory.getLogger(DraftResource2.class);
  private static final Function<Map<String, Object>, String> getLoginValue = getValueOf("login");
  private static final String AUTH_DHCP = "1";
  private static final String AUTH_PPPOE = "2";

  private final CrmRepository crmRepository;
  @Context
  private UriInfo uriInfo;
  private DraftRepository draftRepository;
  private AgreementRepository agreementRepository;
  private NetworkRepository networkRepository;
  private NetworkService networkService;
  private final StateMachine stateMachine;
  private final ActionFactory actionFactory;
  private final DBI dbi;

  public DraftResource2(
      DraftRepository draftRepository,
      CrmRepository crmRepository,
      AgreementRepository agreementRepository,
      NetworkRepository networkRepository,
      NetworkService networkService,
      DBI dbi)
  {
    this.draftRepository = draftRepository;
    this.crmRepository = crmRepository;
    this.agreementRepository = agreementRepository;
    this.networkRepository = networkRepository;
    this.networkService = networkService;
    this.dbi = dbi;
    this.stateMachine = new StateMachine();
    this.actionFactory = new ActionFactory(networkRepository, networkService);
  }

  @GET
  public Response retrieveDraftsByOwner(
      @QueryParam("owner") final Optional<String> ownerParam,
      @QueryParam("entityType") final Optional<String> entityType)
  {
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
    return new Predicate<Map<String, Object>>()
    {
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
  public Response retrieveDraftByType(
      @PathParam("entityType") String entityType,
      @PathParam("entityId") long entityId)
  {
    return Response
        .ok(ImmutableMap.of("drafts",
                            draftRepository.getEntity(entityType, entityId)))
        .build();
  }

  @PUT
  @Path("/{draftId}")
  public Response updateDraft(
      LinkedHashMap<String, Object> body,
      @PathParam("draftId") final long draftId
  )
  {
    logger.debug("PUT /drafts2 '{}'", body);
    final Data data = MapData.of(body).dataOf("drafts");
    final Data original = MapData.of(draftRepository.get(draftId));
    ensureNotImported(original);

    final List<String> messages =
        dbi.inTransaction(new TransactionCallback<List<String>>()
        {
          @Override
          public List<String> inTransaction(Handle handle, TransactionStatus status) throws Exception {
            draftRepository.update(draftId, data, handle);
            final Data draft = draftRepository.get(draftId, handle);

            if (isServiceDraft(original)) {
              final Action action = accessChangeAction(original, draft);
              logger.debug(
                  "performing transition action '{}'",
                  action.getClass().getSimpleName()
              );
              return action.perform(serviceId(original), draft, handle);
            }

            return new ArrayList<>();
          }
        });

    logger.info(
        "updated draft '{}' of '{}/{}'", draftId,
        original.stringOf("entityType"),
        original.stringOf("entityId")
    );
    return Response.ok(
        ImmutableMap.of("messages", messages)).build();
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
      if (AUTH_PPPOE.equals(authentication)) {
        networkRepository.removePppoe(original.entityId());
        logger.info("removed PPPoE for {}", original.entityId());
        kickPppoeOf(original);
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

  private long serviceId(Data draft) {
    return draft.longOf("entityId");
  }

  private boolean isServiceDraft(Data draft) {
    return "services".equals(draft.stringOf("entityType"));
  }


  private void ensureNotImported(Data draft) {
    checkParam(
        !"IMPORTED".equals(draft.stringOf("status")),
        newWebException("trying to update IMPORTED draft '%d'", draft.longOf("id"))
    );
  }

  private WebApplicationException newWebException(String message, Object... args) {
    return new WebApplicationException(
        new IllegalStateException(
            String.format(message, args)
        )
    );
  }

  private Action accessChangeAction(Data original, Data draft)
  {
    if (!"services".equals(original.stringOf("entityType"))) {
      return NoAction.INSTANCE;
    }
    final Access originalAccess = new Access(original);
    final Access access = new Access(draft);
    final Transitions transition = stateMachine.transitionOf(
        originalAccess.state(),
        access.event()
    );
    return actionFactory.actionOf(transition);
  }

  private void kickPppoeOf(final Draft draft) {
    final ValueMap data = valueMapOf(draft.data());
    final String productChannel = data.get("product_channel").toString().toUpperCase();
    if (productChannel.length() > 0) {
      final int interfaceId = data.get("core_router").asIntegerOr(-1);
      if (interfaceId > 0) {
        final ValueMap interfaceData = valueMapOf(networkRepository.findDevice(interfaceId));
        final String login = data.get("auth_a").toString();
        final String master = interfaceData.get("name").toString();
        networkService.kickPppoeUser(master, login);
      }
    }
  }


  private void disableDhcp(int switchId, int port) {
    if (switchId != -1 && port != -1) {
      networkRepository.disableDhcp(switchId, port);
    }
  }

  private void bindDhcp(long serviceId, int switchId, int port) {
    if (switchId != -1 && port != -1) {
      networkRepository.bindDhcp(serviceId, switchId, port);
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
    assert userData != null; // just for intellij not on produce warning
    final String roles = String.valueOf(userData.get("roles"));
    return Splitter.on(',').trimResults().split(roles);
  }

  private String userOperationCountry(final String user) {
    final Map<String, Object> userData = crmRepository.findUserByLogin(user);
    checkParam(userData != null, "unknown user '%s'", user);
    assert userData != null; // just for intellij not on produce warning
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
    return new Predicate<Map<String, Object>>()
    {
      @Override
      public boolean apply(final Map<String, Object> draft) {
        final String submittedDraftOwner = String.valueOf(draft.get("owner"));
        return users.contains(submittedDraftOwner);
      }
    };
  }

  private void filterInPlaceByEntityType(
      final Set<Map<String, Object>> drafts,
      @Nonnull final String entityType)
  {
    final Iterator<Map<String, Object>> draftsIterator = drafts.iterator();
    while (draftsIterator.hasNext()) {
      Map<String, Object> draft = draftsIterator.next();
      if (!entityType.equals(draft.get("entityType"))) {
        draftsIterator.remove();
      }
    }
  }

}
