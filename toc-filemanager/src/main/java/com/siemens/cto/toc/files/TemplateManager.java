package com.siemens.cto.toc.files;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import com.siemens.cto.aem.domain.model.resource.ResourceType;

public interface TemplateManager {

    String getAbsoluteLocation(TocFile templateName) throws IOException;
    Path getTemplatePathForResourceType(ResourceType template) throws IOException;
    Collection<ResourceType> getResourceTypes() throws IOException;

    /**
     *
     * @param resourceTypeName
     * @return a string containing the text of the tempate related to the resource instance named
     */
    String getResourceTypeTemplate(String resourceTypeName);

    /**
     *
     * @param masterTemplateName
     * @return a string containing the text of the master template named
     */
    String getMasterTemplate(String masterTemplateName);

}
