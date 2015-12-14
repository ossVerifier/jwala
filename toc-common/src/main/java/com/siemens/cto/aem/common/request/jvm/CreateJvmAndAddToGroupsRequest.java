package com.siemens.cto.aem.common.request.jvm;

import com.siemens.cto.aem.common.request.Request;
import com.siemens.cto.aem.common.request.group.AddJvmToGroupRequest;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.common.rule.group.GroupIdsRule;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CreateJvmAndAddToGroupsRequest implements Serializable, Request {

    private static final long serialVersionUID = 1L;
    private final CreateJvmRequest createCommand;
    private final Set<Identifier<Group>> groups;

    public CreateJvmAndAddToGroupsRequest(final String theName,
                                          final String theHostName,
                                          final Set<Identifier<Group>> theGroups,
                                          final Integer theHttpPort,
                                          final Integer theHttpsPort,
                                          final Integer theRedirectPort,
                                          final Integer theShutdownPort,
                                          final Integer theAjpPort,
                                          final Path theStatusPath,
                                          final String theSystemProperties) {

        createCommand = new CreateJvmRequest(theName,
                                             theHostName,
                                             theHttpPort,
                                             theHttpsPort,
                                             theRedirectPort,
                                             theShutdownPort,
                                             theAjpPort,
                                             theStatusPath,
                                             theSystemProperties);
        groups = Collections.unmodifiableSet(new HashSet<>(theGroups));
    }

    public CreateJvmRequest getCreateCommand() {
        return createCommand;
    }

    public Set<AddJvmToGroupRequest> toAddCommandsFor(final Identifier<Jvm> aJvmId) {
        return new AddJvmToGroupCommandSetBuilder(aJvmId,
                                                  groups).build();
    }

    public Set<Identifier<Group>> getGroups() {
        return groups;
    }

    @Override
    public void validate() throws BadRequestException {
        createCommand.validate();
        new GroupIdsRule(groups).validate();
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
        CreateJvmAndAddToGroupsRequest rhs = (CreateJvmAndAddToGroupsRequest) obj;
        return new EqualsBuilder()
                .append(this.createCommand, rhs.createCommand)
                .append(this.groups, rhs.groups)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(createCommand)
                .append(groups)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("createCommand", createCommand)
                .append("groups", groups)
                .toString();
    }
}
