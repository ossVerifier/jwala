package com.siemens.cto.aem.domain.model.event;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class Event<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private final T request;
	private final AuditEvent auditEvent;

	public Event(final T theRequest, final AuditEvent theAuditEvent) {
		request = theRequest;
		auditEvent = theAuditEvent;
	}

	public T getRequest() {
		return request;
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
                .append(this.request, rhs.request)
                .append(this.auditEvent, rhs.auditEvent)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(request)
                .append(auditEvent)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("request", request)
                .append("auditEvent", auditEvent)
                .toString();
    }
}
