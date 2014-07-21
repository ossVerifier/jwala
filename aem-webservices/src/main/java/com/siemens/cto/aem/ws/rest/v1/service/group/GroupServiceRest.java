package com.siemens.cto.aem.ws.rest.v1.service.group;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.ws.rest.v1.provider.NameSearchParameterProvider;
import com.siemens.cto.aem.ws.rest.v1.provider.PaginationParamProvider;
import com.siemens.cto.aem.ws.rest.v1.service.group.impl.JsonControlGroup;
import com.siemens.cto.aem.ws.rest.v1.service.group.impl.JsonJvms;
import com.siemens.cto.aem.ws.rest.v1.service.group.impl.JsonUpdateGroup;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.impl.JsonControlJvm;
import com.siemens.cto.aem.ws.rest.v1.service.webserver.impl.JsonControlWebServer;

@Path("/groups")
@Produces(MediaType.APPLICATION_JSON)
public interface GroupServiceRest {

    @GET
    Response getGroups(@BeanParam final PaginationParamProvider paginationParamProvider,
                       @BeanParam final NameSearchParameterProvider aGroupNameSearch);

    @GET
    @Path("/{groupId}")
    Response getGroup(@PathParam("groupId") final Identifier<Group> aGroupId);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Response createGroup(final String aNewGroupName);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateGroup(final JsonUpdateGroup anUpdatedGroup);

    @DELETE
    @Path("/{groupId}")
    Response removeGroup(@PathParam("groupId") final Identifier<Group> aGroupId);

    @POST
    @Path("/{groupId}/jvms")
    Response addJvmsToGroup(@PathParam("groupId") final Identifier<Group> aGroupId,
                            final JsonJvms someJvmsToAdd);

    @DELETE
    @Path("/{groupId}/jvms/{jvmId}")
    Response removeJvmFromGroup(@PathParam("groupId") final Identifier<Group> aGroupId,
                                @PathParam("jvmId") final Identifier<Jvm> aJvmId);

    @POST
    @Path("/{groupId}/jvms/commands")
    Response controlGroupJvms(@PathParam("groupId") final Identifier<Group> aGroupId,
                            final JsonControlJvm jvmControlOperation,
                            @Context SecurityContext jaxrsSecurityContext);

    @POST
    @Path("/{groupId}/commands")
    Response controlGroup(@PathParam("groupId") final Identifier<Group> aGroupId,
                            final JsonControlGroup groupControlOperation,
                            @Context SecurityContext jaxrsSecurityContext);

    @POST
    @Path("/{groupId}/webservers/commands")
    Response controlGroupWebservers(@PathParam("groupId") final Identifier<Group> aGroupId, 
                            final JsonControlWebServer jsonControlWebServer,
                            @Context SecurityContext jaxrsSecurityContext);
}
