package com.siemens.cto.aem.domain.model.webserver;

import java.io.Serializable;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.command.MultipleRuleCommand;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.rule.GroupIdRule;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.rule.HostNameRule;
import com.siemens.cto.aem.domain.model.rule.PortNumberRule;
import com.siemens.cto.aem.domain.model.webserver.rule.WebServerIdRule;
import com.siemens.cto.aem.domain.model.webserver.rule.WebServerNameRule;

public class UpdateWebServerCommand implements Serializable, Command {

	private static final long serialVersionUID = 1L;

	private final Identifier<WebServer> id;
    private final Identifier<Group> newGroup;
	private final String newHost;
	private final String newName;
	private final Integer newPort;

	public UpdateWebServerCommand(final Identifier<WebServer> theId,
			final Identifier<Group> theNewGroupId, 
			final String theNewName, final String theNewHost, final Integer theNewPort) {
		id = theId;
		newHost = theNewHost;
		newPort = theNewPort;
		newName = theNewName;
		newGroup = theNewGroupId;
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

	public Identifier<Group> getNewGroup() {
		return newGroup;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((newGroup == null) ? 0 : newGroup.hashCode());
		result = prime * result + ((newHost == null) ? 0 : newHost.hashCode());
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
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (newGroup == null) {
			if (other.newGroup != null)
				return false;
		} else if (!newGroup.equals(other.newGroup))
			return false;
		if (newHost == null) {
			if (other.newHost != null)
				return false;
		} else if (!newHost.equals(other.newHost))
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
		return "UpdateWebServerCommand {id=" + id + ", newGroup=" + newGroup
				+ ", newHost=" + newHost + ", newName=" + newName
				+ ", newPort=" + newPort + "}";
	}

    @Override
    public void validateCommand() throws BadRequestException {
        new MultipleRuleCommand(new WebServerNameRule(newName),
                                new HostNameRule(newHost, AemFaultType.INVALID_WEBSERVER_HOST),
                                new GroupIdRule(newGroup),
                                new PortNumberRule(newPort, AemFaultType.INVALID_WEBSERVER_PORT),
                                new WebServerIdRule(id)).validateCommand();
    }

}
