package com.siemens.cto.toc.files;

import com.siemens.cto.aem.domain.model.resource.ResourceType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;

public interface FileManager {

    String getAbsoluteLocation(TocFile templateName) throws IOException;
    Path getTemplatePathForResourceType(ResourceType template) throws IOException;
    Collection<ResourceType> getResourceTypes() throws IOException;

    /**
     *
     * @param resourceTypeName
     * @return a string containing the text of the tempate related to the resource instance named
     */
    String getResourceTypeTemplate(String resourceTypeName);
    InputStream getResourceTypeTemplateByStream(String resourceTypeName);

    /**
     *
     * @param masterTemplateName
     * @return a string containing the text of the masters template named
     */
    String getMasterTemplate(String masterTemplateName);
    InputStream getMasterTempateByStream(String masterTemplateName);

}
