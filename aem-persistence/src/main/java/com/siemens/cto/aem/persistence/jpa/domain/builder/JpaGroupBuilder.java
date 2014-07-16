package com.siemens.cto.aem.persistence.jpa.domain.builder;

import java.util.HashSet;
import java.util.Set;

import org.joda.time.Chronology;
import org.joda.time.DateTime;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;

public class JpaGroupBuilder {

    public static final Chronology USE_DEFAULT_CHRONOLOGY = null;
    private JpaGroup group;

    public JpaGroupBuilder() {
    }

    public JpaGroupBuilder(final JpaGroup aGroup) {
        group = aGroup;
    }

    public JpaGroupBuilder setGroup(final JpaGroup aGroup) {
        group = aGroup;
        return this;
    }

    public Group build() {
        return new Group(new Identifier<Group>(group.getId()),
                         group.getName(),
                         getJvms(),
                         group.getState(),
                         new DateTime(group.getStateUpdated(), 
                                 USE_DEFAULT_CHRONOLOGY));
    }

    protected Set<Jvm> getJvms() {
        final Set<Jvm> jvms = new HashSet<>();
        if (group.getJvms() != null) {
            final JpaJvmBuilder builder = new JpaJvmBuilder();
            for (final JpaJvm jpaJvm : group.getJvms()) {
                jvms.add(builder.setJvm(jpaJvm).build());
            }
        }

        return jvms;
    }
}
