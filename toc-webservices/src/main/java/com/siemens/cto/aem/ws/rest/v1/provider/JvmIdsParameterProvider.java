package com.siemens.cto.aem.ws.rest.v1.provider;

import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.ws.rs.QueryParam;
import java.util.HashSet;
import java.util.Set;

public class JvmIdsParameterProvider extends AbstractIdsParameterProvider<Jvm> {

    @QueryParam("jvmId")
    private Set<String> jvmIds;

    public JvmIdsParameterProvider(final Set<String> someJmIds) {
        this();
        jvmIds = new HashSet<>(someJmIds);
    }

    public JvmIdsParameterProvider() {
        super("Invalid JVM Identifier specified");
    }

    @Override
    protected Set<String> getIds() {
        return jvmIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("jvmIds", jvmIds)
                .toString();
    }
}
