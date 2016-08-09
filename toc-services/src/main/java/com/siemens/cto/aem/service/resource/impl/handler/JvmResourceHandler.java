package com.siemens.cto.aem.service.resource.impl.handler;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.resource.ResourceIdentifier;
import com.siemens.cto.aem.common.domain.model.resource.ResourceTemplateMetaData;
import com.siemens.cto.aem.common.request.jvm.UploadJvmConfigTemplateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.siemens.cto.aem.persistence.service.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.JvmPersistenceService;
import com.siemens.cto.aem.persistence.service.ResourceDao;
import com.siemens.cto.aem.service.resource.ResourceHandler;
import com.siemens.cto.aem.service.resource.impl.CreateResourceResponseWrapper;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;

/**
 * Handler for a jvm resource identified by a "resource identifier" {@link ResourceIdentifier}
 *
 * Created by JC043760 on 7/21/2016
 */
public class JvmResourceHandler extends ResourceHandler {

    private final GroupPersistenceService groupPersistenceService;
    private final JvmPersistenceService jvmPersistenceService;

    public JvmResourceHandler(final ResourceDao resourceDao, final GroupPersistenceService groupPersistenceService,
                              final JvmPersistenceService jvmPersistenceService, final ResourceHandler successor) {
        this.resourceDao = resourceDao;
        this.groupPersistenceService = groupPersistenceService;
        this.jvmPersistenceService = jvmPersistenceService;
        this.successor = successor;
    }

    @Override
    public ConfigTemplate fetchResource(final ResourceIdentifier resourceIdentifier) {
        ConfigTemplate configTemplate = null;
        if (canHandle(resourceIdentifier)) {
            configTemplate = resourceDao.getJvmResource(resourceIdentifier.resourceName, resourceIdentifier.jvmName);
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
            final Jvm jvm = jvmPersistenceService.findJvmByExactName(resourceIdentifier.jvmName);
            final Group parentGroup = groupPersistenceService.getGroup(resourceIdentifier.groupName);
            final Jvm jvmWithParentGroup = new Jvm(jvm.getId(),
                    jvm.getJvmName(),
                    jvm.getHostName(),
                    jvm.getGroups(),
                    parentGroup,
                    jvm.getHttpPort(),
                    jvm.getHttpsPort(),
                    jvm.getRedirectPort(),
                    jvm.getShutdownPort(),
                    jvm.getAjpPort(),
                    jvm.getStatusPath(),
                    jvm.getSystemProperties(),
                    jvm.getState(),
                    jvm.getErrorStatus(),
                    jvm.getLastUpdatedDate(),
                    jvm.getUserName(),
                    jvm.getEncryptedPassword());

            final UploadJvmConfigTemplateRequest uploadJvmTemplateRequest = new UploadJvmConfigTemplateRequest(jvmWithParentGroup, metaData.getTemplateName(),
                    data, convertResourceTemplateMetaDataToJson(metaData));
            uploadJvmTemplateRequest.setConfFileName(metaData.getDeployFileName());
            createResourceResponseWrapper = new CreateResourceResponseWrapper(jvmPersistenceService.uploadJvmTemplateXml(uploadJvmTemplateRequest));
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
               StringUtils.isNotEmpty(resourceIdentifier.jvmName) &&
               !"*".equalsIgnoreCase(resourceIdentifier.jvmName) &&
               /*StringUtils.isEmpty(resourceIdentifier.groupName) &&*/
               StringUtils.isEmpty(resourceIdentifier.webAppName) &&
               StringUtils.isEmpty(resourceIdentifier.webServerName);
    }
}
