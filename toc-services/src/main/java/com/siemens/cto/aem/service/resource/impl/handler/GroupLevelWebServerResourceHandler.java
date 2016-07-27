package com.siemens.cto.aem.service.resource.impl.handler;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.resource.ResourceIdentifier;
import com.siemens.cto.aem.common.domain.model.resource.ResourceTemplateMetaData;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.siemens.cto.aem.persistence.service.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.ResourceDao;
import com.siemens.cto.aem.persistence.service.WebServerPersistenceService;
import com.siemens.cto.aem.service.resource.ResourceContentGeneratorService;
import com.siemens.cto.aem.service.resource.ResourceHandler;
import com.siemens.cto.aem.service.resource.impl.CreateResourceResponseWrapper;
import com.siemens.cto.aem.service.resource.impl.handler.exception.ResourceHandlerException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Handler for a group level web server resource identified by a "resource identifier" {@link ResourceIdentifier}
 *
 * Created by JC043760 on 7/21/2016
 */
public class GroupLevelWebServerResourceHandler extends ResourceHandler {

    private static final String MSG_ERR_CONVERTING_DATA_INPUTSTREAM_TO_STR = "Error converting data input stream to string!";
    private final GroupPersistenceService groupPersistenceService;
    private final WebServerPersistenceService webServerPersistenceService;
    private final ResourceContentGeneratorService resourceContentGeneratorService;

    public GroupLevelWebServerResourceHandler(final ResourceDao resourceDao,
                                              final GroupPersistenceService groupPersistenceService,
                                              final WebServerPersistenceService webServerPersistenceService,
                                              final ResourceContentGeneratorService resourceContentGeneratorService,
                                              final ResourceHandler successor) {
        this.resourceDao = resourceDao;
        this.groupPersistenceService = groupPersistenceService;
        this.webServerPersistenceService = webServerPersistenceService;
        this.resourceContentGeneratorService = resourceContentGeneratorService;
        this.successor = successor;
    }

    @Override
    public ConfigTemplate fetchResource(final ResourceIdentifier resourceIdentifier) {
        ConfigTemplate configTemplate = null;
        if (canHandle(resourceIdentifier)) {
            configTemplate = resourceDao.getGroupLevelWebServerResource(resourceIdentifier.resourceName, resourceIdentifier.groupName);
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

            final Group group = groupPersistenceService.getGroupWithWebServers(metaData.getEntity().getGroup());
            final Set<WebServer> webServers = group.getWebServers();
            final Map<String, UploadWebServerTemplateRequest> uploadWebServerTemplateRequestMap = new HashMap<>();
            ConfigTemplate createdConfigTemplate = null;
            final String templateContent;

            try {
                templateContent = IOUtils.toString(data);
            } catch (final IOException ioe) {
                throw new ResourceHandlerException(MSG_ERR_CONVERTING_DATA_INPUTSTREAM_TO_STR, ioe);
            }

            for (final WebServer webServer : webServers) {

                UploadWebServerTemplateRequest uploadWebServerTemplateRequest = new UploadWebServerTemplateRequest(webServer,
                        metaData.getTemplateName(), convertResourceTemplateMetaDataToJson(metaData), new ByteArrayInputStream(templateContent.getBytes())) {
                    @Override
                    public String getConfFileName() {
                        return metaData.getDeployFileName();
                    }
                };

                // Since we're just creating the same template for all the JVMs, we just keep one copy of the created
                // configuration template. Note that ResourceGroup is null since we only need the web server paths and
                // application properties for mapping.
                final String generatedDeployPath = resourceContentGeneratorService.generateContent(metaData.getDeployPath(), webServer);
                createdConfigTemplate = webServerPersistenceService.uploadWebServerConfigTemplate(uploadWebServerTemplateRequest,
                        generatedDeployPath + "/" + metaData.getDeployFileName(), null);
            }

            UploadWebServerTemplateRequest uploadWebServerTemplateRequest = new UploadWebServerTemplateRequest(null,
                    metaData.getTemplateName(), convertResourceTemplateMetaDataToJson(metaData), new ByteArrayInputStream(templateContent.getBytes())) {
                @Override
                public String getConfFileName() {
                    return metaData.getDeployFileName();
                }
            };
            uploadWebServerTemplateRequestMap.put(metaData.getDeployFileName(), uploadWebServerTemplateRequest);
            groupPersistenceService.populateGroupWebServerTemplates(group.getName(), uploadWebServerTemplateRequestMap);
            createResourceResponseWrapper = new CreateResourceResponseWrapper(createdConfigTemplate);
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
               StringUtils.isNotEmpty(resourceIdentifier.groupName) &&
                "*".equalsIgnoreCase(resourceIdentifier.webServerName) &&
               StringUtils.isEmpty(resourceIdentifier.jvmName) &&
               StringUtils.isEmpty(resourceIdentifier.webAppName);
    }
}
