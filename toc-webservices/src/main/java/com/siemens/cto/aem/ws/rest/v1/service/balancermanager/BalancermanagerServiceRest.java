package com.siemens.cto.aem.ws.rest.v1.service.balancermanager;

import com.siemens.cto.aem.ws.rest.v1.provider.AuthenticatedUser;
import org.springframework.beans.factory.InitializingBean;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/balancermanager")
@Produces(MediaType.APPLICATION_JSON)
public interface BalancermanagerServiceRest extends InitializingBean {

    @POST
    @Path("/{groupName}")
    Response drainUserGroup(@PathParam("groupName") final String groupName,
                            final String webServerNames,
                            @BeanParam AuthenticatedUser authenticatedUser);

    @POST
    @Path("/{groupName}/{webserverName}")
    Response drainUserWebServer(@PathParam("groupName") final String groupName,
                                @PathParam("webserverName") final String webserverName,
                                @BeanParam AuthenticatedUser authenticatedUser);

    @GET
    @Path("/{groupName}")
    Response getGroup(@PathParam("groupName") final String groupName, @BeanParam AuthenticatedUser authenticatedUser);


}