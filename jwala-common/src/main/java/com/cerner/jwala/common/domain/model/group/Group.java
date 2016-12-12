package com.cerner.jwala.common.domain.model.group;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.state.StateType;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Group implements Serializable {

    private final Identifier<Group> id;
    private final String name;
    private final Set<Jvm> jvms;
    private final Set<WebServer> webServers;
    private final CurrentState currentState;
    private final Set<History> history;
    private final Set<Application> applications;

    public Group(final Identifier<Group> theId,
                 final String theName) {
        this(theId, theName, Collections.<Jvm> emptySet(), GroupState.GRP_UNKNOWN, DateTime.now());
    }

    public Group(final Identifier<Group> theId,
                 final String theName,
                 final Set<Jvm> theJvms) {
        this(theId, theName, theJvms, GroupState.GRP_UNKNOWN, DateTime.now());
    }

    public Group(final Identifier<Group> theId,
                 final String theName,
                 final Set<Jvm> theJvms,
                 final GroupState theState,
                 final DateTime theAsOf) {
        id = theId;
        name = theName;
        jvms = Collections.unmodifiableSet(new HashSet<>(theJvms));
        webServers = null;
        currentState = new CurrentState(theId, theState, theAsOf, StateType.GROUP);
        history = null;
        applications = null;
    }

    public Group(final Identifier<Group> theId,
                 final String theName,
                 final Set<Jvm> theJvms,
                 final Set<WebServer> theWebServers,
                 final CurrentState theState,
                 final Set<History> theHistory) {
        id = theId;
        name = theName;
        jvms = Collections.unmodifiableSet(new HashSet<>(theJvms));
        webServers = Collections.unmodifiableSet(new HashSet<>(theWebServers));
        currentState = theState;
        history = theHistory;
        applications = null;
    }

    public Group(final Identifier<Group> theId,
                 final String theName,
                 final Set<Jvm> theJvms,
                 final CurrentState theState) {
        id = theId;
        name = theName;
        jvms = Collections.unmodifiableSet(new HashSet<>(theJvms));
        webServers = null;
        currentState = theState;
        history = null;
        applications = null;
    }

    public Group(Identifier<Group> id, String name, Set<Jvm> jvms, Set<WebServer> webServers, CurrentState currentState, Set<History> history, Set<Application> applications) {
        this.id = id;
        this.name = name;
        this.jvms = jvms;
        this.webServers = webServers;
        this.currentState = currentState;
        this.history = history;
        this.applications = applications;
    }

    public Identifier<Group> getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Jvm> getJvms() {
        return jvms;
    }

    public Set<WebServer> getWebServers() {
        return webServers;
    }

    public CurrentState getCurrentState() {
        return currentState;
    }

    public Set<History> getHistory() {
        return history;
    }

    public Set<Application> getApplications() {
        return applications;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        Group rhs = (Group) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .append(this.name, rhs.name)
                .append(this.jvms, rhs.jvms)
                .append(this.currentState,rhs.currentState)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(name)
                .append(jvms)
                .append(currentState)
                .append(history)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", jvms=" + jvms +
                ", webServers=" + webServers +
                ", currentState=" + currentState +
                ", history=" + history +
                ", applications=" + applications +
                '}';
    }
}
