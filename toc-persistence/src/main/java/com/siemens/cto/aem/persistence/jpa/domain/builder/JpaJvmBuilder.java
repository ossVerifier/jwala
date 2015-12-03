package com.siemens.cto.aem.persistence.jpa.domain.builder;

import com.siemens.cto.aem.domain.model.group.LiteGroup;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmBuilder;
import com.siemens.cto.aem.domain.model.path.Path;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;

import java.util.HashSet;
import java.util.Set;

public class JpaJvmBuilder {

    private JpaJvm jvm;

    public JpaJvmBuilder() {
    }

    public JpaJvmBuilder(final JpaJvm aJvm) {
        jvm = aJvm;
    }

    public JpaJvmBuilder setJvm(final JpaJvm aJvm) {
        jvm = aJvm;
        return this;
    }

    public Jvm build() {
        final JvmBuilder builder = new JvmBuilder().setId(new Identifier<Jvm>(jvm.getId()))
                                                   .setName(jvm.getName())
                                                   .setHostName(jvm.getHostName())
                                                   .setStatusPath(new Path(jvm.getStatusPath()))
                                                   .setGroups(createLiteGroups())
                                                   .setHttpPort(jvm.getHttpPort())
                                                   .setHttpsPort(jvm.getHttpsPort())
                                                   .setRedirectPort(jvm.getRedirectPort())
                                                   .setShutdownPort(jvm.getShutdownPort())
                                                   .setAjpPort(jvm.getAjpPort())
                                                   .setSystemProperties(jvm.getSystemProperties()) ;
        return builder.build();
    }

    protected Set<LiteGroup> createLiteGroups() {
        final Set<LiteGroup> groups = new HashSet<>();
        final JpaLiteGroupBuilder builder = new JpaLiteGroupBuilder();

        if (jvm.getGroups() != null) {
            for (final JpaGroup group : jvm.getGroups()) {
                builder.setGroup(group);
                groups.add(builder.build());
            }

        }
        return groups;
    }
}
