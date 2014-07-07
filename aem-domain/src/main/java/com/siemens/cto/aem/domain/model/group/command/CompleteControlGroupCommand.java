package com.siemens.cto.aem.domain.model.group.command;

import java.io.Serializable;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.group.GroupControlHistory;
import com.siemens.cto.aem.domain.model.id.Identifier;

public class CompleteControlGroupCommand implements Serializable, Command {

    private static final long serialVersionUID = 1L;

    private final Identifier<GroupControlHistory> controlHistoryId;
    
    private final long groupSize;
    
    private final long successfulCommands;

    public CompleteControlGroupCommand(final Identifier<GroupControlHistory> theControlHistoryId, long theGroupSize, long theSuccessfulCommands) {
        controlHistoryId = theControlHistoryId;
        groupSize = theGroupSize;
        successfulCommands = theSuccessfulCommands;
    }

    public Identifier<GroupControlHistory> getControlHistoryId() {
        return controlHistoryId;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        //Intentionally empty
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((controlHistoryId == null) ? 0 : controlHistoryId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CompleteControlGroupCommand other = (CompleteControlGroupCommand) obj;
        if (controlHistoryId == null) {
            if (other.controlHistoryId != null)
                return false;
        } else if (!controlHistoryId.equals(other.controlHistoryId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CompleteControlGroupCommand [controlHistoryId=" + controlHistoryId + "]";
    }

    public long getGroupSize() {
        return groupSize;
    }

    public long getSuccessfulCommands() {
        return successfulCommands;
    }

}
