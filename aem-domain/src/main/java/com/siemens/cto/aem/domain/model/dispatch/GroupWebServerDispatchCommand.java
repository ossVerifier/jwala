package com.siemens.cto.aem.domain.model.dispatch;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupControlHistory;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.command.ControlGroupWebServerCommand;

public class GroupWebServerDispatchCommand extends DispatchCommand {
    
    private static final long serialVersionUID = 1L;
    private final Group group;
    private final ControlGroupWebServerCommand command;
    private final User user;
    private final Identifier<GroupControlHistory> groupControlHistoryId;

    public GroupWebServerDispatchCommand(Group theGroup, ControlGroupWebServerCommand theCommand, User theUser,
            Identifier<GroupControlHistory> theHistoryId) {
        group = theGroup;
        command = theCommand;
        user = theUser;
        groupControlHistoryId = theHistoryId;
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

    public Identifier<GroupControlHistory> getGroupControlHistoryId() {
        return groupControlHistoryId;
    }
    
    @Override
    public String toString() {
        return "GroupWebServerDispatchCommand [group=" + group + ", command=" + command + ", user=" + user
                + ", groupControlHistoryId=" + groupControlHistoryId + "]";
    }

}
