package com.siemens.cto.toc.files.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.siemens.cto.aem.domain.model.resource.ResourceType;
import com.siemens.cto.toc.files.RepositoryService;
import com.siemens.cto.toc.files.RepositoryFileInformation;
import com.siemens.cto.toc.files.RepositoryFileInformation.Type;
import com.siemens.cto.toc.files.TemplateManager;
import com.siemens.cto.toc.files.TocFile;
import com.siemens.cto.toc.files.TocPath;
import com.siemens.cto.toc.files.resources.ResourceTypeDeserializer;

public class TemplateManagerImpl implements TemplateManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateManagerImpl.class);

    @Autowired
    private RepositoryService fileSystemStorage;

    @Autowired 
    private ResourceTypeDeserializer resourceTypeDeserializer;
    
    @Override
    public String getAbsoluteLocation(TocFile templateName) throws IOException {
        return fileSystemStorage.find(TocPath.TEMPLATES, FileSystems.getDefault().getPath(templateName.getFileName())).getFoundPath().toString();
    }

    
    @Override 
    public Collection<ResourceType> getResourceTypes() throws IOException {
        
        RepositoryFileInformation action = fileSystemStorage.findAll(TocPath.RESOURCE_TYPES, "*.json");
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

    @Override
    public String getResourceTypeTemplate(String resourceTypeName) {
        try {
            // TODO: Figure out if this the best way to derive at the template name (by getting the resource type name and removing the spaces and assuming that the they would be the same as that of the file name).
            resourceTypeName = StringUtils.replace(resourceTypeName, " ", "");
            RepositoryFileInformation fileInformation = fileSystemStorage.find(TocPath.RESOURCE_TYPES, Paths.get(resourceTypeName + "Template.tpl"));
            if (fileInformation.getType().equals(Type.FOUND)) {
                return fileInformation.readFile();
            }
        }
        catch (IOException ioe) {
            System.out.println(ioe);
        }
        return null;
    }


    @Override
    public Path getTemplatePathForResourceType(ResourceType template) throws IOException {
        RepositoryFileInformation action = fileSystemStorage.findAll(TocPath.RESOURCE_TYPES, "*.json");
        if(action.getType() == Type.FOUND) {
            for(Path path : action) {
                ResourceType type = resourceTypeDeserializer.loadResourceType(path);
                if(type.isValid() && type.getName().equals(template.getName())) {
                    String baseName = ResourceTypeDeserializer.parseRootNameFromFile(path);                   
                    return path.getParent().resolve(baseName + "Template.tpl");
                }
            }            
        }
        throw new FileNotFoundException("xxxTemplate.tpl file for " + template.getName());
    }

    @Override
    public String getMasterTemplate(String masterTemplateName) {
        try {
            RepositoryFileInformation fileInformation = fileSystemStorage.find(TocPath.TEMPLATES, Paths.get(masterTemplateName + ".tpl"));
            if (fileInformation.getType().equals(Type.FOUND)) {
                return fileInformation.readFile();
            }
        }
        catch (IOException ioe) {
            System.out.println(ioe);
        }
        return null;
    }
}
