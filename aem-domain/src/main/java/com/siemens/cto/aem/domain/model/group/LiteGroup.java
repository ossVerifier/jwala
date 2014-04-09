package com.siemens.cto.aem.domain.model.group;

import java.io.Serializable;

import com.siemens.cto.aem.domain.model.id.Identifier;

public class LiteGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Identifier<Group> id;
    private final String name;

    public LiteGroup(final Identifier<Group> theId,
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

        final LiteGroup liteGroup = (LiteGroup) o;

        if (id != null ? !id.equals(liteGroup.id) : liteGroup.id != null) {
            return false;
        }
        if (name != null ? !name.equals(liteGroup.name) : liteGroup.name != null) {
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
        return "LiteGroup{" +
               "id=" + id +
               ", name='" + name + '\'' +
               '}';
    }
}
