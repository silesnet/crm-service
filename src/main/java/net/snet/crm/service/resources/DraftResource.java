package net.snet.crm.service.resources;

import com.yammer.metrics.annotation.Timed;
import net.snet.crm.service.bo.Draft;
import net.snet.crm.service.dao.DraftDAO;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Path("/drafts")
public class DraftResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DraftResource.class);

    private DraftDAO draftDAO;

    public DraftResource(DBI dbi) {
        this.draftDAO = dbi.onDemand(DraftDAO.class);
    }

    @GET
    @Produces({"application/json; charset=UTF-8"})
    @Timed(name = "get-requests")
    public Map<String, Object> getDraftsByUserId(@QueryParam("user_id") String userId) {
        LOGGER.debug("drafts called");

        final HashMap<String, Object> draftsMap = new HashMap<String, Object>();

        Iterator<Draft> drafts = draftDAO.findDraftsByUserId(userId);

        draftsMap.put("drafts", drafts);

        return draftsMap;
    }

    @GET
    @Path("/{draftId}")
    @Produces({"application/json; charset=UTF-8"})
    @Timed(name = "get-requests")
    public Draft getDraftById(@PathParam("draftId") long draftId) {
        LOGGER.debug("drafts called");
        return draftDAO.findDraftById(draftId);
    }

    @POST
    @Timed(name = "post-requests")
    public String insertDraft(@QueryParam("user_id") String userId, String body) {
        LOGGER.debug("drafts called");
        return draftDAO.insertDraft(new Draft("service", userId, body)).toString();
    }

    @PUT
    @Path("/{draftId}")
    @Timed(name = "put-requests")
    public Response updateDraft(@PathParam("draftId") Integer draftId, String body) {
        LOGGER.debug("drafts called");

        draftDAO.updateDraft(body, draftId);

        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
    @Timed(name = "delete-requests")
    public Response deleteDraft(@PathParam("id") long id) {
        LOGGER.debug("drafts called");
        if (draftDAO.findDraftById(id) != null) {
            draftDAO.deleteDraftById(id);
            return Response.noContent().build();
        }
        return Response.serverError().build();
    }
}

