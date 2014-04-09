package com.siemens.cto.aem.ws.rest.v1.provider;

import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Theories.class)
public class NameSearchParameterProviderTest {

    @Test
    public void testNameIsPresent() {

        final String name = "This Name Is Present";
        final NameSearchParameterProvider provider = new NameSearchParameterProvider(name);

        assertTrue(provider.isNamePresent());
        assertEquals(name,
                     provider.getName());
    }

    @DataPoints
    public static String[] notPresentNames() {
        return new String[] {"", "    ", null};
    }

    @Theory(nullsAccepted = true)
    public void testNameIsNotPresent(final String aPotentialName) {

        final NameSearchParameterProvider provider = new NameSearchParameterProvider(aPotentialName);

        assertFalse(provider.isNamePresent());
    }
}
