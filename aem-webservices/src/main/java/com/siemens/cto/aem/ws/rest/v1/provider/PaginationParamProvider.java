package com.siemens.cto.aem.ws.rest.v1.provider;

import javax.ws.rs.QueryParam;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.ws.rest.v1.fault.RestFaultType;

public class PaginationParamProvider {

    private static final String NOT_PRESENT = null;

    @QueryParam("offset")
    private String offset;

    @QueryParam("limit")
    private String limit;

    @QueryParam("all")
    private String retrieveAll;

    public PaginationParamProvider() {
    }

    public PaginationParamProvider(final String retrieveAll) {
        this(NOT_PRESENT,
             NOT_PRESENT,
             retrieveAll);
    }

    public PaginationParamProvider(final String offset,
                                   final String limit) {
        this(offset,
             limit,
             NOT_PRESENT);
    }

    public PaginationParamProvider(final String anOffset,
                                   final String aLimit,
                                   final String shouldRetrieveAll) {
        offset = anOffset;
        limit = aLimit;
        retrieveAll = shouldRetrieveAll;
    }

    public PaginationParameter getPaginationParameter() throws BadRequestException {
        try {
            if (areParametersPresent()) {
                return new PaginationParameter(convert(offset),
                                               convert(limit));
            } else if (isRetrieveAllParameterPresent()) {
                return PaginationParameter.all();
            } else {
                return new PaginationParameter();
            }
        } catch (final IllegalArgumentException iae) {
            throw new BadRequestException(RestFaultType.INVALID_PAGINATION_PARAMETER,
                                          "Invalid pagination arguments (offset=" + offset + ", limit=" + limit + "): " + iae.getMessage(),
                                          iae);
        }
    }

    protected boolean areParametersPresent() {
        return (offset != null) && (limit != null);
    }

    protected boolean isRetrieveAllParameterPresent() {
        return (retrieveAll != null);
    }

    protected Integer convert(final String anInteger) throws BadRequestException {
        try {
            final Integer converted = Integer.parseInt(anInteger);
            return converted;
        } catch (final NumberFormatException nfe) {
            throw new BadRequestException(RestFaultType.INVALID_PAGINATION_PARAMETER,
                                          "Couldn't parse (" + anInteger + ") : " + nfe.getMessage(),
                                          nfe);
        }
    }

    @Override
    public String toString() {
        return "PaginationParamProvider{" +
               "offset='" + offset + '\'' +
               ", limit='" + limit + '\'' +
               '}';
    }
}
