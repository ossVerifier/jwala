package com.siemens.cto.aem.ws.rest.v1.service.resource;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.provider.PaginationParamProvider;
import com.siemens.cto.aem.ws.rest.v1.service.resource.impl.JsonCreateResourceInstance;
import com.siemens.cto.aem.ws.rest.v1.service.resource.impl.JsonUpdateResourceInstanceAttributes;
import com.siemens.cto.aem.ws.rest.v1.service.resource.impl.JsonUpdateResourceInstanceFriendlyName;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by z003e5zv on 3/16/2015.
 */
@Path("/resources")
@Produces(MediaType.APPLICATION_JSON)
public interface ResourceServiceRest {

    @GET
    @Path("/types")
    Response getAll();

    @GET
    @Path("/{name}")
    Response findResourceInstanceByNameGroup(@PathParam("name") final String name, @MatrixParam("groupName") final String groupName, @MatrixParam("resourceTypeName") String resourceTypeName, final PaginationParamProvider paginationParamProvider);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response createResourceInstance(final JsonCreateResourceInstance aResourceInstanceToCreate,
                                    @BeanParam final AuthenticatedUser aUser);

    @POST
    @Path("/updateAttributes")
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateResourceInstanceAttributes(final JsonUpdateResourceInstanceAttributes aResourceInstanceToUpdate,
                                              @BeanParam final AuthenticatedUser aUser);

    @POST
    @Path("/updateFriendlyName")
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateResourceInstanceFriendlyName(final JsonUpdateResourceInstanceFriendlyName jsonUpdateResourceInstanceFriendlyName, @BeanParam final AuthenticatedUser aUser);

    @DELETE
    @Path("/{resourceInstanceId}")
    Response removeResourceInstance(@PathParam("resourceInstanceId") final Identifier<ResourceInstance> aResourceInstanceId);

}
