package com.cerner.jwala.control.webserver.windows;

import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.control.webserver.command.windows.WindowsWebServerNetOperation;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class WindowsWebServerNetOperationTest {

    @Test
    public void testNetOperationForEveryWebServerOperation() {

        final Set<WebServerControlOperation> missingOperationMappings = EnumSet.noneOf(WebServerControlOperation.class);

        for (final WebServerControlOperation operation : WebServerControlOperation.values()) {
            if (WindowsWebServerNetOperation.lookup(operation) == null) {
                missingOperationMappings.add(operation);
            }
        }

        assertEquals(Collections.<WebServerControlOperation>emptySet(),
                     missingOperationMappings);
    }
}
