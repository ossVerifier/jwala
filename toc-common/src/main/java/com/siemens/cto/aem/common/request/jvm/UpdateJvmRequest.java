package com.siemens.cto.aem.common.request.jvm;

import com.siemens.cto.aem.common.request.Request;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.request.group.AddJvmToGroupRequest;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.common.rule.*;
import com.siemens.cto.aem.common.rule.group.GroupIdsRule;
import com.siemens.cto.aem.common.rule.jvm.JvmIdRule;
import com.siemens.cto.aem.common.rule.jvm.JvmNameRule;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class UpdateJvmRequest implements Serializable, Request {

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
    private final String newSystemProperties;

    private final Set<Identifier<Group>> groupIds;

    public UpdateJvmRequest(final Identifier<Jvm> theId,
                            final String theNewJvmName,
                            final String theNewHostName,
                            final Set<Identifier<Group>> theGroupIds,
                            final Integer theNewHttpPort,
                            final Integer theNewHttpsPort,
                            final Integer theNewRedirectPort,
                            final Integer theNewShutdownPort,
                            final Integer theNewAjpPort,
                            final Path theNewStatusPath,
                            final String theNewSystemProperties) {
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
        newSystemProperties = theNewSystemProperties;
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

    public String getNewSystemProperties() {return newSystemProperties;}

    public Set<AddJvmToGroupRequest> getAssignmentCommands() {
        return new AddJvmToGroupCommandSetBuilder(id,
                                                  groupIds).build();
    }

    public Path getNewStatusPath() {
        return newStatusPath;
    }

    @Override
    public void validate() {
        new MultipleRules(new JvmNameRule(newJvmName),
                          new HostNameRule(newHostName),
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
        UpdateJvmRequest rhs = (UpdateJvmRequest) obj;
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
                .append(this.newSystemProperties, rhs.newSystemProperties)
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
                .append(newSystemProperties)
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
                .append("newSystemProperties", newSystemProperties)
                .toString();
    }
}
