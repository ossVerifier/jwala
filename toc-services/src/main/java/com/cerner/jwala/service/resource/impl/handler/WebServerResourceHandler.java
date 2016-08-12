package com.cerner.jwala.service.resource.impl.handler;

import com.cerner.jwala.common.domain.model.resource.ResourceIdentifier;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.request.webserver.UploadWebServerTemplateRequest;
import com.cerner.jwala.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.cerner.jwala.persistence.service.ResourceDao;
import com.cerner.jwala.persistence.service.WebServerPersistenceService;
import com.cerner.jwala.service.resource.ResourceContentGeneratorService;
import com.cerner.jwala.service.resource.ResourceHandler;
import com.cerner.jwala.service.resource.impl.CreateResourceResponseWrapper;

import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;

/**
 * Handler for a web server resource identified by a "resource identifier" {@link ResourceIdentifier}
 *
 * Created by JC043760 on 7/21/2016
 */
public class WebServerResourceHandler extends ResourceHandler {

    private final WebServerPersistenceService webServerPersistenceService;
    private final ResourceContentGeneratorService resourceContentGeneratorService;

    public WebServerResourceHandler(final ResourceDao resourceDao,
                                    final WebServerPersistenceService webServerPersistenceService,
                                    final ResourceContentGeneratorService resourceContentGeneratorService,
                                    final ResourceHandler successor) {
        this.resourceDao = resourceDao;
        this.webServerPersistenceService = webServerPersistenceService;
        this.successor = successor;
        this.resourceContentGeneratorService = resourceContentGeneratorService;
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
    public CreateResourceResponseWrapper createResource(final ResourceIdentifier resourceIdentifier,
                                                        final ResourceTemplateMetaData metaData,
                                                        final InputStream data) {
        CreateResourceResponseWrapper createResourceResponseWrapper = null;
        if (canHandle(resourceIdentifier)) {
            final WebServer webServer = webServerPersistenceService.findWebServerByName(resourceIdentifier.webServerName);
            final UploadWebServerTemplateRequest uploadWebArchiveRequest = new UploadWebServerTemplateRequest(webServer,
                    metaData.getTemplateName(), convertResourceTemplateMetaDataToJson(metaData), data) {
                @Override
                public String getConfFileName() {
                    return metaData.getDeployFileName();
                }
            };
            final String generatedDeployPath = resourceContentGeneratorService.generateContent(metaData.getDeployPath(), webServer);
            createResourceResponseWrapper = new CreateResourceResponseWrapper(webServerPersistenceService
                    .uploadWebServerConfigTemplate(uploadWebArchiveRequest, generatedDeployPath + "/" + metaData.getDeployFileName(), null));
        } else if (successor != null) {
            createResourceResponseWrapper = successor.createResource(resourceIdentifier, metaData, data);
        }
        return createResourceResponseWrapper;
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
               /*StringUtils.isEmpty(resourceIdentifier.groupName) &&*/
               StringUtils.isEmpty(resourceIdentifier.webAppName) &&
               StringUtils.isEmpty(resourceIdentifier.jvmName);
    }
}
