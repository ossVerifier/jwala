package com.siemens.cto.aem.common.domain.model.resource;

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
    private String relativeDir;
    private Entity entity;

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

    public String getRelativeDir() {
        return relativeDir;
    }

    public void setRelativeDir(String relativeDir) {
        this.relativeDir = relativeDir;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

}
