package net.snet.crm.service.resources;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import net.snet.crm.domain.model.agreement.Agreement;
import net.snet.crm.domain.model.agreement.AgreementRepository;
import net.snet.crm.domain.model.agreement.Customer;
import net.snet.crm.domain.model.agreement.Service;
import net.snet.crm.domain.model.draft.Draft;
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
  private AgreementRepository agreementRepository;

  public DraftResource2(DraftRepository draftRepository, CrmRepository crmRepository,
                        AgreementRepository agreementRepository) {
    this.draftRepository = draftRepository;
    this.crmRepository = crmRepository;
    this.agreementRepository = agreementRepository;
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
                              @PathParam("draftId") long draftId) {
    logger.debug("PUT /drafts2 '{}'", body);
    final Optional<Map<String, Object>> draftData = optionalMapOf("drafts", body);
    checkParam(draftData.isPresent(), "can't update draft, data not sent");
    logger.debug("updating draft '{}'", draftId);
    final Map<String, Object> originalDraft = draftRepository.get(draftId);
    draftRepository.update(draftId, draftData.get());
    logger.debug("draft '{}' updated", draftId);
    final Map<String, Object> draft = draftRepository.get(draftId);
    if (isDraftToImport(originalDraft, draft)) {
      importDraft(draft);
    }
    return Response
        .ok(ImmutableMap.of("drafts", draft))
        .build();
  }

  private boolean isDraftToImport(Map<String, Object> original, Map<String, Object> current) {
    final String originalStatus = valueOf("status", original, String.class);
    final String currentStatus = valueOf("status", current, String.class);
    return
        !originalStatus.equals(currentStatus)
            && "IMPORTED".equals(currentStatus);
  }

  private void importDraft(Map<String, Object> draft) {
    final long draftId = valueOf("id", draft, Long.class);
    final String entityType = valueOf("entityType", draft, String.class);
    final long entityId = valueOf("entityId", draft, Long.class);
    if ("customers".equals(entityType)) {
      logger.info("importing draft '{}' into 'customers/{}'...", draftId, entityId);
      final Customer customer = new Customer(new Draft(draft));
      final Customer addedCustomer = agreementRepository.addCustomer(customer);
      logger.info("added new customer '{}'", addedCustomer.id().value());
    } else if ("agreements".equals(entityType)) {
      logger.info("importing draft '{}' into 'agreements/{}'...", draftId, entityId);
      final Agreement agreement = new Agreement(new Draft(draft));
      final Agreement addedAgreement = agreementRepository.add(agreement);
      logger.info("added new agreement '{}'", addedAgreement.id().value());
    } else if ("services".equals(entityType)) {
      logger.info("importing draft '{}' into 'services/{}'...", draftId, entityId);
      final Service service = new Service(new Draft(draft));
      final Service addedService = agreementRepository.addService(service);
      logger.info("added new service '{}'", addedService.id().value());
    } else {
      logger.info("can't import draft '{}' unknown entity type '{}/{}'", draftId, entityType, entityId);
    }
  }

  @DELETE
  @Path("/{draftId}")
  public Response deleteDraft(@PathParam("draftId") long draftId) {
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
