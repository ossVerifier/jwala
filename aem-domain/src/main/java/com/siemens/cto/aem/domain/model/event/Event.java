package com.siemens.cto.aem.domain.model.event;

import java.io.Serializable;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;

public class Event<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private final T command;
	private final AuditEvent auditEvent;

	public Event(final T theCommand, final AuditEvent theEvent) {
		command = theCommand;
		auditEvent = theEvent;
	}

	public T getCommand() {
		return command;
	}

	public AuditEvent getAuditEvent() {
		return auditEvent;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final Event<?> event = (Event<?>) o;

		if (auditEvent != null ? !auditEvent.equals(event.auditEvent)
				: event.auditEvent != null) {
			return false;
		}
		if (command != null ? !command.equals(event.command)
				: event.command != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = command != null ? command.hashCode() : 0;
		result = 31 * result + (auditEvent != null ? auditEvent.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "Event{" + "command=" + command + ", auditEvent=" + auditEvent
				+ '}';
	}
}
