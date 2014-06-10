package com.siemens.cto.aem.ws.rest.v1.provider;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.QueryParam;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;

public class JvmIdsParameterProvider {

    @QueryParam("jvmId")
    private Set<String> jvmIds;

    public JvmIdsParameterProvider() {
    }

    public JvmIdsParameterProvider(final Set<String> someJmIds) {
        jvmIds = new HashSet<>(someJmIds);
    }

    public Set<Identifier<Jvm>> valueOf() {

        if (isParameterPresent()) {
            return parseParameters();
        }

        return Collections.emptySet();
    }

    private boolean isParameterPresent() {
        return jvmIds != null;
    }

    private Set<Identifier<Jvm>> parseParameters() {
        try {
            final Set<Identifier<Jvm>> ids = new HashSet<>();
            for (final String jvmId : jvmIds) {
                ids.add(new Identifier<Jvm>(Long.valueOf(jvmId)));
            }
            return ids;
        } catch (final NumberFormatException nfe) {
            throw new BadRequestException(AemFaultType.INVALID_IDENTIFIER,
                                          "Invalid JVM Identifier specified",
                                          nfe);
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("jvmIds", jvmIds)
                .toString();
    }
}
