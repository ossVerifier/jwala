package com.siemens.cto.aem.domain.model.group;

import java.io.Serializable;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.id.Identifier;

public class UpdateGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Identifier<Group> id;
    private final String name;
    private final AuditEvent updateEvent;

    public UpdateGroup(final Identifier<Group> theNewId,
                       final String theNewName,
                       final AuditEvent theUpdateEvent) {
        id = theNewId;
        name = theNewName;
        updateEvent = theUpdateEvent;
    }

    public Identifier<Group> getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public AuditEvent getUpdateEvent() {
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

        final UpdateGroup that = (UpdateGroup) o;

        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (updateEvent != null ? !updateEvent.equals(that.updateEvent) : that.updateEvent != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (updateEvent != null ? updateEvent.hashCode() : 0);
        return result;
    }
}
