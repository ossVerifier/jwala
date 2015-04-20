package com.siemens.cto.aem.ws.rest.v1.service.resource;

import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.service.resource.impl.JsonResourceInstance;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by z003e5zv on 3/16/2015.
 */
@Path("/resources")
@Produces(MediaType.APPLICATION_JSON)
public interface ResourceServiceRest {

    /**
     * /aem/v1.0/resources/types
     *
     * @return a list of resourceTypes from the file system
     */
    @GET
    @Path("/types")
    Response getTypes();

    /**
     * /aem/v1.0/resources;groupName=[your group name]
     *
     * @PathParam name  the name of the resource instance
     * @MatrixParam groupName the name of the previously created group
     * @return a list of ResourceInstance objects associated with a group
     */
    @GET
    Response findResourceInstanceByGroup(@MatrixParam("groupName") final String groupName);

    /**
     * /aem/v1.0/resources/[your resource instance name];groupName=[your group name]
     *
     * @param name the name of an existing resource instance
     * @param groupName the name of an existing group
     * @param resourceTypeName
     * @return a specific resourceInstance object if present
     */
    @GET
    @Path("/{name}")
    Response findResourceInstanceByNameGroup(@PathParam("name") final String name, @MatrixParam("groupName") final String groupName, @MatrixParam("resourceTypeName") String resourceTypeName);

    @GET
    @Path("/{name}/generate")
    // TODO: Replace generate with a noun that "describes" the service e.g. /{name}/xml please see https://blog.apigee.com/detail/restful_api_design_nouns_are_good_verbs_are_bad
    // TODO: Remove unused parameters.
    Response generateResourceInstanceByNameGroup(@PathParam("name") final String name, @MatrixParam("groupName") final String groupName, @MatrixParam("resourceTypeName") String resourceTypeName);

    /**
     * /aem/v1.0/resources <br/>
     * JSON POST data of JsonResourceInstance
     * @param aResourceInstanceToCreate
     * @param aUser the authenticated user who is creating the ResourceInstance
     * @return the newly created ResourceInstance object
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response createResourceInstance(final JsonResourceInstance aResourceInstanceToCreate, @BeanParam final AuthenticatedUser aUser);

    /**
     * /aem/v1.0/resources/[resource instance name];groupName=[your group name] <br/>
     * JSON PUT conttaining the same object as create, but empty attributes will remain the same and it will detect changes in the name within the JsonResourceInstance object
     * @PathParam name the name of an existing resource instance for updating
     * @MatrixParam groupName the name of an existing group which is associcated with the resource instance to be updated.
     * @BeanParam AuthenticatedUser  the authenticated user who is updating the resource instance
     * @return the updated ResourceInstance object
     */
    @PUT
    @Path("/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateResourceInstanceAttributes(@PathParam("name") final String name, @MatrixParam("groupName") final String groupName, final JsonResourceInstance aResourceInstanceToUpdate, @BeanParam final AuthenticatedUser aUser);

    /**
     * /aem/v1.0/resources/[resource instance name];groupName=[your group name]
     * @PathParam name the name of a the existing ResourceInstance to be deleted
     * @MatrixParam groupName the group name of the resource instance to be deleted
     * @BeanParam AuthenticatedUser the user which is doing the deletion
     * @return  If successful nothing.
     */
    @DELETE
    @Path("/{name}")
    Response removeResourceInstance(@PathParam("name") final String name, @MatrixParam("groupName") final String groupName);

    /**
     * Removes a list of resources.
     *
     * usage: /aem/v1.0/resources;groupName=[group name];resourceName=[resourceName1];resourceName=[resourceName2]
     *
     * @param groupName the group where the resources to be removed belong to.
     * @param resourceNames the names of the resources to remove.
     *
     * @return
     */
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    Response removeResources(@MatrixParam("groupName") final String groupName,
                             @MatrixParam("resourceName") final List<String> resourceNames);

    @GET
    @Path("/types/{resourceTypeName}/template")
    Response getTemplate(@PathParam("resourceTypeName") final String resourceTypeName);

}
