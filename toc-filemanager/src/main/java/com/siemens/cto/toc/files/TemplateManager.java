package com.siemens.cto.toc.files;

import java.io.IOException;
import java.util.Collection;

import com.siemens.cto.aem.domain.model.resource.ResourceType;

public interface TemplateManager {

    String getAbsoluteLocation(TocFile templateName) throws IOException;
    Collection<ResourceType> getResourceTypes() throws IOException;
    
}
