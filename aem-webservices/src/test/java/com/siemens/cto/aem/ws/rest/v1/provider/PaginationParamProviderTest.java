package com.siemens.cto.aem.ws.rest.v1.provider;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public class PaginationParamProviderTest {

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @DataPoints
    public static String[] invalidParameters = new String[] {"notanumber", "-1", null, "-2"};

    @Theory(nullsAccepted = true)
    public void testConvertBadParameters(final String anOffset,
                                         final String aLimit) {

        assumeNotNull(anOffset);
        assumeNotNull(aLimit);
        expected.expect(BadRequestException.class);

        final PaginationParamProvider provider = new PaginationParamProvider(anOffset,
                                                                             aLimit);

        assertTrue(provider.areParametersPresent());

        final PaginationParameter parameter = provider.getPaginationParameter();
    }

    @Theory(nullsAccepted = true)
    public void testAtLeastOneParameterMissing(final String anOffset,
                                               final String aLimit) {

        assumeTrue((anOffset == null) || (aLimit == null));

        final PaginationParamProvider provider = new PaginationParamProvider(anOffset,
                                                                             aLimit);
        assertFalse(provider.areParametersPresent());
        assertFalse(provider.isRetrieveAllParameterPresent());

        final PaginationParameter parameter = provider.getPaginationParameter();
        assertTrue(parameter.isLimited());
    }

    @Theory(nullsAccepted = true)
    public void testAtLeastOneParameterMissingButAllRequested(final String anOffset,
                                                              final String aLimit) {

        assumeTrue((anOffset == null) || (aLimit == null));

        final PaginationParamProvider provider = new PaginationParamProvider(anOffset,
                                                                             aLimit,
                                                                             "requested");

        assertFalse(provider.areParametersPresent());
        assertTrue(provider.isRetrieveAllParameterPresent());

        final PaginationParameter parameter = provider.getPaginationParameter();

        assertNotNull(parameter);
        assertFalse(parameter.isLimited());
    }

    @Test
    public void testConvertGoodParameters() {

        final PaginationParameter expectedParameter = new PaginationParameter(1,
                                                                              1);

        final PaginationParamProvider provider = new PaginationParamProvider(expectedParameter.getOffset().toString(),
                                                                             expectedParameter.getLimit().toString());

        assertTrue(provider.areParametersPresent());

        final PaginationParameter actualParameter = provider.getPaginationParameter();

        assertEquals(expectedParameter,
                     actualParameter);
    }

    @Test
    public void testAllRequested() {

        final PaginationParamProvider provider = new PaginationParamProvider("I should retrieve all");

        assertFalse(provider.areParametersPresent());
        assertTrue(provider.isRetrieveAllParameterPresent());

        final PaginationParameter parameter = provider.getPaginationParameter();

        assertFalse(parameter.isLimited());
    }
}
