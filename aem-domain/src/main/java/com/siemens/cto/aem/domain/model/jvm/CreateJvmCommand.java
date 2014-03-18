package com.siemens.cto.aem.domain.model.jvm;

import java.io.Serializable;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.command.MultipleRuleCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.rule.GroupIdRule;
import com.siemens.cto.aem.domain.model.jvm.rule.HostNameRule;
import com.siemens.cto.aem.domain.model.jvm.rule.JvmNameRule;

public class CreateJvmCommand implements Serializable, Command {

    private static final long serialVersionUID = 1L;

    private final Identifier<Group> group;
    private final String jvmName;
    private final String hostName;

    public CreateJvmCommand(final Identifier<Group> theGroupId,
                            final String theName,
                            final String theHostName) {
        group = theGroupId;
        jvmName = theName;
        hostName = theHostName;
    }

    public Identifier<Group> getGroup() {
        return group;
    }

    public String getJvmName() {
        return jvmName;
    }

    public String getHostName() {
        return hostName;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        new MultipleRuleCommand(new JvmNameRule(jvmName),
                                new HostNameRule(hostName),
                                new GroupIdRule(group)).validateCommand();
    }

    public boolean isValid() {
        return isValidJvmName() && isValidHostName() && isValidGroup();
    }

    public boolean isValidJvmName() {
        return containsText(jvmName);
    }

    public boolean isValidHostName() {
        return containsText(hostName);
    }

    public boolean isValidGroup() {
        return group != null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final CreateJvmCommand that = (CreateJvmCommand) o;

        if (group != null ? !group.equals(that.group) : that.group != null) {
            return false;
        }
        if (hostName != null ? !hostName.equals(that.hostName) : that.hostName != null) {
            return false;
        }
        if (jvmName != null ? !jvmName.equals(that.jvmName) : that.jvmName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = group != null ? group.hashCode() : 0;
        result = 31 * result + (jvmName != null ? jvmName.hashCode() : 0);
        result = 31 * result + (hostName != null ? hostName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CreateJvmCommand{" +
               "group=" + group +
               ", jvmName='" + jvmName + '\'' +
               ", hostName='" + hostName + '\'' +
               '}';
    }

    protected boolean containsText(final String aValue) {
        return (aValue != null) && (!"".equals(aValue.trim()));
    }
}
