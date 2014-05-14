package com.siemens.cto.aem.ws.rest.v1.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;

public class InternalErrorExceptionMapper implements ExceptionMapper<InternalErrorException> {

    @Override
    public Response toResponse(final InternalErrorException exception) {
        return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                                     exception);
    }
}
