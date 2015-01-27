package com.siemens.cto.toc.files.impl;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.siemens.cto.aem.domain.model.resource.ResourceType;
import com.siemens.cto.toc.files.Repository;
import com.siemens.cto.toc.files.RepositoryAction;
import com.siemens.cto.toc.files.RepositoryAction.Type;
import com.siemens.cto.toc.files.TemplateManager;
import com.siemens.cto.toc.files.TocFile;
import com.siemens.cto.toc.files.TocPath;
import com.siemens.cto.toc.files.resources.ResourceTypeDeserializer;

public class TemplateManagerImpl implements TemplateManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateManagerImpl.class);

    @Autowired
    private Repository fileSystemStorage;

    @Autowired 
    private ResourceTypeDeserializer resourceTypeDeserializer;
    
    @Override
    public String getAbsoluteLocation(TocFile templateName) throws IOException {
        return fileSystemStorage.find(TocPath.TEMPLATES, FileSystems.getDefault().getPath(templateName.getFileName())).getFoundPath().toString();
    }

    
    @Override 
    public Collection<ResourceType> getResourceTypes() throws IOException {
        
        RepositoryAction action = fileSystemStorage.findAll(TocPath.RESOURCE_TYPES, "*.json");
        if(action.getType() == Type.FOUND) {
            Collection<ResourceType> results = new ArrayList<ResourceType>();
            for(Path path : action) {
                results.add(resourceTypeDeserializer.loadResourceType(path));
            }
            
            return results;
        }
        else {
            return Collections.<ResourceType>emptyList();
        }
        
    }
}
