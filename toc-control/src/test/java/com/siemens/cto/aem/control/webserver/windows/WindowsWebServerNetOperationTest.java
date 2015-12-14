package com.siemens.cto.aem.control.webserver.windows;

import com.siemens.cto.aem.control.webserver.command.windows.WindowsWebServerNetOperation;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerControlOperation;
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
