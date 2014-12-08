package com.siemens.cto.aem.domain.model.webserver;

import java.io.Serializable;
import java.util.Collection;

import com.siemens.cto.aem.domain.model.path.FileSystemPath;
import com.siemens.cto.aem.domain.model.rule.HostNameRule;
import com.siemens.cto.aem.domain.model.rule.webserver.HttpConfigFileRule;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.path.Path;
import com.siemens.cto.aem.domain.model.rule.MultipleRules;
import com.siemens.cto.aem.domain.model.rule.PortNumberRule;
import com.siemens.cto.aem.domain.model.rule.group.GroupIdsRule;
import com.siemens.cto.aem.domain.model.rule.StatusPathRule;
import com.siemens.cto.aem.domain.model.rule.webserver.WebServerNameRule;

public class CreateWebServerCommand implements Serializable, Command {

    private static final long serialVersionUID = 1L;

    private final Collection<Identifier<Group>> groupIds;
    private final String host;
    private final String name;
    private final Integer port;
    private final Integer httpsPort;
    private final Path statusPath;
    private final FileSystemPath httpConfigFile;

    public CreateWebServerCommand(final Collection<Identifier<Group>> theGroupIds,
                                  final String theName,
                                  final String theHost,
                                  final Integer thePort,
                                  final Integer theHttpsPort,
                                  final Path theStatusPath,
                                  final FileSystemPath theHttpConfigFile) {
        host = theHost;
        port = thePort;
        httpsPort = theHttpsPort;
        name = theName;
        groupIds = theGroupIds;
        statusPath = theStatusPath;
        httpConfigFile = theHttpConfigFile;
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

    public Integer getHttpsPort() {
        return httpsPort;
    }

    public Path getStatusPath() {
        return statusPath;
    }

    public FileSystemPath getHttpConfigFile() {
        return httpConfigFile;
    }

    @Override
    public void validateCommand() {
        new MultipleRules(new WebServerNameRule(name),
                          new HostNameRule(host),
                          new PortNumberRule(port, AemFaultType.INVALID_WEBSERVER_PORT),
                          new PortNumberRule(httpsPort, AemFaultType.INVALID_WEBSERVER_HTTPS_PORT, true),
                          new GroupIdsRule(groupIds),
                          new StatusPathRule(statusPath),
                          new HttpConfigFileRule(httpConfigFile)).validate();
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
        CreateWebServerCommand rhs = (CreateWebServerCommand) obj;
        return new EqualsBuilder()
                .append(this.groupIds, rhs.groupIds)
                .append(this.host, rhs.host)
                .append(this.name, rhs.name)
                .append(this.port, rhs.port)
                .append(this.httpsPort, rhs.httpsPort)
                .append(this.statusPath, rhs.statusPath)
                .append(this.httpConfigFile, rhs.httpConfigFile)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(groupIds)
                .append(host)
                .append(name)
                .append(port)
                .append(httpsPort)
                .append(statusPath)
                .append(httpConfigFile)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("groupIds", groupIds)
                .append("host", host)
                .append("name", name)
                .append("port", port)
                .append("httpsPort", httpsPort)
                .append("statusPath", statusPath)
                .append("httpConfigFile", httpConfigFile)
                .toString();
    }
}