package com.siemens.cto.aem.domain.command.dispatch;

public class WebServerDispatchCommandResult {

    private final Boolean wasSuccessful;

    private final GroupWebServerDispatchCommand groupWebServerDispatchCommand;

    public WebServerDispatchCommandResult(Boolean wasSuccessful, GroupWebServerDispatchCommand groupJvmDispatchCommand) {
        this.wasSuccessful = wasSuccessful;
        this.groupWebServerDispatchCommand = groupJvmDispatchCommand;
    }

    public Boolean wasSuccessful() {
        return wasSuccessful;
    }

    public GroupWebServerDispatchCommand getGroupWebServerDispatchCommand() {
        return groupWebServerDispatchCommand;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        return "WebServerDispatchCommandResult [wasSuccessful=" + wasSuccessful +
                ", groupWebServerDispatchCommand=" + groupWebServerDispatchCommand + "]";
    }

}
