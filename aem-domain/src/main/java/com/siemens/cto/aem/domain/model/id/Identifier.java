package com.siemens.cto.aem.domain.model.id;

import java.io.Serializable;

public class Identifier<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long id;

    public Identifier(final String id) {
        this(Long.valueOf(id));
    }

    public Identifier(final Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Identifier<?> that = (Identifier<?>) o;

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Identifier{" +
               "id=" + id +
               '}';
    }
    
    /**
     * Helper method to return an identifier templated by a type
     */
    public static <U> Identifier<U> id(final Long longId) {
        return new Identifier<U>(longId);
    }
    /**
     * Helper method to return an identifier templated by a type
     */
    public static <U> Identifier<U> id(final Long longId, Class<U> clazz ) {
        return new Identifier<U>(longId);
    }

}
