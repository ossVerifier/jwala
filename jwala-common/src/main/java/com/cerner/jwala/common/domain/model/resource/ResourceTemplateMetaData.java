package com.cerner.jwala.common.domain.model.resource;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Resource template meta data.
 *
 * Created by JC043760 on 3/30/2016.
 */
public class ResourceTemplateMetaData {
    private final String templateName;
    private final ContentType contentType;
    private final String deployFileName;
    private final String deployPath;
    private final Entity entity;
    private boolean unpack = false;
    private boolean overwrite = false;

    @JsonIgnore
    private String jsonData;

    @JsonCreator
    public ResourceTemplateMetaData(@JsonProperty("templateName") final String templateName,
                                    @JsonProperty("contentType") final ContentType contentType,
                                    @JsonProperty("deployFileName") final String deployFileName,
                                    @JsonProperty("deployPath") final String deployPath,
                                    @JsonProperty("entity") final Entity entity,
                                    @JsonProperty("unpack") final boolean unpack,
                                    @JsonProperty("overwrite") boolean overwrite) {
        this.templateName = templateName;
        this.contentType = contentType;
        this.deployFileName = deployFileName;
        this.deployPath = deployPath;
        this.entity = entity;
        this.unpack = unpack;
        this.overwrite = overwrite;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getContentType() {
        if (contentType != null) {
            return contentType.contentTypeStr;
        }
        return StringUtils.EMPTY;
    }

    public Entity getEntity() {
        return entity;
    }

    public String getDeployFileName() {
        return deployFileName;
    }

    public String getDeployPath() {
        return deployPath;
    }

    public boolean isUnpack() {
        return unpack;
    }

    public boolean isOverwrite() { return overwrite; }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
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
                ", overwrite=" + overwrite +
                '}';
    }
}
