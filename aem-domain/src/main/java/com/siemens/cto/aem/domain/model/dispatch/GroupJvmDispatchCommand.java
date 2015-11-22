package com.siemens.cto.aem.domain.model.dispatch;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupJvmCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import org.apache.commons.lang3.builder.EqualsBuilder;

public class GroupJvmDispatchCommand extends DispatchCommand {
   
    private static final long serialVersionUID = 1L;
    private final Group group;
    private final ControlGroupJvmCommand command;
    private final User user;

    public GroupJvmDispatchCommand(Group theGroup, ControlGroupJvmCommand theCommand, User theUser) {
        group = theGroup;
        command = theCommand;
        user = theUser;
    }
    
    public Group getGroup() {
        return group;
    }

    public ControlGroupJvmCommand getCommand() {
        return command;
    }

    public User getUser() {
        return user;
    }


    @Override
    public String toString() {
        return "GroupJvmDispatchCommand [group=" + group + ", command=" + command + ", user=" + user + "]";
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
        GroupJvmDispatchCommand rhs = (GroupJvmDispatchCommand) obj;
        return new EqualsBuilder()
        .append(this.group, rhs.group)
        .append(this.command, rhs.command)
        .append(this.user,rhs.user)
        .isEquals();
    }
    
}
