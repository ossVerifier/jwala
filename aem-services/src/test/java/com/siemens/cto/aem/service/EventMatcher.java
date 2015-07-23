package com.siemens.cto.aem.service;

import com.siemens.cto.aem.domain.model.event.Event;
import org.mockito.ArgumentMatcher;

public class EventMatcher<T> extends ArgumentMatcher<Event<T>> {

    private final T expectedCommand;

    public EventMatcher(final T theCommand) {
        expectedCommand = theCommand;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean matches(final Object argument) {
        final Event<T> event = (Event<T>)argument;
        return expectedCommand.equals(event.getCommand());
    }
}
