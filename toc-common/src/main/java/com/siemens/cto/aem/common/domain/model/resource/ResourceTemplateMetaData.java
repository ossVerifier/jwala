package com.siemens.cto.aem.common.domain.model.resource;

import org.apache.commons.lang3.StringUtils;

/**
 * Resource template meta data.
 *
 * Created by JC043760 on 3/30/2016.
 */
public class ResourceTemplateMetaData {
    private String templateName;
    private ContentType contentType;
    private String deployFileName;
    private String deployPath;
    private Entity entity;
    private boolean unpack = false;

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getContentType() {
        if (contentType != null) {
            return contentType.contentTypeStr;
        }
        return StringUtils.EMPTY;
    }

    public void setContentType(String contentType) {
        this.contentType = ContentType.fromContentTypeStr(contentType);
    }

    public Entity getEntity() {
        return entity;
    }

    public String getDeployFileName() {
        return deployFileName;
    }

    public void setDeployFileName(String deployFileName) {
        this.deployFileName = deployFileName;
    }

    public String getDeployPath() {
        return deployPath;
    }

    public void setDeployPath(String deployPath) {
        this.deployPath = deployPath;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public boolean isUnpack() {
        return unpack;
    }

    public void setUnpack(boolean unpack) {
        this.unpack = unpack;
    }

    @Override
    public String toString() {
        return "ResourceTemplateMetaData{" +
                "templateName='" + templateName + '\'' +
                ", contentType=" + contentType +
                ", deployFileName='" + deployFileName + '\'' +
                ", deployPath='" + deployPath + '\'' +
                ", entity=" + entity +
                ", unpack=" + unpack +
                '}';
    }
}
