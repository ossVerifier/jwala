package com.cerner.jwala.common.domain.model.id;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.exception.BadRequestException;

public class IdentifierSetBuilder {

    private Collection<String> ids;

    public IdentifierSetBuilder() {
    }

    public IdentifierSetBuilder(final Collection<String> someIds) {
        ids = someIds;
    }

    public IdentifierSetBuilder setIds(final Collection<String> someIds) {
        ids = someIds;
        return this;
    }

    public <T> Set<Identifier<T>> build() {

        try {
            final Set<Identifier<T>> newIds = new HashSet<>();

            for (final String id : ids) {
                newIds.add(new Identifier<T>(id));
            }

            return newIds;
        } catch (final NumberFormatException nfe) {
            throw new BadRequestException(AemFaultType.INVALID_IDENTIFIER,
                                          nfe.getMessage(),
                                          nfe);
        }
    }
}
