package com.siemens.cto.aem.domain.model.dispatch;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupControlHistory;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.User;

public class GroupJvmDispatchCommand extends SplittableDispatchCommand {
   
    private static final long serialVersionUID = 1L;
    private final Group group;
    private final ControlGroupCommand command;
    private final User user;
    private final Identifier<GroupControlHistory> groupControlHistoryId;

    public GroupJvmDispatchCommand(Group theGroup, ControlGroupCommand theCommand, User theUser, Identifier<GroupControlHistory> theHistoryId) {
        group = theGroup;
        command = theCommand;
        user = theUser;
        groupControlHistoryId = theHistoryId;
    }
    
    public Group getGroup() {
        return group;
    }

    public ControlGroupCommand getCommand() {
        return command;
    }

    public User getUser() {
        return user;
    }

    public Identifier<GroupControlHistory> getGroupControlHistoryId() {
        return groupControlHistoryId;
    }

    @Override
    public String toString() {
        return "GroupJvmDispatchCommand [group=" + group + ", command=" + command + ", user=" + user
                + ", groupControlHistoryId=" + groupControlHistoryId + "]";
    }
    
}
