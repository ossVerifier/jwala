package com.siemens.cto.aem.domain.model.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceType {

    String name;
    boolean valid;
    String contentType;
    List<Map<?, ?>> properties;
    private String entityType;
    private String configFileName;
    private String relativeDir;
    private String templateName;

    public ResourceType() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public List<Map<?, ?>> getProperties() {
        return properties;
    }

    public void setProperties(List<Map<?, ?>> properties) {
        this.properties = properties;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean validity) {
        this.valid = validity;
    }

    public Map<String, String> addException(Throwable exception) {
        if (properties == null) {
            properties = new ArrayList<Map<?, ?>>();
        }

        Map<String, String> exceptionProp = new HashMap<String, String>();
        exceptionProp.put("exception", exception.toString());

        properties.add(exceptionProp);
        return exceptionProp;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getConfigFileName() {
        return configFileName;
    }

    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
    }

    public String getRelativeDir() {
        return relativeDir;
    }

    public void setRelativeDir(String relativeDir) {
        this.relativeDir = relativeDir;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
}
