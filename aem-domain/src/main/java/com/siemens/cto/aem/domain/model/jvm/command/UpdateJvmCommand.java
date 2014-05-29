package com.siemens.cto.aem.domain.model.jvm.command;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.group.AddJvmToGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.rule.MultipleRules;
import com.siemens.cto.aem.domain.model.rule.group.GroupIdsRule;
import com.siemens.cto.aem.domain.model.rule.jvm.JvmHostNameRule;
import com.siemens.cto.aem.domain.model.rule.jvm.JvmIdRule;
import com.siemens.cto.aem.domain.model.rule.jvm.JvmNameRule;

public class UpdateJvmCommand implements Serializable, Command {

    private static final long serialVersionUID = 1L;

    private final Identifier<Jvm> id;
    private final String newJvmName;
    private final String newHostName;
    private final Set<Identifier<Group>> groupIds;

    public UpdateJvmCommand(final Identifier<Jvm> theId,
                            final String theNewJvmName,
                            final String theNewHostName,
                            final Set<Identifier<Group>> theGroupIds) {
        id = theId;
        newJvmName = theNewJvmName;
        newHostName = theNewHostName;
        groupIds = Collections.unmodifiableSet(new HashSet<>(theGroupIds));
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

    public Set<AddJvmToGroupCommand> getAssignmentCommands() {
        return new AddJvmToGroupCommandSetBuilder(id,
                                                  groupIds).build();
    }

    @Override
    public void validateCommand() throws BadRequestException {
        new MultipleRules(new JvmNameRule(newJvmName),
                                new JvmHostNameRule(newHostName),
                                new JvmIdRule(id),
                                new GroupIdsRule(groupIds)).validate();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final UpdateJvmCommand that = (UpdateJvmCommand) o;

        if (groupIds != null ? !groupIds.equals(that.groupIds) : that.groupIds != null) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (newHostName != null ? !newHostName.equals(that.newHostName) : that.newHostName != null) {
            return false;
        }
        if (newJvmName != null ? !newJvmName.equals(that.newJvmName) : that.newJvmName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (newJvmName != null ? newJvmName.hashCode() : 0);
        result = 31 * result + (newHostName != null ? newHostName.hashCode() : 0);
        result = 31 * result + (groupIds != null ? groupIds.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UpdateJvmCommand{" +
               "id=" + id +
               ", newJvmName='" + newJvmName + '\'' +
               ", newHostName='" + newHostName + '\'' +
               ", groupIds=" + groupIds +
               '}';
    }
}
