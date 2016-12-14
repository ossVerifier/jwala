package com.cerner.jwala.common.domain.model.fault;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class AemFaultTypeTest {

    @Test
    public void testUniquenessOfMessageCodes() {

        final Set<String> messageCodes = new HashSet<>();
        final Set<AemFaultType> duplicates = EnumSet.noneOf(AemFaultType.class);

        for (final AemFaultType faultType : AemFaultType.values()) {
            final String messageCode = faultType.getMessageCode().toLowerCase(Locale.US);
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
