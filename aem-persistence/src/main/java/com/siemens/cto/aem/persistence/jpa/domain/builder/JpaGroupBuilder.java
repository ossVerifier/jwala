package com.siemens.cto.aem.persistence.jpa.domain.builder;

import java.util.HashSet;
import java.util.Set;

import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.persistence.dao.webserver.impl.jpa.JpaWebServerBuilder;
import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServer;
import org.joda.time.Chronology;
import org.joda.time.DateTime;

import com.siemens.cto.aem.domain.model.group.CurrentGroupState;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupState;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;

public class JpaGroupBuilder {

    public static final Chronology USE_DEFAULT_CHRONOLOGY = null;
    private JpaGroup group;
    private CurrentGroupState stateDetailSource = null;
    private boolean fetchWebServers = false;

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
        if(stateDetailSource == null) {
            if (fetchWebServers) {
                return new Group(new Identifier<Group>(group.getId()),
                                     group.getName(),
                                     getJvms(),
                                     getWebServers(),
                                     stateDetailSource);
            }
            return new Group(new Identifier<Group>(group.getId()),
                             group.getName(),
                             getJvms(),
                             getState(),
                             getAsOf());
        } else {
            return new Group(new Identifier<Group>(group.getId()),
                             group.getName(),
                             getJvms(),
                             stateDetailSource,
                             getAsOf());
        }
    }

    private DateTime getAsOf() {
        if (group.getStateUpdated() != null) {
            return new DateTime(group.getStateUpdated(),
                                USE_DEFAULT_CHRONOLOGY);
        }

        return null;
    }

    private GroupState getState() {

        if (group.getState() != null) {
            return group.getState();
        }

        return GroupState.UNKNOWN;
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

    protected Set<WebServer> getWebServers() {
        final Set<WebServer> webServers = new HashSet<>();
        if (group.getWebServers() != null) {
            for (final JpaWebServer jpaWebServer : group.getWebServers()) {
                webServers.add(new JpaWebServerBuilder(jpaWebServer).build());
            }
        }

        return webServers;
    }

    public JpaGroupBuilder setStateDetail(CurrentGroupState originalStatus) {
        this.stateDetailSource = originalStatus;
        return this;
    }

    public JpaGroupBuilder setFetchWebServers(boolean fetchWebServers) {
        this.fetchWebServers = fetchWebServers;
        return this;
    }
}
