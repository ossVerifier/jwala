package com.siemens.cto.aem.domain.model.audit;

import java.io.Serializable;

public class AuditEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final AuditUser user;
    private final AuditDateTime dateTime;

    public AuditEvent(final AuditUser theUser,
                      final AuditDateTime theDateTime) {
        user = theUser;
        dateTime = theDateTime;
    }

    public AuditUser getUser() {
        return user;
    }

    public AuditDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AuditEvent that = (AuditEvent) o;

        if (dateTime != null ? !dateTime.equals(that.dateTime) : that.dateTime != null) {
            return false;
        }
        if (user != null ? !user.equals(that.user) : that.user != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = user != null ? user.hashCode() : 0;
        result = 31 * result + (dateTime != null ? dateTime.hashCode() : 0);
        return result;
    }
}
