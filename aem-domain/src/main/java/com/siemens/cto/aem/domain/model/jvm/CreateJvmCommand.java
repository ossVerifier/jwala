package com.siemens.cto.aem.domain.model.jvm;

import java.io.Serializable;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.command.MultipleRuleCommand;
import com.siemens.cto.aem.domain.model.rule.jvm.JvmHostNameRule;
import com.siemens.cto.aem.domain.model.rule.jvm.JvmNameRule;

public class CreateJvmCommand implements Serializable, Command {

    private static final long serialVersionUID = 1L;

    private final String jvmName;
    private final String hostName;

    public CreateJvmCommand(final String theName,
                            final String theHostName) {
        jvmName = theName;
        hostName = theHostName;
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
                                new JvmHostNameRule(hostName)).validateCommand();
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
        int result = jvmName != null ? jvmName.hashCode() : 0;
        result = 31 * result + (hostName != null ? hostName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CreateJvmCommand{" +
               "jvmName='" + jvmName + '\'' +
               ", hostName='" + hostName + '\'' +
               '}';
    }
}
