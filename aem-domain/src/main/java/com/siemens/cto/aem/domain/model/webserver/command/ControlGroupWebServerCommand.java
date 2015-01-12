package com.siemens.cto.aem.domain.model.webserver.command;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;

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

    public ControlGroupWebServerCommand(final Identifier<Group> theId,
            final WebServerControlOperation theControlOperation) {
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((controlOperation == null) ? 0 : controlOperation.hashCode());
        result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ControlGroupWebServerCommand rhs = (ControlGroupWebServerCommand) obj;
        return new EqualsBuilder()
        .append(this.controlOperation, rhs.controlOperation)
        .append(this.groupId, rhs.groupId)
        .isEquals();
    }

}