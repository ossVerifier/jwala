package com.siemens.cto.aem.domain.model.group;

import java.io.Serializable;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;

public class CreateGroupEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final CreateGroupCommand createGroupCommand;
    private final AuditEvent creationEvent;

    public CreateGroupEvent(final CreateGroupCommand theCreateGroupCommand,
                            final AuditEvent theCreationEvent) {
        createGroupCommand = theCreateGroupCommand;
        creationEvent = theCreationEvent;
    }

    public CreateGroupCommand getCreateGroupCommand() {
        return createGroupCommand;
    }

    public AuditEvent getAuditEvent() {
        return creationEvent;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final CreateGroupEvent that = (CreateGroupEvent) o;

        if (createGroupCommand != null ? !createGroupCommand.equals(that.createGroupCommand) : that.createGroupCommand != null) {
            return false;
        }
        if (creationEvent != null ? !creationEvent.equals(that.creationEvent) : that.creationEvent != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = createGroupCommand != null ? createGroupCommand.hashCode() : 0;
        result = 31 * result + (creationEvent != null ? creationEvent.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CreateGroupEvent{" +
               "createGroupCommand=" + createGroupCommand +
               ", creationEvent=" + creationEvent +
               '}';
    }
}
