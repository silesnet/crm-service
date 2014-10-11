package net.snet.crm.service.resources;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Map;

@Path("/drafts2")
@Produces({"application/json; charset=UTF-8"})
@Consumes(MediaType.APPLICATION_JSON)
public class DraftResource2 {

	@Context private UriInfo uriInfo;

	@POST
	public Response createDraft() {
		Long draftId = 1L;
		Map<?, ?>  draftData = ImmutableMap.of(
			"name", "Jan Nowak"
		);
		Map<String, Object> draft = Maps.newLinkedHashMap();
		draft.put("id", draftId);
		draft.put("entityType", "customers");
		draft.put("entityId", 0);
		draft.put("entityName", "Jan Nowak");
		draft.put("owner", "test");
		draft.put("status", "DRAFT");
		draft.put("data", draftData);

		Map<?, ?> resData = ImmutableMap.of("drafts", draft);
		return Response.created(uriInfo.getRequestUriBuilder().replacePath("{id}").build(draftId))
					.entity(resData)
					.build();
	}

}
