package com.siemens.cto.aem.domain.model.webserver;

import java.io.Serializable;

import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.command.MultipleRuleCommand;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.rule.GroupIdRule;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.rule.JvmNameRule;
import com.siemens.cto.aem.domain.model.rule.HostNameRule;
import com.siemens.cto.aem.domain.model.rule.PortNumberRule;

public class CreateWebServerCommand implements Serializable, Command {

	private static final long serialVersionUID = 1L;

    private final Identifier<Group> group;
	private final String host;
	private final String name;
	private final Integer port;

	public CreateWebServerCommand(
			final Identifier<Group> theGroupId, 
			final String theName, final String theHost, final Integer thePort) {
		host = theHost;
		port = thePort;
		name = theName;
		group = theGroupId;
	}

	public Identifier<Group> getGroup() {
		return group;
	}

	public String getName() {
		return name;
	}

	public String getHost() {
		return host;
	}

	public Integer getPort() {
		return port;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
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
		CreateWebServerCommand other = (CreateWebServerCommand) obj;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (port == null) {
			if (other.port != null)
				return false;
		} else if (!port.equals(other.port))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CreateWebServerCommand {newGroup=" + group + ", newHost="
				+ host + ", newName=" + name + ", newPort=" + port
				+ "}";
	}

	public void validateCommand() {
        new MultipleRuleCommand(new JvmNameRule(name),
                new HostNameRule(host),
                new PortNumberRule(port, AemFaultType.INVALID_WEBSERVER_PORT ),
                new GroupIdRule(group)).validateCommand();
	}

}
