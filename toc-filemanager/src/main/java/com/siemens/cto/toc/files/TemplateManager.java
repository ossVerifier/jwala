package com.siemens.cto.toc.files;

import java.io.IOException;

public interface TemplateManager {

    <T extends TocFile> String getAbsoluteLocation(T templateName) throws IOException;

}
