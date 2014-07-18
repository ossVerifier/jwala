package com.siemens.cto.aem.domain.model.webserver.command;

import java.io.Serializable;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.command.GroupCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.rule.group.GroupIdRule;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlOperation;

public class ControlGroupWebServerCommand implements Serializable, GroupCommand {
    private static final long serialVersionUID = 1L;

    private final Identifier<Group> groupId;
    private final WebServerControlOperation controlOperation;

    public ControlGroupWebServerCommand(final Identifier<Group> theId, final WebServerControlOperation theControlOperation) {
        groupId = theId;
        controlOperation = theControlOperation;
    }

    public Identifier<Group> getGroupId() {
        return groupId;
    }

    public WebServerControlOperation getControlOperation() {
        return controlOperation;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        new GroupIdRule(groupId).validate();
    }

    @Override
    public String getExternalOperationName() {
        return controlOperation.getExternalValue();
    }

    @Override
    public String getType() {
        return "GroupWebServer";
    }

    @Override
    public Long getId() {
        return groupId.getId();
    }

    @Override
    public String toString() {
        return "ControlGroupWebServerCommand [groupId=" + groupId + ", controlOperation=" + controlOperation + "]";
    }

}