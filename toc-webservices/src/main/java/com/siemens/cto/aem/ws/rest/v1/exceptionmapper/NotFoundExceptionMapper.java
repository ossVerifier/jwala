package com.siemens.cto.aem.ws.rest.v1.exceptionmapper;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Override
    public Response toResponse(final NotFoundException exception) {
        return ResponseBuilder.notOk(Response.Status.NOT_FOUND,
                                     exception);
    }
}
