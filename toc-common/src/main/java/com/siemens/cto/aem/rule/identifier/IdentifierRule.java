package com.siemens.cto.aem.rule.identifier;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.MessageResponseStatus;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.rule.Rule;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class IdentifierRule<T> implements Rule {

    private final Identifier<T> id;
    private final MessageResponseStatus messageResponseStatus;
    private final String message;

    public IdentifierRule(final Identifier<T> theId,
                          final MessageResponseStatus theMessageResponseStatus,
                          final String theMessage) {
        id = theId;
        messageResponseStatus = theMessageResponseStatus;
        message = theMessage;
    }

    @Override
    public boolean isValid() {
        return id != null && id.getId() != null;
    }

    @Override
    public void validate() throws BadRequestException {
        if (!isValid()) {
            throw new BadRequestException(messageResponseStatus,
                                          message);
        }
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
        IdentifierRule rhs = (IdentifierRule) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .append(this.messageResponseStatus, rhs.messageResponseStatus)
                .append(this.message, rhs.message)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(messageResponseStatus)
                .append(message)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("messageResponseStatus", messageResponseStatus)
                .append("message", message)
                .toString();
    }
}
