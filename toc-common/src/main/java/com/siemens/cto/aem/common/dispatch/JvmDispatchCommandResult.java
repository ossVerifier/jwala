package com.siemens.cto.aem.common.dispatch;

public class JvmDispatchCommandResult {

    private final Boolean wasSuccessful;

    private final GroupJvmDispatchCommand groupJvmDispatchCommand;

    public JvmDispatchCommandResult(Boolean wasSuccessful, GroupJvmDispatchCommand groupJvmDispatchCommand) {
        this.wasSuccessful = wasSuccessful;
        this.groupJvmDispatchCommand = groupJvmDispatchCommand;
    }

    public Boolean wasSuccessful() {
        return wasSuccessful;
    }

    public GroupJvmDispatchCommand getGroupJvmDispatchCommand() {
        return groupJvmDispatchCommand;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        JvmDispatchCommandResult other = (JvmDispatchCommandResult) obj;
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
        return "JvmDispatchCommandResult [wasSuccessful=" + wasSuccessful + ", controlId=]";
    }

}
