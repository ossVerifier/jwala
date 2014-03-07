package com.siemens.cto.aem.ws.rest.v1.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;

public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {

    @Override
    public Response toResponse(final BadRequestException exception) {
        return ResponseBuilder.notOk(Response.Status.BAD_REQUEST,
                                     exception);
    }
}
