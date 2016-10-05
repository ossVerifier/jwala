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

    private static final ObjectMapper objectMapper = new ObjectMapper();

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

    public boolean isOverwrite() { return overwrite; }

    public void setOverwrite(boolean overwrite) { this.overwrite = overwrite; }

    public String getJsonData() {
        try {
            final ResourceTemplateMetaData metaData = createFromJsonStr(jsonData);
            if (this.equals(metaData)) {
                return jsonData;
            }
            jsonData = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
            return jsonData;
        } catch (final IOException e) {
            throw new ResourceTemplateMetaDataException("Failed to generate JSON data!", e);
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceTemplateMetaData metaData = (ResourceTemplateMetaData) o;

        if (unpack != metaData.unpack) return false;
        if (overwrite != metaData.overwrite) return false;
        if (templateName != null ? !templateName.equals(metaData.templateName) : metaData.templateName != null)
            return false;
        if (contentType != metaData.contentType) return false;
        if (deployFileName != null ? !deployFileName.equals(metaData.deployFileName) : metaData.deployFileName != null)
            return false;
        if (deployPath != null ? !deployPath.equals(metaData.deployPath) : metaData.deployPath != null) return false;
        return !(entity != null ? !entity.equals(metaData.entity) : metaData.entity != null);

    }

    @Override
    public int hashCode() {
        int result = templateName != null ? templateName.hashCode() : 0;
        result = 31 * result + (contentType != null ? contentType.hashCode() : 0);
        result = 31 * result + (deployFileName != null ? deployFileName.hashCode() : 0);
        result = 31 * result + (deployPath != null ? deployPath.hashCode() : 0);
        result = 31 * result + (entity != null ? entity.hashCode() : 0);
        result = 31 * result + (unpack ? 1 : 0);
        result = 31 * result + (overwrite ? 1 : 0);
        return result;
    }

    /**
     * Creates a resource meta data from a json string
     * @param jsonData the json resource meta data string
     * @return {@link ResourceTemplateMetaData}
     * @throws IOException
     */
    public static ResourceTemplateMetaData createFromJsonStr(final String jsonData) throws IOException {
        if (StringUtils.isNotEmpty(jsonData)) {
            final ResourceTemplateMetaData metaData = objectMapper.readValue(jsonData, ResourceTemplateMetaData.class);
            metaData.setJsonData(jsonData);
            return metaData;
        }
        return new ResourceTemplateMetaData();
    }
}
