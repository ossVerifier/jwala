package com.siemens.cto.aem.service.resource.impl;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.resource.*;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.exception.FaultCodeException;
import com.siemens.cto.aem.common.request.app.UploadAppTemplateRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmConfigTemplateRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.common.request.resource.ResourceInstanceRequest;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.persistence.service.*;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.service.exception.ResourceServiceException;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.template.HarmonyTemplate;
import com.siemens.cto.aem.template.HarmonyTemplateEngine;
import com.siemens.cto.toc.files.FileManager;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ResourceServiceImpl implements ResourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceServiceImpl.class);

    private final FileManager fileManager;
    private final HarmonyTemplateEngine templateEngine;
    private final SpelExpressionParser expressionParser;
    private final Expression encryptExpression;
    private final ResourcePersistenceService resourcePersistenceService;
    private final GroupPersistenceService groupPersistenceService;

    private final String encryptExpressionString="new com.siemens.cto.infrastructure.StpCryptoService().encryptToBase64( #stringToEncrypt )";

    @Autowired // TODO: Instantiate in the constructor...
    private JvmService jvmService;

    @Autowired // TODO: Instantiate in the constructor...
    private WebServerService webServerService;

    @Autowired // TODO: Instantiate in the constructor...
    private ApplicationService applicationService;

    @Autowired // TODO: Instantiate in the constructor...
    private ApplicationPersistenceService applicationPersistenceService;

    @Autowired  // TODO: Instantiate in the constructor...
    private JvmPersistenceService jvmPersistenceService;

    @Autowired  // TODO: Instantiate in the constructor...
    private WebServerPersistenceService webServerPersistenceService;

    @Value("${paths.resource-types}")
    private String templatePath;

    public ResourceServiceImpl(
            final FileManager theFileManager,
            final HarmonyTemplateEngine harmonyTemplateEngine,
            final ResourcePersistenceService resourcePersistenceService,
            final GroupPersistenceService groupPersistenceService
            ) {
        fileManager = theFileManager;
        templateEngine = harmonyTemplateEngine;
        this.resourcePersistenceService = resourcePersistenceService;
        this.groupPersistenceService = groupPersistenceService;
        expressionParser = new SpelExpressionParser();
        encryptExpression = expressionParser.parseExpression(encryptExpressionString);
    }

    @Override
    public Collection<ResourceType> getResourceTypes() {
        try {
            Collection<ResourceType> resourceTypes = fileManager.getResourceTypes();
            for(ResourceType rtype : resourceTypes) {
                if(rtype.isValid()) {
                    HarmonyTemplate template = templateEngine.getTemplate(rtype);
                    try {
                        template.check();
                    } catch(Exception exception) { 
                        LOGGER.error("Discovered a bad template", exception);
                        rtype.setValid(false);
                        rtype.addException(exception);
                    }
                }
            }
            return resourceTypes;
        } catch (IOException e) {
            // This is extremely unlikely since we return ResourceTypes(valid=false) even when files are invalid. 
            String errorString = "Failed to get resource types from disk.";
            LOGGER.error(errorString, e);
            throw new FaultCodeException(AemFaultType.INVALID_PATH, errorString, e);
        }
    }

    @Override
    @Transactional (readOnly = true)
    public ResourceInstance getResourceInstance(final Identifier<ResourceInstance> aResourceInstanceId) {
        return this.resourcePersistenceService.getResourceInstance(aResourceInstanceId);
    }

    @Override
    @Transactional (readOnly = true)
    public List<ResourceInstance> getResourceInstancesByGroupName(final String groupName) {
        Group group = this.groupPersistenceService.getGroup(groupName);
        return this.resourcePersistenceService.getResourceInstancesByGroupId(group.getId().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public ResourceInstance getResourceInstanceByGroupNameAndName(final String groupName, final String name) {
        Group group = this.groupPersistenceService.getGroup(groupName);
        return this.resourcePersistenceService.getResourceInstanceByGroupIdAndName(group.getId().getId(), name);
    }

    @Override
    public String generateResourceInstanceFragment(String groupName, String resourceInstanceName) {
        final Map<String, String> mockedValues = new HashMap<>();
        mockedValues.put("jvm.id", "[jvm.id of instance]");
        mockedValues.put("jvm.name", "[jvm.name of instance]");
        mockedValues.put("app.name", "[app.name of Web App]");
        return generateResourceInstanceFragment(groupName, resourceInstanceName, mockedValues);
    }

    @Override
    public String generateResourceInstanceFragment(String groupName, String resourceInstanceName, Map<String, String> mockedValues) {
        ResourceInstance resourceInstance =  this.getResourceInstanceByGroupNameAndName(groupName, resourceInstanceName);
        return templateEngine.populateResourceInstanceTemplate(resourceInstance, null, mockedValues);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResourceInstance> getResourceInstancesByGroupNameAndResourceTypeName(final String groupName, final String resourceTypeName) {
        Group group = this.groupPersistenceService.getGroup(groupName);
        return this.resourcePersistenceService.getResourceInstancesByGroupIdAndResourceTypeName(group.getId().getId(), resourceTypeName);
    }

    @Override
    @Transactional
    public ResourceInstance createResourceInstance(final ResourceInstanceRequest createResourceInstanceRequest, final User creatingUser) {
        this.groupPersistenceService.getGroup(createResourceInstanceRequest.getGroupName());
        return this.resourcePersistenceService.createResourceInstance(createResourceInstanceRequest);
    }

    @Override
    @Transactional
    public ResourceInstance updateResourceInstance(final String groupName, final String name, final ResourceInstanceRequest updateResourceInstanceRequest, final User updatingUser) {
        ResourceInstance resourceInstance = this.getResourceInstanceByGroupNameAndName(groupName, name);
        return this.resourcePersistenceService.updateResourceInstance(resourceInstance, updateResourceInstanceRequest);
    }

    @Override
    @Transactional
    public void deleteResourceInstance(final String groupName, final String name) {
        ResourceInstance resourceInstance = this.getResourceInstanceByGroupNameAndName(groupName, name);
        this.resourcePersistenceService.deleteResourceInstance(resourceInstance.getResourceInstanceId());
    }

    @Override
    @Transactional
    public void deleteResources(final String groupName, final List<String> resourceNames) {
        resourcePersistenceService.deleteResources(groupName, resourceNames);
    }

    @Override
    public String encryptUsingPlatformBean(String cleartext) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("stringToEncrypt", cleartext);
        return encryptExpression.getValue(context, String.class);
    }

    @Override
    public String getTemplate(final String resourceTypeName) {
        return templateEngine.getTemplate(resourceTypeName);
    }

    @Override
    @Transactional
    public void createTemplate(final String metaDataFile, final String templateFile, final User user) {
        final ObjectMapper mapper = new ObjectMapper();
        final ResourceTemplateMetaData metaData;
        final String templateData;
        final String jsonData;

        try {
            jsonData = FileUtils.readFileToString(new File(metaDataFile));
            metaData = mapper.readValue(jsonData, ResourceTemplateMetaData.class);
            templateData = FileUtils.readFileToString(new File(templateFile));
        } catch(final IOException ioe) {
            throw new ResourceServiceException(ioe);
        }

        // Let's create the template!
        final EntityType entityType = EntityType.fromValue(metaData.getEntity().getType());
        switch (entityType) {
            case JVM:
                createJvmTemplate(user, metaData, templateData);
                break;
            case JVMS:
                createJvmsTemplate(user, metaData, templateData);
                break;
            case WEB_SERVER:
                createWebServerTemplate(user, metaData, templateData);
                break;
            case WEB_SERVERS:
                createWebServersTemplate(user, metaData, templateData);
                break;
            case APP:
                createApplicationTemplate(user, metaData, templateData);
                break;
            case APPS:
                createApplicationsTemplate(user, metaData, templateData);
                break;
            default:
                throw new ResourceServiceException("Invalid entity type '" + metaData.getEntity().getType() + "'");
        }
        createMetaAndTemplateDataLocalCopy(metaData, templateData, jsonData);
    }

    /**
     * Make a local copy of the template file and its meta data in the templates path since they are used in resource generation.
     * @param metaData {@link ResourceTemplateMetaData}
     * @param templateData the template file content
     * @param jsonData the meta data String that describes templateData
     */
    protected void createMetaAndTemplateDataLocalCopy(final ResourceTemplateMetaData metaData, final String templateData,
                                                      final String jsonData) {
        final ContentType contentType = ContentType.fromContentTypeStr(metaData.getContentType());
        if (contentType != ContentType.UNDEFINED) {
            try {
                final String tmpFileName = templatePath + "/" + metaData.getName() + contentType;
                final File localCopyMetaDataFile = new File(tmpFileName + "Properties.json");
                FileUtils.writeStringToFile(localCopyMetaDataFile, jsonData);
                final File localCopyTemplateFile = new File(tmpFileName + "Template.tpl");
                FileUtils.writeStringToFile(localCopyTemplateFile, templateData);
            } catch (final IOException ioe) {
                throw new ResourceServiceException(ioe);
            }
        } else {
            throw new ResourceServiceException("Invalid content type = \"" + metaData.getContentType() + "\"!");
        }
    }

    /**
     * Create the JVM template in the db and in the templates path for a specific JVM entity target.
     * @param user the user
     * @param metaData the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData the template content/data
     */
    private void createJvmTemplate(final User user, final ResourceTemplateMetaData metaData, final String templateData) {
        final Jvm jvm = jvmService.getJvm(metaData.getEntity().getTarget());
        UploadJvmConfigTemplateRequest uploadJvmTemplateRequest = new UploadJvmConfigTemplateRequest(jvm, metaData.getTemplateName(),
                new ByteArrayInputStream(templateData.getBytes(StandardCharsets.UTF_8)));
        uploadJvmTemplateRequest.setConfFileName(metaData.getConfigFileName());
        jvmService.uploadJvmTemplateXml(uploadJvmTemplateRequest, user);
    }

    /**
     * Create the JVM template in the db and in the templates path for all the JVMs.
     * @param user the user
     * @param metaData the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData the template content/data
     */
    // TODO: When the resource file is locked, don't overwrite!
    private void createJvmsTemplate(final User user, final ResourceTemplateMetaData metaData, final String templateData) {
        final Set<Jvm> jvms = groupPersistenceService.getGroup(metaData.getEntity().getGroup()).getJvms();
        final List<UploadJvmTemplateRequest> uploadJvmTemplateRequestList = new ArrayList<>();
        for (final Jvm jvm: jvms) {
            UploadJvmConfigTemplateRequest uploadJvmTemplateRequest = new UploadJvmConfigTemplateRequest(jvm, metaData.getTemplateName(),
                    new ByteArrayInputStream(templateData.getBytes(StandardCharsets.UTF_8)));
            uploadJvmTemplateRequest.setConfFileName(metaData.getConfigFileName());
            uploadJvmTemplateRequestList.add(uploadJvmTemplateRequest);
            jvmService.uploadJvmTemplateXml(uploadJvmTemplateRequest, user);
        }
        groupPersistenceService.populateGroupJvmTemplates(metaData.getEntity().getGroup(), uploadJvmTemplateRequestList, user);
    }

    /**
     * Create the web server template in the db and in the templates path for a specific web server entity target.
     * @param user the user
     * @param metaData the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData the template content/data
     */
    private void createWebServerTemplate(final User user, final ResourceTemplateMetaData metaData, final String templateData) {
        final WebServer webServer = webServerService.getWebServer(metaData.getEntity().getTarget());
        UploadWebServerTemplateRequest uploadWebArchiveRequest = new UploadWebServerTemplateRequest(webServer,
                metaData.getTemplateName(), new ByteArrayInputStream(templateData.getBytes(StandardCharsets.UTF_8))) {
            @Override
            public String getConfFileName() {
                return metaData.getConfigFileName();
            }
        };
        webServerService.uploadWebServerConfig(uploadWebArchiveRequest, user);
    }

    /**
     * Create the web server template in the db and in the templates path for all the web servers.
     * @param user the user
     * @param metaData the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData the template content/data
     */
    private void createWebServersTemplate(final User user, final ResourceTemplateMetaData metaData, final String templateData) {
        final Group group = groupPersistenceService.getGroupWithWebServers(metaData.getEntity().getGroup());
        final Set<WebServer> webServers = group.getWebServers();
        final List<UploadWebServerTemplateRequest> uploadWebServerTemplateRequestList = new ArrayList<>();
        for (final WebServer webServer: webServers) {
            UploadWebServerTemplateRequest uploadWebServerTemplateRequest = new UploadWebServerTemplateRequest(webServer,
                    metaData.getTemplateName(), new ByteArrayInputStream(templateData.getBytes(StandardCharsets.UTF_8))) {
                @Override
                public String getConfFileName() {
                    return metaData.getConfigFileName();
                }
            };
            uploadWebServerTemplateRequestList.add(uploadWebServerTemplateRequest);
            webServerService.uploadWebServerConfig(uploadWebServerTemplateRequest, user);
        }
        groupPersistenceService.populateGroupWebServerTemplates(group.getName(), uploadWebServerTemplateRequestList, user);
    }

    /**
     * Create the application template in the db and in the templates path for a specific application entity target.
     * @param user the user
     * @param metaData the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData the template content/data
     */
    private void createApplicationTemplate(final User user, final ResourceTemplateMetaData metaData, final String templateData) {
        final Application application = applicationService.getApplication(metaData.getEntity().getTarget());
        UploadAppTemplateRequest uploadAppTemplateRequest = new UploadAppTemplateRequest(application, metaData.getTemplateName(),
                metaData.getConfigFileName(), metaData.getEntity().getParentName(),
                new ByteArrayInputStream(templateData.getBytes(StandardCharsets.UTF_8)));
        applicationService.uploadAppTemplate(uploadAppTemplateRequest, user);
    }

    /**
     * Create the application template in the db and in the templates path for all the application.
     * @param user the user
     * @param metaData the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData the template content/data
     */
    private void createApplicationsTemplate(final User user, final ResourceTemplateMetaData metaData, final String templateData) {
        final Group group = groupPersistenceService.getGroup(metaData.getEntity().getGroup());
        final List<Application> applications = applicationPersistenceService.findApplicationsBelongingTo(metaData.getEntity().getGroup());
        for (final Application application: applications) {
            UploadAppTemplateRequest uploadAppTemplateRequest = new UploadAppTemplateRequest(application, metaData.getTemplateName(),
                    metaData.getConfigFileName(), metaData.getEntity().getParentName(),
                    new ByteArrayInputStream(templateData.getBytes(StandardCharsets.UTF_8)));
            applicationService.uploadAppTemplate(uploadAppTemplateRequest, user);
            groupPersistenceService.populateGroupAppTemplate(group, metaData.getConfigFileName(), templateData);
        }
    }

    @Override
    @Transactional
    public int removeTemplate(final String name) {
        final int recordsRemovedTotal = applicationPersistenceService.removeTemplate(name) + jvmPersistenceService.removeTemplate(name)
                + webServerPersistenceService.removeTemplate(name) + groupPersistenceService.removeAppTemplate(name) +
                  groupPersistenceService.removeJvmTemplate(name) + groupPersistenceService.removeWeServerTemplate(name);
        // TODO: Delete the template file here!
        return recordsRemovedTotal;
    }
}
