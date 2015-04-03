package com.siemens.cto.aem.ws.rest.v1.service.resource;

import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.service.resource.impl.JsonResourceInstance;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by z003e5zv on 3/16/2015.
 */
@Path("/resources")
@Produces(MediaType.APPLICATION_JSON)
public interface ResourceServiceRest {

    /**
     * /aem/v1.0/resources/types
     * @PathParam name
     * @MatrixParam groupName
     * @BeanParam AuthenticatedUser
     * @return
     */
    @GET
    @Path("/types")
    Response getTypes();

    /**
     * /aem/v1.0/resources;groupName=[your group name]
     * @PathParam name
     * @MatrixParam groupName
     * @return
     */
    @GET
    Response findResourceInstanceByGroup(@MatrixParam("groupName") final String groupName);

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
    Response findResourceInstanceByNameGroup(@PathParam("name") final String name, @MatrixParam("groupName") final String groupName, @MatrixParam("resourceTypeName") String resourceTypeName);

    /**
     * /aem/v1.0/resources
     * JSON POST data
     * @param aResourceInstanceToCreate
     * @param aUser
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response createResourceInstance(final JsonResourceInstance aResourceInstanceToCreate, @BeanParam final AuthenticatedUser aUser);

    /**
     * /aem/v1.0/resources/[resource instance name];groupName=[your group name]
     * JSON PUT conttaining the same object as create, but empty attributes will remain the same
     * @PathParam name
     * @MatrixParam groupName
     * @BeanParam AuthenticatedUser
     * @return
     */
    @PUT
    @Path("/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateResourceInstanceAttributes(@PathParam("name") final String name, @MatrixParam("groupName") final String groupName, final JsonResourceInstance aResourceInstanceToUpdate, @BeanParam final AuthenticatedUser aUser);

    /**
     * /aem/v1.0/resources/[resource instance name];groupName=[your group name]
     * @PathParam name
     * @MatrixParam groupName
     * @BeanParam AuthenticatedUser
     * @return
     */
    @DELETE
    @Path("/{name}")
    Response removeResourceInstance(@PathParam("name") final String name, @MatrixParam("groupName") final String groupName);

}
