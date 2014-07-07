package com.siemens.cto.aem.domain.model.dispatch;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.JvmControlHistory;

public class JvmDispatchCommandResult {
    
    private final Boolean wasSuccessful;
    
    private final Identifier<JvmControlHistory> controlId;

    private final GroupJvmDispatchCommand groupJvmDispatchCommand;

    public JvmDispatchCommandResult(Boolean wasSuccessful, Identifier<JvmControlHistory> controlId, GroupJvmDispatchCommand groupJvmDispatchCommand) {
        this.wasSuccessful = wasSuccessful;
        this.controlId = controlId;
        this.groupJvmDispatchCommand = groupJvmDispatchCommand;
    }

    public Boolean wasSuccessful() {
        return wasSuccessful;
    }
    
    public Identifier<JvmControlHistory> getControlId() {
        return controlId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((controlId == null) ? 0 : controlId.hashCode());
        result = prime * result + ((wasSuccessful == null) ? 0 : wasSuccessful.hashCode());
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
        JvmDispatchCommandResult other = (JvmDispatchCommandResult) obj;
        if (controlId == null) {
            if (other.controlId != null)
                return false;
        } else if (!controlId.equals(other.controlId))
            return false;
        if (wasSuccessful == null) {
            if (other.wasSuccessful != null)
                return false;
        } else if (!wasSuccessful.equals(other.wasSuccessful))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "JvmDispatchCommandResult [wasSuccessful=" + wasSuccessful + ", controlId=" + controlId + "]";
    }

    public Boolean getWasSuccessful() {
        return wasSuccessful;
    }

    public GroupJvmDispatchCommand getGroupJvmDispatchCommand() {
        return groupJvmDispatchCommand;
    }
}
