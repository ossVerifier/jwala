package com.siemens.cto.aem.service;

import com.siemens.cto.aem.common.request.Request;
import org.mockito.ArgumentMatcher;

public class CommandMatcher<T> extends ArgumentMatcher<T> {

    private final Request expectedRequest;

    public CommandMatcher(final Request theExpectedRequest) {
        expectedRequest = theExpectedRequest;
    }

    @Override
    public boolean matches(final Object argument) {
        return expectedRequest.equals(argument);
    }
}
