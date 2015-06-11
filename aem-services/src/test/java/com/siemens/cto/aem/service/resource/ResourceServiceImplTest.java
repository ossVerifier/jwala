package com.siemens.cto.aem.service.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.siemens.cto.aem.persistence.service.group.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.resource.ResourcePersistenceService;
import org.junit.Test;
import org.mockito.Mockito;

import com.siemens.cto.aem.service.resource.impl.ResourceServiceImpl;
import com.siemens.cto.aem.template.HarmonyTemplateEngine;
import com.siemens.cto.toc.files.FileManager;

public class ResourceServiceImplTest {

    ResourceService cut = new ResourceServiceImpl(Mockito.mock(FileManager.class), Mockito.mock(HarmonyTemplateEngine.class), Mockito.mock(ResourcePersistenceService.class), Mockito.mock(GroupPersistenceService.class));
    
    @Test
    public void testEncryption() { 
        assertEquals("sr94UX5Zuw7QBM992+lAvQ==", cut.encryptUsingPlatformBean("hello"));
    }

    @Test
    public void testCreate() {
        assertNotNull("");
    }
    @Test
    public void testUpdateAttributes() {
        assertNotNull("");
    }
    @Test
    public void testUpdateFriendlyName() {
        assertNotNull("");
    }
    @Test
    public void testDelete() {
        assertNotNull("");
    }
    @Test
    public void testRead() {
        assertNotNull("");
    }
    @Test
    public void getType() {
        assertNotNull("");
    }
}
