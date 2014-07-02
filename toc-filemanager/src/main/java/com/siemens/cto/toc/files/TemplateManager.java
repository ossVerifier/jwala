package com.siemens.cto.toc.files;

import java.io.IOException;

public interface TemplateManager {

    RepositoryAction locateTemplate(String templateName) throws IOException;

}
