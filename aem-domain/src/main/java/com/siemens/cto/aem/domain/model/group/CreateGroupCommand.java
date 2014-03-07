package com.siemens.cto.aem.domain.model.group;

import java.io.Serializable;

public class CreateGroupCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String groupName;

    public CreateGroupCommand(final String theGroupName) {
        groupName = theGroupName;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final CreateGroupCommand that = (CreateGroupCommand) o;

        if (groupName != null ? !groupName.equals(that.groupName) : that.groupName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return groupName != null ? groupName.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "CreateGroupCommand{" +
               "groupName='" + groupName + '\'' +
               '}';
    }
}
