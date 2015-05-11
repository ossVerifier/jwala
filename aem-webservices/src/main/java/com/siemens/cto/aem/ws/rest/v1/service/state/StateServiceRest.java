package com.siemens.cto.aem.ws.rest.v1.service.state;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.siemens.cto.aem.ws.rest.v1.provider.TimeoutParameterProvider;

@Path("/states")
@Produces(MediaType.APPLICATION_JSON)
public interface StateServiceRest {

    @GET
    Response pollStates(@Context final HttpServletRequest aRequest,
                           @BeanParam final TimeoutParameterProvider aTimeoutParamProvider,
                           @QueryParam("clientId") @DefaultValue("1") final String clientId);

    @Path("/next")
    @GET
    Response pollState(@Context final HttpServletRequest aRequest,
            @QueryParam("clientId") @DefaultValue("1") final String clientId);
}
