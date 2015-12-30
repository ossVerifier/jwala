package com.siemens.cto.aem.persistence.jpa.domain.builder;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.LiteGroup;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmBuilder;
import com.siemens.cto.aem.common.domain.model.path.Path;
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
                                                   .setGroups(createGroups())
                                                   .setHttpPort(jvm.getHttpPort())
                                                   .setHttpsPort(jvm.getHttpsPort())
                                                   .setRedirectPort(jvm.getRedirectPort())
                                                   .setShutdownPort(jvm.getShutdownPort())
                                                   .setAjpPort(jvm.getAjpPort())
                                                   .setSystemProperties(jvm.getSystemProperties()) ;
        return builder.build();
    }

    protected Set<Group> createGroups() {
        final Set<Group> groups = new HashSet<>();

        if (jvm.getGroups() != null) {
            for (final JpaGroup jpaGroup : jvm.getGroups()) {
                groups.add(new Group(Identifier.<Group>id(jpaGroup.getId()), jpaGroup.getName()));
            }

        }
        return groups;
    }

}
