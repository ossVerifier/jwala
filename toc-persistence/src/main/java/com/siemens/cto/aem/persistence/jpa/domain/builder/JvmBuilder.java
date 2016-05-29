package com.siemens.cto.aem.persistence.jpa.domain.builder;

import java.util.HashSet;
import java.util.Set;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;

/**
 * {@link Jvm} builder from a {@link JpaJvm}.
 */
public class JvmBuilder {

    private JpaJvm jpaJvm;

    public JvmBuilder() {}

    public JvmBuilder(final JpaJvm aJvm) {
        jpaJvm = aJvm;
    }

    public JvmBuilder setJpaJvm(final JpaJvm aJvm) {
        jpaJvm = aJvm;
        return this;
    }

    public Jvm build() {
        final com.siemens.cto.aem.common.domain.model.jvm.JvmBuilder builder =
                new com.siemens.cto.aem.common.domain.model.jvm.JvmBuilder();
        builder.setId(new Identifier<Jvm>(jpaJvm.getId()))
               .setName(jpaJvm.getName())
               .setHostName(jpaJvm.getHostName())
               .setStatusPath(new Path(jpaJvm.getStatusPath()))
               .setGroups(createGroups())
               .setHttpPort(jpaJvm.getHttpPort())
               .setHttpsPort(jpaJvm.getHttpsPort())
               .setRedirectPort(jpaJvm.getRedirectPort())
               .setShutdownPort(jpaJvm.getShutdownPort())
               .setAjpPort(jpaJvm.getAjpPort())
               .setSystemProperties(jpaJvm.getSystemProperties())
               .setState(jpaJvm.getState())
               .setErrorStatus(jpaJvm.getErrorStatus())
               .setLastUpdatedDate(jpaJvm.getLastUpdateDate());
               .setUserName(jpaJvm.getUserName())
               .setEncryptedPassword(jpaJvm.getEncryptedPassword());
        return builder.build();
    }

    protected Set<Group> createGroups() {
        final Set<Group> groups = new HashSet<>();

        if (jpaJvm.getGroups() != null) {
            for (final JpaGroup jpaGroup : jpaJvm.getGroups()) {
                groups.add(new Group(Identifier.<Group>id(jpaGroup.getId()), jpaGroup.getName()));
            }

        }
        return groups;
    }

}
