package com.siemens.cto.aem.ws.rest.v1.provider;

import javax.ws.rs.QueryParam;

import com.siemens.cto.aem.common.exception.FaultCodeException;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.ws.rest.v1.fault.RestFaultType;

public class PaginationParamProvider {

    @QueryParam("offset")
    private String offset;

    @QueryParam("limit")
    private String limit;

    public PaginationParamProvider() {
    }

    public PaginationParamProvider(final String anOffset,
                                   final String aLimit) {
        offset = anOffset;
        limit = aLimit;
    }

    public PaginationParameter getPaginationParameter() throws FaultCodeException {
        try {
            if (areParametersPresent()) {
                return new PaginationParameter(convert(offset),
                                               convert(limit));
            } else {
                return new PaginationParameter();
            }
        } catch (final IllegalArgumentException iae) {
            throw new FaultCodeException(RestFaultType.INVALID_PAGINATION_PARAMETER,
                                         "Invalid pagination arguments (offset=" + offset + ", limit=" + limit + "): " + iae.getMessage());
        }
    }

    protected boolean areParametersPresent() {
        return (offset != null) && (limit != null);
    }

    protected Integer convert(final String anInteger) throws FaultCodeException {
        try {
            final Integer converted = Integer.parseInt(anInteger);
            return converted;
        } catch (final NumberFormatException nfe) {
            throw new FaultCodeException(RestFaultType.INVALID_PAGINATION_PARAMETER,
                                         "Couldn't parse (" + anInteger + ") : " + nfe.getMessage());
        }
    }
}
