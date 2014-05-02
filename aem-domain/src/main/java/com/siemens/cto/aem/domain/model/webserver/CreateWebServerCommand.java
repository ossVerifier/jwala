package com.siemens.cto.aem.domain.model.webserver;

import java.io.Serializable;
import java.util.Collection;

import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.command.MultipleRuleCommand;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.rule.PortNumberRule;
import com.siemens.cto.aem.domain.model.rule.group.GroupIdsRule;
import com.siemens.cto.aem.domain.model.rule.webserver.WebServerHostNameRule;
import com.siemens.cto.aem.domain.model.rule.webserver.WebServerNameRule;

public class CreateWebServerCommand implements Serializable, Command {

    private static final long serialVersionUID = 1L;

    private final Collection<Identifier<Group>> groupIds;
    private final String host;
    private final String name;
    private final Integer port;

    public CreateWebServerCommand(final Collection<Identifier<Group>> theGroupIds, final String theName,
            final String theHost, final Integer thePort) {
        host = theHost;
        port = thePort;
        name = theName;
        groupIds = theGroupIds;
    }

    public Collection<Identifier<Group>> getGroups() {
        return groupIds;
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
    public void validateCommand() {
        new MultipleRuleCommand(new WebServerNameRule(name), new WebServerHostNameRule(host), new PortNumberRule(port,
                AemFaultType.INVALID_WEBSERVER_PORT), new GroupIdsRule(groupIds)).validateCommand();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (groupIds == null ? 0 : groupIds.hashCode());
        result = prime * result + (host == null ? 0 : host.hashCode());
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (port == null ? 0 : port.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CreateWebServerCommand other = (CreateWebServerCommand) obj;
        if (groupIds == null) {
            if (other.groupIds != null) {
                return false;
            }
        } else if (!groupIds.equals(other.groupIds)) {
            return false;
        }
        if (host == null) {
            if (other.host != null) {
                return false;
            }
        } else if (!host.equals(other.host)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (port == null) {
            if (other.port != null) {
                return false;
            }
        } else if (!port.equals(other.port)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "CreateWebServerCommand {groupIds=" + groupIds + ", host=" + host + ", name=" + name + ", port=" + port
                + "}";
    }
}
