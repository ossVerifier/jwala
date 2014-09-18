package com.siemens.cto.aem.ws.rest.v1.service.group;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import com.siemens.cto.aem.ws.rest.v1.provider.GroupIdsParameterProvider;
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
    Response createGroup(final String aNewGroupName,
                         @BeanParam final AuthenticatedUser aUser);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    Response updateGroup(final JsonUpdateGroup anUpdatedGroup,
                         @BeanParam final AuthenticatedUser aUser);

    @DELETE
    @Path("/{groupId}")
    Response removeGroup(@PathParam("groupId") final Identifier<Group> aGroupId);

    @POST
    @Path("/{groupId}/jvms")
    Response addJvmsToGroup(@PathParam("groupId") final Identifier<Group> aGroupId,
                            final JsonJvms someJvmsToAdd,
                            @BeanParam final AuthenticatedUser aUser);

    @DELETE
    @Path("/{groupId}/jvms/{jvmId}")
    Response removeJvmFromGroup(@PathParam("groupId") final Identifier<Group> aGroupId,
                                @PathParam("jvmId") final Identifier<Jvm> aJvmId,
                                @BeanParam final AuthenticatedUser aUser);

    @POST
    @Path("/{groupId}/jvms/commands")
    Response controlGroupJvms(@PathParam("groupId") final Identifier<Group> aGroupId,
                              final JsonControlJvm jvmControlOperation,
                              @BeanParam final AuthenticatedUser aUser);

    @POST
    @Path("/{groupId}/commands")
    Response controlGroup(@PathParam("groupId") final Identifier<Group> aGroupId,
                          final JsonControlGroup groupControlOperation,
                          @BeanParam final AuthenticatedUser aUser);

    @POST
    @Path("/{groupId}/webservers/commands")
    Response controlGroupWebservers(@PathParam("groupId") final Identifier<Group> aGroupId,
                                    final JsonControlWebServer jsonControlWebServer,
                                    @BeanParam final AuthenticatedUser aUser);

    @DELETE
    @Path("/{groupId}/state")
    Response resetState(@PathParam("groupId") final Identifier<Group> aGroupId,
                        @BeanParam final AuthenticatedUser aUser);

    @GET
    @Path("/states/current")
//    TODO This should be reconciled with pagination, and with how to retrieve the states for every jvm without having to explicitly specify them
    Response getCurrentJvmStates(@BeanParam final GroupIdsParameterProvider aGroupIdsParameterProvider);

    /**
     * Gets the membership details of a group's children in other groups (e.g. jvm1 is a member of group2, group3)
     * Note: The group specified by id will not be included hence the word "Other" in the method name.
     *
     * @param id the id of the group
     * @param groupChildType the child type to get details on
     * @return membership details of a group's children
     */
    @GET
    @Path("/{groupId}/children/otherGroup/connectionDetails")
    Response getOtherGroupMembershipDetailsOfTheChildren(@PathParam("groupId") final Identifier<Group> id,
                                                         @QueryParam("groupChildType") final GroupChildType groupChildType);

}
