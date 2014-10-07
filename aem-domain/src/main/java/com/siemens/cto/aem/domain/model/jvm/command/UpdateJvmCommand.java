package com.siemens.cto.aem.domain.model.jvm.command;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.AddJvmToGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.path.Path;
import com.siemens.cto.aem.domain.model.rule.MultipleRules;
import com.siemens.cto.aem.domain.model.rule.PortNumberRule;
import com.siemens.cto.aem.domain.model.rule.ShutdownPortNumberRule;
import com.siemens.cto.aem.domain.model.rule.group.GroupIdsRule;
import com.siemens.cto.aem.domain.model.rule.jvm.JvmHostNameRule;
import com.siemens.cto.aem.domain.model.rule.jvm.JvmIdRule;
import com.siemens.cto.aem.domain.model.rule.jvm.JvmNameRule;
import com.siemens.cto.aem.domain.model.rule.webserver.StatusPathRule;

public class UpdateJvmCommand implements Serializable, Command {

    private static final long serialVersionUID = 1L;

    private final Identifier<Jvm> id;
    private final String newJvmName;
    private final String newHostName;
    private final Integer newHttpPort;
    private final Integer newHttpsPort;
    private final Integer newRedirectPort;
    private final Integer newShutdownPort;
    private final Integer newAjpPort;
    private final Path newStatusPath;

    private final Set<Identifier<Group>> groupIds;

    public UpdateJvmCommand(final Identifier<Jvm> theId,
                            final String theNewJvmName,
                            final String theNewHostName,
                            final Set<Identifier<Group>> theGroupIds,
                            final Integer theNewHttpPort,
                            final Integer theNewHttpsPort,
                            final Integer theNewRedirectPort,
                            final Integer theNewShutdownPort,
                            final Integer theNewAjpPort,
                            final Path theNewStatusPath) {
        id = theId;
        newJvmName = theNewJvmName;
        newHostName = theNewHostName;
        groupIds = Collections.unmodifiableSet(new HashSet<>(theGroupIds));
        newHttpPort = theNewHttpPort;
        newHttpsPort = theNewHttpsPort;
        newRedirectPort = theNewRedirectPort;
        newShutdownPort = theNewShutdownPort;
        newAjpPort = theNewAjpPort;
        newStatusPath = theNewStatusPath;
    }

    public Identifier<Jvm> getId() {
        return id;
    }

    public String getNewJvmName() {
        return newJvmName;
    }

    public String getNewHostName() {
        return newHostName;
    }

    public Integer getNewHttpPort() {
        return newHttpPort;
    }

    public Integer getNewHttpsPort() {
        return newHttpsPort;
    }

    public Integer getNewRedirectPort() {
        return newRedirectPort;
    }

    public Integer getNewShutdownPort() {
        return newShutdownPort;
    }

    public Integer getNewAjpPort() {
        return newAjpPort;
    }

    public Set<AddJvmToGroupCommand> getAssignmentCommands() {
        return new AddJvmToGroupCommandSetBuilder(id,
                                                  groupIds).build();
    }

    public Path getNewStatusPath() {
        return newStatusPath;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        new MultipleRules(new JvmNameRule(newJvmName),
                          new JvmHostNameRule(newHostName),
                          new StatusPathRule(newStatusPath),
                          new JvmIdRule(id),
                          new GroupIdsRule(groupIds),
                          new PortNumberRule(newHttpPort, AemFaultType.INVALID_JVM_HTTP_PORT),
                          new PortNumberRule(newHttpsPort, AemFaultType.INVALID_JVM_HTTPS_PORT, true),
                          new PortNumberRule(newRedirectPort, AemFaultType.INVALID_JVM_REDIRECT_PORT),
                          new ShutdownPortNumberRule(newShutdownPort, AemFaultType.INVALID_JVM_SHUTDOWN_PORT),
                          new PortNumberRule(newAjpPort, AemFaultType.INVALID_JVM_AJP_PORT)).validate();
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
        UpdateJvmCommand rhs = (UpdateJvmCommand) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .append(this.newJvmName, rhs.newJvmName)
                .append(this.newHostName, rhs.newHostName)
                .append(this.newHttpPort, rhs.newHttpPort)
                .append(this.newHttpsPort, rhs.newHttpsPort)
                .append(this.newRedirectPort, rhs.newRedirectPort)
                .append(this.newShutdownPort, rhs.newShutdownPort)
                .append(this.newAjpPort, rhs.newAjpPort)
                .append(this.groupIds, rhs.groupIds)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(newJvmName)
                .append(newHostName)
                .append(newHttpPort)
                .append(newHttpsPort)
                .append(newRedirectPort)
                .append(newShutdownPort)
                .append(newAjpPort)
                .append(groupIds)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("newJvmName", newJvmName)
                .append("newHostName", newHostName)
                .append("newHttpPort", newHttpPort)
                .append("newHttpsPort", newHttpsPort)
                .append("newRedirectPort", newRedirectPort)
                .append("newShutdownPort", newShutdownPort)
                .append("newAjpPort", newAjpPort)
                .append("groupIds", groupIds)
                .toString();
    }
}
