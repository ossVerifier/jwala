package com.siemens.cto.aem.domain.model.group;

import java.io.Serializable;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;

public class CreateGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final AuditEvent creationEvent;

    public CreateGroup(final String theName,
                       final AuditEvent theCreationEvent) {
        name = theName;
        creationEvent = theCreationEvent;
    }

    public String getName() {
        return name;
    }

    public AuditEvent getCreationEvent() {
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

        final CreateGroup that = (CreateGroup) o;

        if (creationEvent != null ? !creationEvent.equals(that.creationEvent) : that.creationEvent != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (creationEvent != null ? creationEvent.hashCode() : 0);
        return result;
    }
}
