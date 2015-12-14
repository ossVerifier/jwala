package com.siemens.cto.aem.control.jvm.windows;

import com.siemens.cto.aem.control.jvm.command.windows.WindowsJvmNetOperation;
import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class WindowsJvmNetOperationTest {

    @Test
    public void testNetOperationForEveryJvmOperation() {

        final Set<JvmControlOperation> missingOperationMappings = EnumSet.noneOf(JvmControlOperation.class);

        for (final JvmControlOperation operation : JvmControlOperation.values()) {
            if (WindowsJvmNetOperation.lookup(operation) == null) {
                missingOperationMappings.add(operation);
            }
        }

        assertEquals(Collections.<JvmControlOperation>emptySet(),
                     missingOperationMappings);
    }
}
