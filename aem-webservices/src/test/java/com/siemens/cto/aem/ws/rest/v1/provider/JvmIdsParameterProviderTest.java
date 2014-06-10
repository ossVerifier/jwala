package com.siemens.cto.aem.ws.rest.v1.provider;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JvmIdsParameterProviderTest {

    @Test
    public void testNoneExpected() throws Exception {
        final JvmIdsParameterProvider provider = new JvmIdsParameterProvider();
        final Set<Identifier<Jvm>> actualIds = provider.valueOf();
        assertTrue(actualIds.isEmpty());
    }

    @Test
    public void testSomeExpected() throws Exception {
        final int numberOfIds = 20;
        final Set<String> expectedStringIds = constructIds(numberOfIds);
        final JvmIdsParameterProvider provider = new JvmIdsParameterProvider(expectedStringIds);
        final Set<Identifier<Jvm>> actualIds = provider.valueOf();
        assertEquals(numberOfIds,
                     actualIds.size());
        for (final Identifier<Jvm> id : actualIds) {
            assertTrue(expectedStringIds.contains(id.getId().toString()));
        }
    }

    @Test(expected = BadRequestException.class)
    public void testBadIds() throws Exception {
        final Set<String> badStringIds = new HashSet<>(Arrays.asList("not a number", "also not a number", "99"));
        final JvmIdsParameterProvider provider = new JvmIdsParameterProvider(badStringIds);
        final Set<Identifier<Jvm>> actualIds = provider.valueOf();
    }

    private Set<String> constructIds(final int aNumberToCreate) {
        final Set<String> ids = new HashSet<>(aNumberToCreate);
        for (int i = 0; i < aNumberToCreate; i++) {
            ids.add(String.valueOf(i + 1));
        }
        return ids;
    }
}
