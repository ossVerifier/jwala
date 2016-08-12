package com.cerner.jwala.ws.rest.v1.service.state;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.cerner.jwala.ws.rest.v1.provider.TimeoutParameterProvider;

@Path("/states")
@Produces(MediaType.APPLICATION_JSON)
public interface StateServiceRest {

    @GET
    @Deprecated
    Response pollStates(@Context final HttpServletRequest aRequest,
                           @BeanParam final TimeoutParameterProvider aTimeoutParamProvider,
                           @QueryParam("clientId") @DefaultValue("1") final String clientId);

    @Path("/next")
    @GET
    @Deprecated
    Response pollState(@Context final HttpServletRequest aRequest,
            @QueryParam("clientId") @DefaultValue("1") final String clientId);

    @Path("/{groupName}/jvm")
    @GET
    Response requestCurrentStatesRetrievalAndNotification(@PathParam("groupName") String groupName);
}
