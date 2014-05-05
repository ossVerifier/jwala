package com.siemens.cto.aem.commandprocessor.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ExecCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<String> commandFragments;

    public ExecCommand(final String... theCommandFragments) {
        this(Arrays.asList(theCommandFragments));
    }

    public ExecCommand(final List<String> theCommandFragments) {
        commandFragments = Collections.unmodifiableList(new ArrayList<>(theCommandFragments));
    }

    public List<String> getCommandFragments() {
        return commandFragments;
    }

    public String toCommandString() {
        final StringBuilder builder = new StringBuilder();
        for (final String fragment : commandFragments) {
            builder.append(fragment);
            builder.append(" ");
        }
        return builder.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ExecCommand that = (ExecCommand) o;

        if (commandFragments != null ? !commandFragments.equals(that.commandFragments) : that.commandFragments != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return commandFragments != null ? commandFragments.hashCode() : 0;
    }
}
