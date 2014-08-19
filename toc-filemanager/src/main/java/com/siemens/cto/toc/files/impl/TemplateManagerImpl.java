package com.siemens.cto.toc.files.impl;

import java.io.IOException;
import java.nio.file.FileSystems;

import org.springframework.beans.factory.annotation.Autowired;

import com.siemens.cto.toc.files.Repository;
import com.siemens.cto.toc.files.RepositoryAction;
import com.siemens.cto.toc.files.TemplateManager;
import com.siemens.cto.toc.files.TocPath;

public class TemplateManagerImpl implements TemplateManager {

    @Autowired
    private Repository fileSystemStorage;
    

    @Override
    public RepositoryAction locateTemplate(String templateName) throws IOException {
        
        return fileSystemStorage.find(TocPath.TEMPLATES, FileSystems.getDefault().getPath(templateName));
    }

}
