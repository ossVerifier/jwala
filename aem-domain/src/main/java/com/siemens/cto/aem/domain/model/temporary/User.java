package com.siemens.cto.aem.domain.model.temporary;

import java.io.Serializable;

public class User implements Serializable {

    @Deprecated
    public static User getHardCodedUser() {
        return new User("hardCodedUser");
    }

    @Deprecated
    public static User getSystemUser() {
        return new User("systemUser");
    }

    private static final long serialVersionUID = 1L;

    private final String id;

    public User(final String theId) {
        id = theId;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final User user = (User) o;

        if (id != null ? !id.equals(user.id) : user.id != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
