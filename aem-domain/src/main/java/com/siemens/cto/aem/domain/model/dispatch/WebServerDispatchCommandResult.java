package com.siemens.cto.aem.domain.model.dispatch;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlHistory;

public class WebServerDispatchCommandResult {

    private final Boolean wasSuccessful;

    private final Identifier<WebServerControlHistory> controlId;

    private final GroupWebServerDispatchCommand groupWebServerDispatchCommand;

    public WebServerDispatchCommandResult(Boolean wasSuccessful, Identifier<WebServerControlHistory> controlId,
            GroupWebServerDispatchCommand groupJvmDispatchCommand) {
        this.wasSuccessful = wasSuccessful;
        this.controlId = controlId;
        this.groupWebServerDispatchCommand = groupJvmDispatchCommand;
    }

    public Boolean wasSuccessful() {
        return wasSuccessful;
    }

    public Identifier<WebServerControlHistory> getControlId() {
        return controlId;
    }

    public GroupWebServerDispatchCommand getGroupWebServerDispatchCommand() {
        return groupWebServerDispatchCommand;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((controlId == null) ? 0 : controlId.hashCode());
        result = prime * result
                + ((groupWebServerDispatchCommand == null) ? 0 : groupWebServerDispatchCommand.hashCode());
        result = prime * result + ((wasSuccessful == null) ? 0 : wasSuccessful.hashCode());
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
        WebServerDispatchCommandResult other = (WebServerDispatchCommandResult) obj;
        if (controlId == null) {
            if (other.controlId != null) {
                return false;
            }
        } else if (!controlId.equals(other.controlId)) {
            return false;
        }
        if (groupWebServerDispatchCommand == null) {
            if (other.groupWebServerDispatchCommand != null) {
                return false;
            }
        } else if (!groupWebServerDispatchCommand.equals(other.groupWebServerDispatchCommand)) {
            return false;
        }
        if (wasSuccessful == null) {
            if (other.wasSuccessful != null) {
                return false;
            }
        } else if (!wasSuccessful.equals(other.wasSuccessful)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "WebServerDispatchCommandResult [wasSuccessful=" + wasSuccessful + ", controlId=" + controlId
                + ", groupWebServerDispatchCommand=" + groupWebServerDispatchCommand + "]";
    }

}
