package com.siemens.cto.aem.domain.model.group;

import java.io.Serializable;

import com.siemens.cto.aem.domain.model.id.Identifier;

public class UpdateGroupCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Identifier<Group> id;
    private final String newName;

    public UpdateGroupCommand(final Identifier<Group> theId,
                              final String theNewName) {
        id = theId;
        newName = theNewName;
    }

    public Identifier<Group> getId() {
        return id;
    }

    public String getNewName() {
        return newName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final UpdateGroupCommand that = (UpdateGroupCommand) o;

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (newName != null ? !newName.equals(that.newName) : that.newName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (newName != null ? newName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UpdateGroupCommand{" +
               "id=" + id +
               ", newName='" + newName + '\'' +
               '}';
    }
}
