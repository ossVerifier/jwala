package com.siemens.cto.aem.domain.model.audit;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.siemens.cto.aem.domain.model.temporary.User;

public class AuditEvent implements Serializable {

    public static AuditEvent now(final User aUser) {
        return new AuditEvent(new AuditUser(aUser),
                              AuditDateTime.now());
    }

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
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        AuditEvent rhs = (AuditEvent) obj;
        return new EqualsBuilder()
                .append(this.user, rhs.user)
                .append(this.dateTime, rhs.dateTime)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(user)
                .append(dateTime)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("user", user)
                .append("dateTime", dateTime)
                .toString();
    }
}
