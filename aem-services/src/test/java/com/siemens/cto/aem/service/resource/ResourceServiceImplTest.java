package com.siemens.cto.aem.service.resource;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;

import com.siemens.cto.aem.service.resource.impl.ResourceServiceImpl;
import com.siemens.cto.aem.template.HarmonyTemplateEngine;
import com.siemens.cto.toc.files.TemplateManager;

public class ResourceServiceImplTest {

    ResourceService cut = new ResourceServiceImpl(Mockito.mock(TemplateManager.class), Mockito.mock(HarmonyTemplateEngine.class));
    
    @Test
    public void testEncryption() { 
        assertEquals("sr94UX5Zuw7QBM992+lAvQ==", cut.encryptUsingPlatformBean("hello"));
    }
}
