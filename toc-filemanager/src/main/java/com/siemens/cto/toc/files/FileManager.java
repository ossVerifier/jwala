package com.siemens.cto.toc.files;

import java.io.IOException;
import java.io.InputStream;

public interface FileManager {

    String getAbsoluteLocation(TocFile templateName) throws IOException;

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
    InputStream getMasterTemplateByStream(String masterTemplateName);

}
