package com.cerner.jwala.windows;

import com.cerner.jwala.common.domain.model.binarydistribution.BinaryDistributionControlOperation;
import com.cerner.jwala.control.command.windows.WindowsBinaryDistributionNetOperation;
import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class WindowsBinaryDistributionNetOperationTest {

    @Test
    public void testNetOperationForEveryBinaryDistributionOperation() {

        final Set<BinaryDistributionControlOperation> missingOperationMappings = EnumSet.noneOf(BinaryDistributionControlOperation.class);

        for (final BinaryDistributionControlOperation operation : BinaryDistributionControlOperation.values()) {
            if (WindowsBinaryDistributionNetOperation.lookup(operation) == null) {
                missingOperationMappings.add(operation);
            }
        }

        assertEquals(Collections.<BinaryDistributionControlOperation>emptySet(),
                missingOperationMappings);
    }
}
