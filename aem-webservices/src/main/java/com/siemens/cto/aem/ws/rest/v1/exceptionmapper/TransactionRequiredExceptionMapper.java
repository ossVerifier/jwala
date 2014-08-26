package com.siemens.cto.aem.ws.rest.v1.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.apache.openjpa.persistence.TransactionRequiredException;

import com.siemens.cto.aem.common.exception.FaultCodeException;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;

public class TransactionRequiredExceptionMapper implements ExceptionMapper<TransactionRequiredException> {

    @Override
    public Response toResponse(final TransactionRequiredException exception) {
        return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                new FaultCodeException(AemFaultType.PERSISTENCE_ERROR, "Database transaction missing", exception));
    }
}
