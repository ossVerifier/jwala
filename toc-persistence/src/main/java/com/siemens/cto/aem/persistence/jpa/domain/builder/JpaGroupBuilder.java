package com.siemens.cto.aem.persistence.jpa.domain.builder;

import com.siemens.cto.aem.common.domain.model.group.CurrentGroupState;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupState;
import com.siemens.cto.aem.common.domain.model.group.History;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.persistence.dao.webserver.impl.jpa.JpaWebServerBuilder;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaHistory;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServer;
import org.joda.time.Chronology;
import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.Set;

public class JpaGroupBuilder {

    public static final Chronology USE_DEFAULT_CHRONOLOGY = null;
    private JpaGroup group;
    private CurrentGroupState currentGroupState = null;
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
        if(currentGroupState == null) {
            if (fetchWebServers) {
                return new Group(new Identifier<Group>(group.getId()),
                                 group.getName(),
                                 getJvms(),
                                 getWebServers(),
                                 currentGroupState,
                                 getHistory());
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
                             currentGroupState,
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

        return GroupState.GRP_UNKNOWN;
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

    protected Set<History> getHistory() {
        final Set<History> history = new HashSet<>();
        if (group.getHistory() != null) {
            for (final JpaHistory jpaHistory : group.getHistory()) {
                history.add(new JpaHistoryBuilder(jpaHistory).build());
            }
        }

        return history;
    }

    public JpaGroupBuilder setStateDetail(CurrentGroupState originalStatus) {
        this.currentGroupState = originalStatus;
        return this;
    }

    public JpaGroupBuilder setFetchWebServers(boolean fetchWebServers) {
        this.fetchWebServers = fetchWebServers;
        return this;
    }
}
