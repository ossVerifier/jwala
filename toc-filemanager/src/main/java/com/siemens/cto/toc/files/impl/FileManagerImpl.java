package com.siemens.cto.toc.files.impl;

import com.siemens.cto.aem.common.domain.model.resource.ResourceType;
import com.siemens.cto.toc.files.*;
import com.siemens.cto.toc.files.RepositoryFileInformation.Type;
import com.siemens.cto.toc.files.resources.ResourceTypeDeserializer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class FileManagerImpl implements FileManager {

    private static final Logger logger = LoggerFactory.getLogger(FileManagerImpl.class);

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
        if (action.getType() == Type.FOUND) {
            Collection<ResourceType> results = new ArrayList<ResourceType>();
            for (Path path : action) {
                results.add(resourceTypeDeserializer.loadResourceType(path));
            }

            return results;
        } else {
            return Collections.<ResourceType>emptyList();
        }

    }

    @Override
    public String getResourceTypeTemplate(String resourceTypeName) {
        try {
            return read(this.getResourceTypeTemplateByStream(resourceTypeName));
        } catch (IOException ioe) {
            logger.error("Failed to read {}", resourceTypeName, ioe);
        }
        return null;
    }

    @Override
    public InputStream getResourceTypeTemplateByStream(String resourceTypeName) {
        try {
            // TODO: Figure out if this the best way to derive at the template name (by getting the resource type name and removing the spaces and assuming that the they would be the same as that of the file name).
            String resourceTypeNameNoWS = StringUtils.replace(resourceTypeName, " ", "");
            RepositoryFileInformation fileInformation = fileSystemStorage.find(TocPath.RESOURCE_TYPES, Paths.get(resourceTypeNameNoWS + "Template.tpl"));
            if (fileInformation.getType().equals(Type.FOUND)) {
                return this.readFile(fileInformation.getPath());
            }
        } catch (IOException ioe) {
            logger.error("Failed to read {}", resourceTypeName, ioe);
        }
        return null;
    }

    @Override
    public Path getTemplatePathForResourceType(ResourceType template) throws IOException {
        RepositoryFileInformation action = fileSystemStorage.findAll(TocPath.RESOURCE_TYPES, "*.json");
        if (action.getType() == Type.FOUND) {
            for (Path path : action) {
                ResourceType type = resourceTypeDeserializer.loadResourceType(path);
                if (type.isValid() && type.getName().equals(template.getName())) {
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
            return read(this.getMasterTemplateByStream(masterTemplateName));
        } catch (IOException ioe) {
            logger.error("Failed to read {} " + masterTemplateName, ioe);
        }
        return null;
    }

    @Override
    public InputStream getMasterTemplateByStream(String masterTemplateName) {
        try {
            RepositoryFileInformation fileInformation = fileSystemStorage.find(TocPath.TEMPLATES, Paths.get(masterTemplateName + ".tpl"));
            if (fileInformation.getType().equals(Type.FOUND)) {
                return readFile(fileInformation.getPath());
            }
        } catch (IOException ioe) {
            logger.error("Failed to read {}", masterTemplateName, ioe);
        }
        return null;
    }

    private InputStream readFile(Path path) throws IOException {
        return Files.newInputStream(path, StandardOpenOption.READ);
    }

    private String read(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
            sb.append(line);
            sb.append("\n");
            line = reader.readLine();
        }
        return sb.toString();
    }

}
