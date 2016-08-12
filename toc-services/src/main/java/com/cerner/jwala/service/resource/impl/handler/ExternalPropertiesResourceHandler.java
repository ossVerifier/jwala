package com.cerner.jwala.service.resource.impl.handler;

import com.cerner.jwala.common.domain.model.resource.EntityType;
import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.properties.ExternalProperties;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.cerner.jwala.persistence.service.ResourceDao;
import com.cerner.jwala.service.resource.ResourceHandler;
import com.cerner.jwala.service.resource.impl.CreateResourceResponseWrapper;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

public class ExternalPropertiesResourceHandler extends ResourceHandler {

    public ExternalPropertiesResourceHandler(final ResourceDao resourceDao, final ResourceHandler successor) {
        this.resourceDao = resourceDao;
        this.successor = successor;
    }

    @Override
    public ConfigTemplate fetchResource(ResourceIdentifier resourceIdentifier) {
        ConfigTemplate configTemplate = null;
        if (canHandle(resourceIdentifier)) {
            configTemplate = resourceDao.getExternalPropertiesResource(resourceIdentifier.resourceName);
        } else if (successor != null) {
            configTemplate = successor.fetchResource(resourceIdentifier);
        }
        return configTemplate;
    }

    @Override
    public CreateResourceResponseWrapper createResource(final ResourceIdentifier resourceIdentifier,
                                                        final ResourceTemplateMetaData metaData,
                                                        final InputStream data) {
        CreateResourceResponseWrapper createResourceResponseWrapper = null;
        if (canHandle(resourceIdentifier)) {
            Long entityId = null;
            Long groupId = null;
            Long appId = null;
            EntityType entityType = EntityType.EXT_PROPERTIES;

            // remove any existing template
            List<String> existingTemplateNames = resourceDao.getResourceNames(resourceIdentifier, EntityType.EXT_PROPERTIES);
            if (existingTemplateNames.size() > 0){
                resourceDao.deleteExternalProperties();
                ExternalProperties.reset();
                // TODO clean up any deployed files on desk (don't delete - just rename with timestamp)
            }

            // create the external properties template
            final String deployFileName = metaData.getDeployFileName();
            createResourceResponseWrapper = new CreateResourceResponseWrapper(resourceDao.createResource(entityId, groupId, appId, entityType, deployFileName, data, convertResourceTemplateMetaDataToJson(metaData)));

            // apply the external properties
            // TODO make get template content generic for all resources
            String propertiesContent = resourceDao.getExternalPropertiesResource(deployFileName).getTemplateContent();
            ExternalProperties.loadFromInputStream(new ByteArrayInputStream(propertiesContent.getBytes()));

        } else if (successor != null) {
            createResourceResponseWrapper = successor.createResource(resourceIdentifier, metaData, data);
        }
        return createResourceResponseWrapper;
    }

    @Override
    public void deleteResource(ResourceIdentifier resourceIdentifier) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean canHandle(ResourceIdentifier resourceIdentifier) {
        // TODO create ResourceIdentifier attribute specifically for the ext properties
        return StringUtils.isNotEmpty(resourceIdentifier.resourceName) &&
                StringUtils.isEmpty(resourceIdentifier.webAppName) &&
                StringUtils.isEmpty(resourceIdentifier.jvmName) &&
                StringUtils.isEmpty(resourceIdentifier.groupName) &&
                StringUtils.isEmpty(resourceIdentifier.webServerName);
    }
}
