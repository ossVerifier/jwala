package com.siemens.cto.aem.domain.model.group;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Group group = (Group) o;

        if (id != null ? !id.equals(group.id) : group.id != null) {
            return false;
        }
        if (name != null ? !name.equals(group.name) : group.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Group {id=" + id + ", name=" + name + "}";
    }
}
