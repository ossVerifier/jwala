package com.siemens.cto.aem.domain.model.group;

import java.io.Serializable;

import com.siemens.cto.aem.domain.model.id.Identifier;

public class Group implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Identifier<Group> id;
    private final String name;

    public Group(final Identifier<Group> theId,
                 final String theName) {
        id = theId;
        name = theName;
    }

    public Identifier<Group> getId() {
        return id;
    }

    public String getName() {
        return name;
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
}
