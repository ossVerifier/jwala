package com.siemens.cto.aem.domain.model.group;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;

public class Group implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Identifier<Group> id;
    private final String name;
    private final Set<Jvm> jvms;

    public Group(final Identifier<Group> theId, final String theName) {
        this(theId, theName, Collections.<Jvm> emptySet());
    }

    public Group(final Identifier<Group> theId, final String theName, final Set<Jvm> theJvms) {
        id = theId;
        name = theName;
        jvms = Collections.unmodifiableSet(new HashSet<>(theJvms));
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
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(name)
                .append(jvms)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("name", name)
                .append("jvms", jvms)
                .toString();
    }
}
