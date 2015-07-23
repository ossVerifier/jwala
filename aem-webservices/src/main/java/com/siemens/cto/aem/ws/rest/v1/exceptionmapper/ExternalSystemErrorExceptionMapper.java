package com.siemens.cto.aem.ws.rest.v1.exceptionmapper;

import com.siemens.cto.aem.common.exception.ExternalSystemErrorException;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ExternalSystemErrorExceptionMapper implements ExceptionMapper<ExternalSystemErrorException> {

    @Override
    public Response toResponse(final ExternalSystemErrorException exception) {
        return ResponseBuilder.notOk(Response.Status.BAD_GATEWAY,
                                     exception);
    }
}
