package com.siemens.cto.aem.service.state.impl;

import com.siemens.cto.aem.service.state.InMemoryStateManagerService;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link InMemoryStateManagerServiceImpl}.
 *
 * Created by JC043760 on 4/18/2016.
 */
public class InMemoryStateManagerServiceImplTest {

    private InMemoryStateManagerService<String, String> inMemoryStateManagerService =
            new InMemoryStateManagerServiceImpl<>();

    @Test
    public void testAll() {
        inMemoryStateManagerService.put("key",  "val");
        assertTrue(inMemoryStateManagerService.containsKey("key"));
        assertEquals(inMemoryStateManagerService.get("key"), "val");
        inMemoryStateManagerService.remove("key");
        assertFalse(inMemoryStateManagerService.containsKey("key"));
    }
}
