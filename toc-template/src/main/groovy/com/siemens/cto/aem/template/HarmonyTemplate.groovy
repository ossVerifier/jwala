package com.siemens.cto.aem.template

import com.siemens.cto.aem.domain.model.resource.ResourceType
import java.nio.file.Path

class HarmonyTemplate {
    
    private final def wrappedTemplate;
    private final Path fileSystemPath;
    private final HarmonyTemplateEngine owner;
    
    public HarmonyTemplate(theTemplate, thePath, theOwner) { 
        wrappedTemplate = theTemplate;
        owner = theOwner;
        fileSystemPath = thePath;
    }

    public void check() {
        owner.checkOnly fileSystemPath
    }
}

