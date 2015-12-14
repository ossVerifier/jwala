package com.siemens.cto.aem.common.domain.model.fault;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class AemFaultTypeTest {

    @Test
    public void testUniquenessOfMessageCodes() {

        final Set<String> messageCodes = new HashSet<>();
        final Set<AemFaultType> duplicates = EnumSet.noneOf(AemFaultType.class);

        for (final AemFaultType faultType : AemFaultType.values()) {
            final String messageCode = faultType.getMessageCode().toLowerCase();
            if (messageCodes.contains(messageCode)) {
                duplicates.add(faultType);
            } else {
                messageCodes.add(messageCode);
            }
        }

        assertEquals(Collections.emptySet(),
                     duplicates);
    }
}
