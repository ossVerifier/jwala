package com.siemens.cto.aem.ws.rest.v1.service.resource;

import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.service.resource.impl.JsonResourceInstance;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

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
     * /aem/v1.0/resources;groupName=[your group name]
     *
     * @param groupName the name of the previously created group
     * @return a list of ResourceInstance objects associated with a group
     */
    @GET
    Response findResourceInstanceByGroup(@MatrixParam("groupName") final String groupName);

    /**
     * /aem/v1.0/resources/[your resource instance name];groupName=[your group name]
     *
     * @param name the name of an existing resource instance
     * @param groupName the name of an existing group
     * @return a specific resourceInstance object if present
     */
    @GET
    @Path("/{name}")
    Response findResourceInstanceByNameGroup(@PathParam("name") final String name, @MatrixParam("groupName") final String groupName);

    /**
     * /aem/v1.0/resources <br/>
     * JSON POST data of JsonResourceInstance
     * @param aResourceInstanceToCreate {@link JsonResourceInstance}
     * @param aUser the authenticated user who is creating the ResourceInstance
     * @return the newly created ResourceInstance object
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response createResourceInstance(final JsonResourceInstance aResourceInstanceToCreate, @BeanParam final AuthenticatedUser aUser);

    /**
     * /aem/v1.0/resources/[resource instance name];groupName=[your group name] <br/>
     * JSON PUT conttaining the same object as create, but empty attributes will remain the same and it will detect changes in the name within the JsonResourceInstance object
     * @param name the name of an existing resource instance for updating
     * @param groupName the name of an existing group which is associcated with the resource instance to be updated.
     * @param aUser the authenticated user who is updating the resource instance
     * @return the updated ResourceInstance object
     */
    @PUT
    @Path("/{name}")
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateResourceInstanceAttributes(@PathParam("name") final String name, @MatrixParam("groupName") final String groupName, final JsonResourceInstance aResourceInstanceToUpdate, @BeanParam final AuthenticatedUser aUser);

    /**
     * Creates a template file and it's corresponding JSON meta data file.
     * A template file is used when generating the actual resource file what will be deployed to a JVM or web server.
     * @param attachments contains the template's meta data and main content
     * @param user a logged in user who's calling this service
     * @return {@link Response}
     */
    @POST
    @Path("/template/{targetName}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response createTemplate(List<Attachment> attachments, @PathParam("targetName") final String targetName, @BeanParam AuthenticatedUser user);

    @GET
    @Path("/data")
    Response getResourceAttrData();

    /**
     * Gets the resource data topology.
     * @return resource JSON data topology wrapped by {@link Response}.
     */
    @GET
    @Path("/topology")
    Response getResourceTopology();

    @GET
    @Path("/{groupName}/{appName}/name")
    Response getApplicationResourceNames(@PathParam("groupName") String groupName, @PathParam("appName") String appName);

    /**
     * Gets an application's resource template.
     * @param groupName the group the application belongs to
     * @param appName the application name
     * @param templateName the template name
     * @return {@link Response}
     */
    @GET
    @Path("/{groupName}/{appName}/{templateName}")
    Response getAppTemplate(@PathParam("groupName") String groupName, @PathParam("appName") String appName,
                            @PathParam("templateName") String templateName);

    /**
     * Checks if a group/jvm/webapp/webserver contains a resource file.
     * @param groupName name of the group under which the resource file should exist or the jvm/webapp/webvserver should exist
     * @param jvmName name of the jvm under which the resource file should exist
     * @param webappName name of the webapp under which the resource file should exist
     * @param webserverName name of the webserver under which the resource file should exist
     * @param fileName name of the resource file that is being searched
     * @return returns a json string with the information about the file {@link Response}
     */
    @GET
    @Path("/exists/{fileName}")
    Response checkFileExists(@QueryParam("group") String groupName,
                             @QueryParam("jvm") String jvmName,
                             @QueryParam("webapp") String webappName,
                             @QueryParam("webserver") String webserverName,
                             @PathParam("fileName") String fileName);

    /**
     * Creates a resource.
     * @param attachments contains the template's meta data and content
     * @param createResourceParam contains information on who owns the resource to be created
     * @param user a logged in user who's calling this service  @return {@link Response}
     */
    @POST
    @Path("/data")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    // TODO: Discuss with the team that the file name/resource name should be in the path instead of the the meta data so instead of /data, it will be /{name} which makes more sense REST-wise
    Response createResource(List<Attachment> attachments, @MatrixParam("") CreateResourceParam createResourceParam,
                            @BeanParam AuthenticatedUser user);

    @DELETE
    @Path("/template/{name}")
    @Deprecated
    Response deleteResource(@PathParam("name") String templateName, @MatrixParam("") ResourceHierarchyParam resourceHierarchyParam, @BeanParam AuthenticatedUser user);

    /**
     * Delete resources.
     * @param templateNameArray contains the template names of resource to delete
     * @param resourceHierarchyParam the entity hierarchy that describes where the resource belongs to
     * @param user the user
     * @return a wrapper class that contains the number of records that were deleted
     */
    @DELETE
    @Path("/templates")
    Response deleteResources(@MatrixParam("name") String [] templateNameArray, @MatrixParam("") ResourceHierarchyParam resourceHierarchyParam, @BeanParam AuthenticatedUser user);

    @GET
    @Path("/{name}/content")
    Response getResourceContent(@PathParam("name") String name, @MatrixParam("") ResourceHierarchyParam resourceHierarchyParam);

    /**
     * Upload an external properties file
     * @param attachment contains the properties file
     * @param user a logged in user calling the service
     * @return the external properties
     */
    @POST
    @Path("/properties")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    Response uploadExternalProperties(@Multipart Attachment attachment, @BeanParam AuthenticatedUser user);

    @GET
    @Path("/properties")
    Response getExternalProperties();

    @GET
    @Path("/properties/file")
    Response getExternalPropertiesFile();
}

