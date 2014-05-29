package com.siemens.cto.aem.domain.model.webserver;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.rule.MultipleRules;
import com.siemens.cto.aem.domain.model.rule.PortNumberRule;
import com.siemens.cto.aem.domain.model.rule.group.GroupIdsRule;
import com.siemens.cto.aem.domain.model.rule.webserver.WebServerHostNameRule;
import com.siemens.cto.aem.domain.model.rule.webserver.WebServerIdRule;
import com.siemens.cto.aem.domain.model.rule.webserver.WebServerNameRule;

public class UpdateWebServerCommand implements Serializable, Command {

    private static final long serialVersionUID = 1L;

    private final Identifier<WebServer> id;
    private final Collection<Identifier<Group>> newGroupIds; //TODO Change this to a Set all the way down the line...
    private final String newHost;
    private final String newName;
    private final Integer newPort;

    public UpdateWebServerCommand(final Identifier<WebServer> theId,
            final Collection<Identifier<Group>> theNewGroupIds, final String theNewName, final String theNewHost,
            final Integer theNewPort) {
        id = theId;
        newHost = theNewHost;
        newPort = theNewPort;
        newName = theNewName;
        newGroupIds = Collections.unmodifiableCollection(new HashSet<>(theNewGroupIds));
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

    public Collection<Identifier<Group>> getNewGroupIds() {
        return newGroupIds;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        final MultipleRules mr =
                new MultipleRules(new WebServerNameRule(newName), new WebServerHostNameRule(newHost),
                        new PortNumberRule(newPort, AemFaultType.INVALID_WEBSERVER_PORT), new WebServerIdRule(id),
                        new GroupIdsRule(newGroupIds));

        mr.validate();
    }

    @Override
    public String toString() {
        return "UpdateWebServerCommand {id=" + id + ", groupIds=" + newGroupIds + ", newHost=" + newHost + ", newName="
                + newName + ", newPort=" + newPort + "}";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (id == null ? 0 : id.hashCode());
        result = prime * result + (newGroupIds == null ? 0 : newGroupIds.hashCode());
        result = prime * result + (newHost == null ? 0 : newHost.hashCode());
        result = prime * result + (newName == null ? 0 : newName.hashCode());
        result = prime * result + (newPort == null ? 0 : newPort.hashCode());
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
        final UpdateWebServerCommand other = (UpdateWebServerCommand) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (newGroupIds == null) {
            if (other.newGroupIds != null) {
                return false;
            }
        } else if (!newGroupIds.equals(other.newGroupIds)) {
            return false;
        }
        if (newHost == null) {
            if (other.newHost != null) {
                return false;
            }
        } else if (!newHost.equals(other.newHost)) {
            return false;
        }
        if (newName == null) {
            if (other.newName != null) {
                return false;
            }
        } else if (!newName.equals(other.newName)) {
            return false;
        }
        if (newPort == null) {
            if (other.newPort != null) {
                return false;
            }
        } else if (!newPort.equals(other.newPort)) {
            return false;
        }
        return true;
    }

}
