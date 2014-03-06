package com.siemens.cto.aem.ws.rest.v1;

import javax.ws.rs.core.Response;

import com.siemens.cto.aem.common.exception.FaultCodeException;
import com.siemens.cto.aem.common.exception.Success;
import com.siemens.cto.aem.ws.rest.ApplicationResponse;

public class ResponseBuilder {

    public static Response ok(final Object someContent) {
        return new ResponseBuilder().applicationResponseContent(someContent).build();
    }

    public static Response notOk(final Response.Status aStatus,
                                 final FaultCodeException aFaultCode) {
        return new ResponseBuilder(aStatus).applicationResponse(new ApplicationResponse(aFaultCode.getMessageResponseStatus(),
                                                                                        aFaultCode.getMessage())).build();
    }

    private ApplicationResponse applicationResponse;
    private Response.Status status;

    public ResponseBuilder() {
        this(Response.Status.OK);
    }

    public ResponseBuilder(final Response.Status aStatus) {
        status = aStatus;
    }

    public ResponseBuilder applicationResponseContent(final Object someContent) {
        return applicationResponse(new ApplicationResponse(Success.SUCCESS,
                                                           someContent));
    }


    public ResponseBuilder applicationResponse(final ApplicationResponse anApplicationResponse) {
        applicationResponse = anApplicationResponse;
        return this;
    }

    public ResponseBuilder status(final Response.Status aStatus) {
        status = aStatus;
        return this;
    }

    public Response build() {
        return Response.status(status).entity(applicationResponse).build();
    }
}
