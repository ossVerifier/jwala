package com.siemens.cto.aem.common.domain.model.audit;

import com.siemens.cto.aem.common.domain.model.user.User;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class AuditUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String userId;

    public AuditUser(final User theUser) {
        userId = theUser.getId();
    }

    public AuditUser(final String theUserId) {
        this.userId = theUserId;
    }

    public String getUserId() {
        return userId;
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
        AuditUser rhs = (AuditUser) obj;
        return new EqualsBuilder()
                .append(this.userId, rhs.userId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(userId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("userId", userId)
                .toString();
    }
}
