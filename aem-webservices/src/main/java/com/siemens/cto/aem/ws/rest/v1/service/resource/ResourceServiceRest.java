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

    /**
     * /aem/v1.0/resources;groupName=[your group name]
     * @param groupName
     * @param paginationParamProvider
     * @return
     */
    @GET
    Response findResourceInstanceByGroup(@MatrixParam("groupName") final String groupName, final PaginationParamProvider paginationParamProvider);

    /**
     * /aem/v1.0/resources/[your resource instance name];groupName=[your group name]
     * @param name
     * @param groupName
     * @param resourceTypeName
     * @param paginationParamProvider
     * @return
     */
    @GET
    @Path("/{name}")
    Response findResourceInstanceByNameGroup(@PathParam("name") final String name, @MatrixParam("groupName") final String groupName, @MatrixParam("resourceTypeName") String resourceTypeName, final PaginationParamProvider paginationParamProvider);

    /**
     * /aem/v1.0/resources
     * JSON POST data
     * @param aResourceInstanceToCreate
     * @param aUser
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response createResourceInstance(final JsonCreateResourceInstance aResourceInstanceToCreate, @BeanParam final AuthenticatedUser aUser);

    /**
     * /aem/v1.0/resources/[resource instance name];groupName=[your group name]
     * JSON PUT conttaining the same object as create, but empty attributes will remain the same
     * @param aResourceInstanceToUpdate
     * @param aUser
     * @return
     */
    @PUT
    @Path("/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateResourceInstanceAttributes(final JsonUpdateResourceInstanceAttributes aResourceInstanceToUpdate,
                                              @BeanParam final AuthenticatedUser aUser);

    /**
     * /aem/v1.0/resources/[resource instance name];groupName=[your group name]
     * @param aResourceInstanceId
     * @return
     */
    @DELETE
    @Path("/{resourceInstanceId}")
    Response removeResourceInstance(@PathParam("resourceInstanceId") final Identifier<ResourceInstance> aResourceInstanceId);

}
