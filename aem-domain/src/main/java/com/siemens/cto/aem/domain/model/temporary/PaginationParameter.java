package com.siemens.cto.aem.domain.model.temporary;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

//TODO Use common class from Health Check once a common repo is available
public class PaginationParameter implements Serializable {

    public static PaginationParameter all() {
        return new PaginationParameter(BEGINNING,
                                       NO_LIMIT);
    }

    private static final long serialVersionUID = 1L;

    private static final Integer DEFAULT_BEGINNING_OFFSET = 0;
    private static final Integer DEFAULT_NUMBER_TO_RETRIEVE = 10;

    private static final Integer BEGINNING = 0;
    private static final Integer NO_LIMIT = 0;

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

    public boolean isLimited() {
        return !NO_LIMIT.equals(limit);
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        PaginationParameter rhs = (PaginationParameter) obj;
        return new EqualsBuilder()
                .append(this.offset, rhs.offset)
                .append(this.limit, rhs.limit)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(offset)
                .append(limit)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("offset", offset)
                .append("limit", limit)
                .toString();
    }
}
