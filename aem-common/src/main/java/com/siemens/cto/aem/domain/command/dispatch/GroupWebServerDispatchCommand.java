package com.siemens.cto.aem.domain.command.dispatch;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.user.User;
import com.siemens.cto.aem.domain.command.webserver.ControlGroupWebServerCommand;
import org.apache.commons.lang3.builder.EqualsBuilder;

public class GroupWebServerDispatchCommand extends DispatchCommand {
    
    private static final long serialVersionUID = 1L;
    private final Group group;
    private final ControlGroupWebServerCommand command;
    private final User user;

    public GroupWebServerDispatchCommand(Group theGroup, ControlGroupWebServerCommand theCommand, User theUser) {
        group = theGroup;
        command = theCommand;
        user = theUser;
    }

    public Group getGroup() {
        return group;
    }

    public ControlGroupWebServerCommand getCommand() {
        return command;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "GroupWebServerDispatchCommand [group=" + group + ", command=" + command + ", user=" + user + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((command == null) ? 0 : command.hashCode());
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        result = prime * result + ((user == null) ? 0 : user.hashCode());
        return result;
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
        GroupWebServerDispatchCommand rhs = (GroupWebServerDispatchCommand) obj;
        return new EqualsBuilder()
        .append(this.command, rhs.command)
        .append(this.group, rhs.group)
        .append(this.user,rhs.user)
        .isEquals();
    }

}
