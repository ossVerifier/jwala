package com.siemens.cto.aem.domain.model.audit;

import java.io.Serializable;

public class AuditUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String userId;

    public AuditUser(final String theUserId) {
        this.userId = theUserId;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AuditUser auditUser = (AuditUser) o;

        if (userId != null ? !userId.equals(auditUser.userId) : auditUser.userId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }
}
