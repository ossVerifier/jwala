package com.siemens.cto.aem.domain.model.id;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Identifier<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long id;

    public Identifier(final String id) {
        this(Long.valueOf(id));
    }

    public Identifier(final Long id) {
        // TODO Throw new IllegalArgumentException if null?
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    /**
     * Helper method to return an identifier templated by a type
     */
    public static <U> Identifier<U> id(final Long longId) {
        return new Identifier<>(longId);
    }

    /**
     * Helper method to return an identifier templated by a type
     */
    public static <U> Identifier<U> id(final Long longId, final Class<U> clazz) {
        return new Identifier<>(longId);
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
        Identifier rhs = (Identifier) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .toString();
    }
}
