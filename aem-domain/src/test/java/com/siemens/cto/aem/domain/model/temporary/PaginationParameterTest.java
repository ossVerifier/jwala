package com.siemens.cto.aem.domain.model.temporary;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PaginationParameterTest {

    @Test
    public void testAll() {

        assertFalse(PaginationParameter.all()
                                       .isLimited());
    }

    @Test
    public void testIsLimited() {

        final PaginationParameter limited =  new PaginationParameter(0, 12);
        final PaginationParameter unlimited = new PaginationParameter(100, 0);

        assertTrue(limited.isLimited());
        assertFalse(unlimited.isLimited());
    }
}
