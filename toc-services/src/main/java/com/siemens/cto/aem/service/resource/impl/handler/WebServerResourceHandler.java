package com.siemens.cto.aem.service.resource.impl.handler;

import com.siemens.cto.aem.common.domain.model.resource.ResourceIdentifier;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.siemens.cto.aem.persistence.service.ResourceDao;
import com.siemens.cto.aem.service.resource.ResourceHandler;
import org.apache.commons.lang3.StringUtils;

/**
 * Handler for a web server resource identified by a "resource identifier" {@link ResourceIdentifier}
 *
 * Created by JC043760 on 7/21/2016
 */
public class WebServerResourceHandler extends ResourceHandler {

    public WebServerResourceHandler(final ResourceDao resourceDao, final ResourceHandler successor) {
        this.resourceDao = resourceDao;
        this.successor = successor;
    }

    @Override
    public ConfigTemplate fetchResource(final ResourceIdentifier resourceIdentifier) {
        ConfigTemplate configTemplate = null;
        if (canHandle(resourceIdentifier)) {
            configTemplate = resourceDao.getWebServerResource(resourceIdentifier.resourceName, resourceIdentifier.webServerName);
        } else if (successor != null) {
            configTemplate = successor.fetchResource(resourceIdentifier);
        }
        return configTemplate;
    }

    @Override
    public void deleteResource(final ResourceIdentifier resourceIdentifier) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean canHandle(final ResourceIdentifier resourceIdentifier) {
        return StringUtils.isNotEmpty(resourceIdentifier.resourceName) &&
               StringUtils.isNotEmpty(resourceIdentifier.webServerName) &&
               !"*".equalsIgnoreCase(resourceIdentifier.webServerName) &&
               StringUtils.isEmpty(resourceIdentifier.groupName) &&
               StringUtils.isEmpty(resourceIdentifier.webAppName) &&
               StringUtils.isEmpty(resourceIdentifier.jvmName);
    }
}
