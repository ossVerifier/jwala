package com.siemens.cto.aem.common.domain.model.resource;

import java.util.Arrays;

/**
 * Resource template meta data.
 *
 * Created by JC043760 on 3/30/2016.
 */
public class ResourceTemplateMetaData {
    private String name;
    private String templateName;
    private String contentType;
    private String configFileName;
    private String path;
    private Entity entity;
    private ResourceProperty [] properties;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Entity getEntity() {
        return entity;
    }

    public String getConfigFileName() {
        return configFileName;
    }

    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public ResourceProperty [] getProperties() {
        return properties;
    }

    public void setProperties(ResourceProperty [] properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "ResourceTemplateMetaData{" +
                "name='" + name + '\'' +
                ", templateName='" + templateName + '\'' +
                ", contentType='" + contentType + '\'' +
                ", configFileName='" + configFileName + '\'' +
                ", path='" + path + '\'' +
                ", entity=" + entity +
                ", properties=" + Arrays.toString(properties) +
                '}';
    }
}
