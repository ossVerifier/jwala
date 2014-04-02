package com.siemens.cto.aem.domain.model.temporary;

import java.io.Serializable;

//TODO Use common class from Health Check once a common repo is available
public class PaginationParameter implements Serializable {

    public static PaginationParameter all() {
        return new PaginationParameter(BEGINNING,
                                       NO_LIMIT);
    }

    private static final long serialVersionUID = 1L;

    public static final Integer DEFAULT_BEGINNING_OFFSET = 0;
    public static final Integer DEFAULT_NUMBER_TO_RETRIEVE = 10;
    
    public static final Integer BEGINNING = 0;
    public static final Integer NO_LIMIT = 0;

    private final Integer offset;
    private final Integer limit;

    public PaginationParameter() {
        this(DEFAULT_BEGINNING_OFFSET,
             DEFAULT_NUMBER_TO_RETRIEVE);
    }

    public PaginationParameter(final Integer theOffset,
                               final Integer theLimit) {
        offset = theOffset;
        limit = theLimit;

        validate(offset,
                 limit);
    }

    public Integer getOffset() {
        return offset;
    }

    public Integer getLimit() {
        return limit;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final PaginationParameter that = (PaginationParameter) o;

        if (limit != null ? !limit.equals(that.limit) : that.limit != null) {
            return false;
        }
        if (offset != null ? !offset.equals(that.offset) : that.offset != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = offset != null ? offset.hashCode() : 0;
        result = 31 * result + (limit != null ? limit.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PaginationParameter{" +
               "offset=" + offset +
               ", limit=" + limit +
               '}';
    }

    protected final void validate(final Integer anOffset,
                                  final Integer aLimit) {
        validateOffset(anOffset);
        validateLimit(aLimit);
    }

    protected final void validateOffset(final Integer anOffset) {
        if ((anOffset == null) || (anOffset < 0)) {
            throw new IllegalArgumentException("Offset must be a non-negative Integer, was: " + anOffset);
        }
    }

    protected final void validateLimit(final Integer aLimit) {
        if ((aLimit == null) || (aLimit < 0)) {
            throw new IllegalArgumentException("Limit must be a non-negative Integer, was: " + aLimit);
        }
    }
}
