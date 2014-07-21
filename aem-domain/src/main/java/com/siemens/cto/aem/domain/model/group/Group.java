package com.siemens.cto.aem.domain.model.group;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;

public class Group implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Identifier<Group> id;
    private final String name;
    private final Set<Jvm> jvms;
    private final CurrentGroupState currentState;

    public Group(final Identifier<Group> theId, final String theName) {
        this(theId, theName, Collections.<Jvm> emptySet(), GroupState.UNKNOWN, DateTime.now());
    }

    public Group(final Identifier<Group> theId, final String theName, final Set<Jvm> theJvms) {
        this(theId, theName, theJvms, GroupState.UNKNOWN, DateTime.now());
    }
    public Group(final Identifier<Group> theId, final String theName, final Set<Jvm> theJvms, GroupState theState, DateTime theAsOf) {
        id = theId;
        name = theName;
        jvms = Collections.unmodifiableSet(new HashSet<>(theJvms));
        currentState = new CurrentGroupState(theId, theState, theAsOf);
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

    public CurrentGroupState getCurrentState() {
        return currentState;
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
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("name", name)
                .append("jvms", jvms)
                .append("currentState", currentState)
                .toString();
    }

}
