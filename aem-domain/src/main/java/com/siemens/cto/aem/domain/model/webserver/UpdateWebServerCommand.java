package com.siemens.cto.aem.domain.model.webserver;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.path.Path;
import com.siemens.cto.aem.domain.model.rule.MultipleRules;
import com.siemens.cto.aem.domain.model.rule.PortNumberRule;
import com.siemens.cto.aem.domain.model.rule.group.GroupIdsRule;
import com.siemens.cto.aem.domain.model.rule.webserver.StatusPathRule;
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
    private final Integer newHttpsPort;
    private final Path newStatusPath;

    public UpdateWebServerCommand(final Identifier<WebServer> theId,
                                  final Collection<Identifier<Group>> theNewGroupIds,
                                  final String theNewName,
                                  final String theNewHost,
                                  final Integer theNewPort,
                                  final Integer theNewHttpsPort,
                                  final Path theNewStatusPath) {
        id = theId;
        newHost = theNewHost;
        newPort = theNewPort;
        newHttpsPort = theNewHttpsPort;
        newName = theNewName;
        newGroupIds = Collections.unmodifiableCollection(new HashSet<>(theNewGroupIds));
        newStatusPath = theNewStatusPath;
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

    public Integer getNewHttpsPort() {
        return newHttpsPort;
    }

    public Collection<Identifier<Group>> getNewGroupIds() {
        return newGroupIds;
    }

    public Path getNewStatusPath() {
        return newStatusPath;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        final MultipleRules mr =
                new MultipleRules(new WebServerNameRule(newName),
                                  new WebServerHostNameRule(newHost),
                                  new PortNumberRule(newPort, AemFaultType.INVALID_WEBSERVER_PORT),
                                  new PortNumberRule(newHttpsPort, AemFaultType.INVALID_WEBSERVER_PORT, true),
                                  new WebServerIdRule(id),
                                  new GroupIdsRule(newGroupIds),
                                  new StatusPathRule(newStatusPath));

        mr.validate();
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
        UpdateWebServerCommand rhs = (UpdateWebServerCommand) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .append(this.newGroupIds, rhs.newGroupIds)
                .append(this.newHost, rhs.newHost)
                .append(this.newName, rhs.newName)
                .append(this.newPort, rhs.newPort)
                .append(this.newHttpsPort, rhs.newHttpsPort)
                .append(this.newStatusPath, rhs.newStatusPath)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(newGroupIds)
                .append(newHost)
                .append(newName)
                .append(newPort)
                .append(newHttpsPort)
                .append(newStatusPath)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("newGroupIds", newGroupIds)
                .append("newHost", newHost)
                .append("newName", newName)
                .append("newPort", newPort)
                .append("newHttpsPort", newHttpsPort)
                .append("newStatusPath", newStatusPath)
                .toString();
    }
}
