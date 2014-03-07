package com.siemens.cto.aem.domain.model.group;

import java.io.Serializable;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;

public class UpdateGroupEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UpdateGroupCommand updateGroupCommand;
    private final AuditEvent updateEvent;

    public UpdateGroupEvent(final UpdateGroupCommand theUpdateGroupCommand,
                            final AuditEvent theUpdateEvent) {
        updateGroupCommand = theUpdateGroupCommand;
        updateEvent = theUpdateEvent;
    }

    public UpdateGroupCommand getUpdateGroupCommand() {
        return updateGroupCommand;
    }

    public AuditEvent getAuditEvent() {
        return updateEvent;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final UpdateGroupEvent that = (UpdateGroupEvent) o;

        if (updateEvent != null ? !updateEvent.equals(that.updateEvent) : that.updateEvent != null) {
            return false;
        }
        if (updateGroupCommand != null ? !updateGroupCommand.equals(that.updateGroupCommand) : that.updateGroupCommand != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = updateGroupCommand != null ? updateGroupCommand.hashCode() : 0;
        result = 31 * result + (updateEvent != null ? updateEvent.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UpdateGroupEvent{" +
               "updateGroupCommand=" + updateGroupCommand +
               ", updateEvent=" + updateEvent +
               '}';
    }
}
