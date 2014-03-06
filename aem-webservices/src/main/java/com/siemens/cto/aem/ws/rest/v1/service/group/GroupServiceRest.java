package com.siemens.cto.aem.ws.rest.v1.service.group;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.ws.rest.v1.provider.PaginationParamProvider;

@Path("/v1/groups")
@Produces(MediaType.APPLICATION_JSON)
public interface GroupServiceRest {

    @GET
//    List<Group> getGroups(@BeanParam final PaginationParamProvider paginationParamProvider);
    Response getGroups(@BeanParam final PaginationParamProvider paginationParamProvider);

    @GET
    @Path("/{groupId}")
//    Group getGroup(@PathParam("groupId") final Identifier<Group> aGroupId);
    Response getGroup(@PathParam("groupId") final Identifier<Group> aGroupId);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
//    Group createGroup(final String aNewGroupName);
    Response createGroup(final String aNewGroupName);
}
