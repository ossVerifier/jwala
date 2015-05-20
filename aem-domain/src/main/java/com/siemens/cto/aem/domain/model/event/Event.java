package com.siemens.cto.aem.domain.model.event;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;

public class Event<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private final T command;
	private final AuditEvent auditEvent;

	public Event(final T theCommand, final AuditEvent theAuditEvent) {
		command = theCommand;
		auditEvent = theAuditEvent;
	}

	public T getCommand() {
		return command;
	}

	public AuditEvent getAuditEvent() {
		return auditEvent;
	}

    public static <T> Event<T> create(T cmd, AuditEvent ae) {
        return new Event<T>(cmd, ae);
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
        Event<?> rhs = (Event<?>) obj;
        return new EqualsBuilder()
                .append(this.command, rhs.command)
                .append(this.auditEvent, rhs.auditEvent)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(command)
                .append(auditEvent)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("command", command)
                .append("auditEvent", auditEvent)
                .toString();
    }
}
