package com.cerner.jwala.common.domain.model.resource;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

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
    private boolean overwrite = false;

    @JsonIgnore
    private String jsonData;

    protected ResourceTemplateMetaData() {
        // Protected constructor to force developers to use createFromJsonStr
        // We want this POJO immutable
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

    /**
     * Creates a resource meta data from a json string
     * @param jsonData the json resource meta data string
     * @return {@link ResourceTemplateMetaData}
     * @throws IOException
     */
    public static ResourceTemplateMetaData createFromJsonStr(final String jsonData) throws IOException {
        if (StringUtils.isNotEmpty(jsonData)) {
            final ResourceTemplateMetaData metaData = new ObjectMapper().readValue(jsonData, ResourceTemplateMetaData.class);
            metaData.setJsonData(jsonData);
            return metaData;
        }
        return new ResourceTemplateMetaData();
    }
}
