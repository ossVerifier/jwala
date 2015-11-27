package com.siemens.cto.aem.domain.command.webserver;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.command.Command;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.path.FileSystemPath;
import com.siemens.cto.aem.domain.model.path.Path;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.rule.*;
import com.siemens.cto.aem.rule.group.GroupIdsRule;
import com.siemens.cto.aem.rule.webserver.HttpConfigFileRule;
import com.siemens.cto.aem.rule.webserver.WebServerIdRule;
import com.siemens.cto.aem.rule.webserver.WebServerNameRule;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class UpdateWebServerCommand implements Serializable, Command {

    private static final long serialVersionUID = 1L;

    private final Identifier<WebServer> id;
    private final Collection<Identifier<Group>> newGroupIds; //TODO (Corey) Peter Agrees: Change this to a Set all the way down the line...
    private final String newHost;
    private final String newName;
    private final Integer newPort;
    private final Integer newHttpsPort;
    private final Path newStatusPath;
    private final Path newSvrRoot;
    private final Path newDocRoot;

    private final FileSystemPath newHttpConfigFile;

    public UpdateWebServerCommand(final Identifier<WebServer> theId,
                                  final Collection<Identifier<Group>> theNewGroupIds,
                                  final String theNewName,
                                  final String theNewHost,
                                  final Integer theNewPort,
                                  final Integer theNewHttpsPort,
                                  final Path theNewStatusPath,
                                  final FileSystemPath theNewHttpConfigFile,
                                  final Path theSvrRoot,
                                  final Path theDocRoot) {
        id = theId;
        newHost = theNewHost;
        newPort = theNewPort;
        newHttpsPort = theNewHttpsPort;
        newName = theNewName;
        newGroupIds = Collections.unmodifiableCollection(new HashSet<>(theNewGroupIds));
        newStatusPath = theNewStatusPath;
        newHttpConfigFile = theNewHttpConfigFile;
        newSvrRoot = theSvrRoot;
        newDocRoot = theDocRoot;
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

    public FileSystemPath getNewHttpConfigFile() {
        return newHttpConfigFile;
    }

    public Path getNewSvrRoot() {
        return newSvrRoot;
    }

    public Path getNewDocRoot() {
        return newDocRoot;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        final MultipleRules mr =
                new MultipleRules(new WebServerNameRule(newName),
                                  new HostNameRule(newHost),
                                  new PortNumberRule(newPort, AemFaultType.INVALID_WEBSERVER_PORT),
                                  new PortNumberRule(newHttpsPort, AemFaultType.INVALID_WEBSERVER_PORT, true),
                                  new WebServerIdRule(id),
                                  new GroupIdsRule(newGroupIds),
                                  new StatusPathRule(newStatusPath),
                                  new HttpConfigFileRule(newHttpConfigFile),
                                  new PathRule(newSvrRoot),
                                  new PathRule(newDocRoot));

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
                .append(this.newHttpConfigFile, rhs.newHttpConfigFile)
                .append(this.newSvrRoot, rhs.newSvrRoot)
                .append(this.newDocRoot, rhs.newDocRoot)
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
                .append(newHttpConfigFile)
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
                .append("newHttpConfigFile", newHttpConfigFile)
                .toString();
    }
}