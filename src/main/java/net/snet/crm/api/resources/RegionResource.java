package net.snet.crm.api.resources;

import com.yammer.dropwizard.jersey.params.LongParam;
import com.yammer.metrics.annotation.Timed;
import net.snet.crm.api.dao.RegionDao;
import net.snet.crm.api.model.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 22.12.13.
 */
@Path("/regions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RegionResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(RegionResource.class);

	private final RegionDao regionDao;

	public RegionResource(RegionDao regionDao) {
		this.regionDao = regionDao;
	}

	@GET
    @Produces({"application/json; charset=UTF-8"})
	@Timed(name = "get-requests")
	public Map<String, Object> findAllRegions() {
		final HashMap<String, Object> regions = new HashMap<>();
		final List<Region> allRegions = regionDao.findAllRegions();
		regions.put("regions", allRegions);
		return regions;
	}

    @GET
    @Path("/{regionId}")
    @Produces({"application/json; charset=UTF-8"})
    @Timed(name = "get-requests")
    public Map<String, Object> returnRegion(@PathParam("regionId") LongParam regionId, @Context HttpServletResponse response) {

        final HashMap<String, Object> regions = new HashMap<>();
        final List<Region> region = regionDao.getRegionById(regionId.get().intValue());

        if (region.isEmpty()) {
            throw new WebApplicationException(Response.status(404).build());
        }

        regions.put("regions", region);
        return regions;
    }

    @DELETE
    @Path("/{regionId}")
    @Timed(name = "get-requests")
    public void deleteRegion(@PathParam("regionId") LongParam regionId) {

        int retCode = regionDao.geleteRegionById(regionId.get().intValue());

        if (retCode == 0) {
            throw new WebApplicationException(Response.status(404).build());
        }
    }
}
