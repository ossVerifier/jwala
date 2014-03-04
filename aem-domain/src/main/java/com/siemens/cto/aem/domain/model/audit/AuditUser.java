package com.siemens.cto.aem.domain.model.audit;

import java.io.Serializable;

public class AuditUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String userName;

    public AuditUser(final String theUserName) {
        this.userName = theUserName;
    }

    public String getUserName() {
        return userName;
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

        if (userName != null ? !userName.equals(auditUser.userName) : auditUser.userName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return userName != null ? userName.hashCode() : 0;
    }
}
