package com.siemens.cto.aem.service;

import com.siemens.cto.aem.domain.command.Command;
import org.mockito.ArgumentMatcher;

public class CommandMatcher<T> extends ArgumentMatcher<T> {

    private final Command expectedCommand;

    public CommandMatcher(final Command theExpectedCommand) {
        expectedCommand = theExpectedCommand;
    }

    @Override
    public boolean matches(final Object argument) {
        return expectedCommand.equals(argument);
    }
}
