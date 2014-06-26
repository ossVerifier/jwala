package com.siemens.cto.aem.domain.model.dispatch;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupCommand;
import com.siemens.cto.aem.domain.model.temporary.User;

public class GroupDispatchCommand extends SplittableDispatchCommand {
   
    private static final long serialVersionUID = 1L;
    private final Group group;
    private final ControlGroupCommand command;
    private final User user;

    public GroupDispatchCommand(Group theGroup, ControlGroupCommand theCommand, User theUser) {
        group = theGroup;
        command = theCommand;
        user = theUser;
    }
    
    public Group getGroup() {
        return group;
    }

    public ControlGroupCommand getCommand() {
        return command;
    }

    @Override public String toString() {
        return "replace-with-equals-builder";
    }

    public User getUser() {
        return user;
    }
    
}
