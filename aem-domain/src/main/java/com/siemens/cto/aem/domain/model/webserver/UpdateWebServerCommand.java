package com.siemens.cto.aem.domain.model.webserver;

import java.io.Serializable;

import com.siemens.cto.aem.domain.model.id.Identifier;

public class UpdateWebServerCommand implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Identifier<WebServer> id;
	private final String newHost;
	private final String newName;
	private final Integer newPort;

	public UpdateWebServerCommand(final Identifier<WebServer> theId,
			final String theName, final String theHost, final Integer thePort) {
		id = theId;
		newHost = theHost;
		newPort = thePort;
		newName = theName;
	}

	public Identifier<WebServer> getId() {
		return id;
	}

	public String getNewName() {
		return newName;
	}

	public String getNewHost() {
		return newHost;
	}

	public Integer getNewPort() {
		return newPort;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((newHost == null) ? 0 : newHost.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((newName == null) ? 0 : newName.hashCode());
		result = prime * result + ((newPort == null) ? 0 : newPort.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UpdateWebServerCommand other = (UpdateWebServerCommand) obj;
		if (newHost == null) {
			if (other.newHost != null)
				return false;
		} else if (!newHost.equals(other.newHost))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (newName == null) {
			if (other.newName != null)
				return false;
		} else if (!newName.equals(other.newName))
			return false;
		if (newPort == null) {
			if (other.newPort != null)
				return false;
		} else if (!newPort.equals(other.newPort))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "UpdateWebServerCommand{" + "id=" + id + ", newName='" + newName
				+ '\'' + ", newHost='" + newHost + '\'' + ", newPort='"
				+ newPort + '\'' + '}';
	}
}
