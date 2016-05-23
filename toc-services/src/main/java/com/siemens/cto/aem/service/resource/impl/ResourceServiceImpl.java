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
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.siemens.cto.aem.persistence.service.*;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.service.exception.ResourceServiceException;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.template.HarmonyTemplate;
import com.siemens.cto.aem.template.HarmonyTemplateEngine;
import com.siemens.cto.aem.template.ResourceFileGenerator;
import com.siemens.cto.toc.files.FileManager;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ResourceServiceImpl implements ResourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceServiceImpl.class);

    private final FileManager fileManager;
    private final HarmonyTemplateEngine templateEngine;
    private final SpelExpressionParser expressionParser;
    private final Expression encryptExpression;
    private final ResourcePersistenceService resourcePersistenceService;
    private final GroupPersistenceService groupPersistenceService;

    private final String encryptExpressionString = "new com.siemens.cto.infrastructure.StpCryptoService().encryptToBase64( #stringToEncrypt )";

    private ApplicationService applicationService;

    private ApplicationPersistenceService applicationPersistenceService;

    private JvmPersistenceService jvmPersistenceService;

    private WebServerPersistenceService webServerPersistenceService;

    @Value("${paths.resource-types}")
    private String templatePath;

    public ResourceServiceImpl(
            final FileManager theFileManager,
            final HarmonyTemplateEngine harmonyTemplateEngine,
            final ResourcePersistenceService resourcePersistenceService,
            final GroupPersistenceService groupPersistenceService,
            final ApplicationPersistenceService applicationPersistenceService,
            final ApplicationService applicationService,
            final JvmPersistenceService jvmPersistenceService,
            final WebServerPersistenceService webServerPersistenceService) {
        fileManager = theFileManager;
        templateEngine = harmonyTemplateEngine;
        this.resourcePersistenceService = resourcePersistenceService;
        this.groupPersistenceService = groupPersistenceService;
        expressionParser = new SpelExpressionParser();
        encryptExpression = expressionParser.parseExpression(encryptExpressionString);
        this.applicationPersistenceService = applicationPersistenceService;
        this.applicationService = applicationService;
        this.jvmPersistenceService = jvmPersistenceService;
        this.webServerPersistenceService = webServerPersistenceService;
    }

    @Override
    public Collection<ResourceType> getResourceTypes() {
        try {
            Collection<ResourceType> resourceTypes = fileManager.getResourceTypes();
            for (ResourceType rtype : resourceTypes) {
                if (rtype.isValid()) {
                    HarmonyTemplate template = templateEngine.getTemplate(rtype);
                    try {
                        template.check();
                    } catch (Exception exception) {
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
    @Transactional(readOnly = true)
    public ResourceInstance getResourceInstance(final Identifier<ResourceInstance> aResourceInstanceId) {
        return this.resourcePersistenceService.getResourceInstance(aResourceInstanceId);
    }

    @Override
    @Transactional(readOnly = true)
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
        ResourceInstance resourceInstance = this.getResourceInstanceByGroupNameAndName(groupName, resourceInstanceName);
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
    public CreateResourceTemplateApplicationResponseWrapper createTemplate(final InputStream metaData,
                                                                           final InputStream templateData,
                                                                           final String targetName) {
        final ObjectMapper mapper = new ObjectMapper();
        final ResourceTemplateMetaData resourceTemplateMetaData;
        final CreateResourceTemplateApplicationResponseWrapper responseWrapper;

        try {
            resourceTemplateMetaData = mapper.readValue(IOUtils.toString(metaData), ResourceTemplateMetaData.class);

            // Let's create the template!
            final EntityType entityType = EntityType.fromValue(resourceTemplateMetaData.getEntity().getType());
            switch (entityType) {
                case JVM:
                    responseWrapper = createJvmTemplate(resourceTemplateMetaData, templateData, targetName);
                    break;
                case GROUPED_JVMS:
                    responseWrapper = createGroupedJvmsTemplate(resourceTemplateMetaData, templateData);
                    break;
                case WEB_SERVER:
                    responseWrapper = createWebServerTemplate(resourceTemplateMetaData, templateData, targetName);
                    break;
                case GROUPED_WEBSERVERS:
                    responseWrapper = createGroupedWebServersTemplate(resourceTemplateMetaData, templateData);
                    break;
                case APP:
                    responseWrapper = createApplicationTemplate(resourceTemplateMetaData, templateData, targetName);
                    break;
                case GROUPED_APPS:
                    responseWrapper = createGroupedApplicationsTemplate(resourceTemplateMetaData, templateData, targetName);
                    break;
                default:
                    throw new ResourceServiceException("Invalid entity type '" + resourceTemplateMetaData.getEntity().getType() + "'");
            }
        } catch (final IOException ioe) {
            throw new ResourceServiceException(ioe);
        }

        return responseWrapper;
    }

    /**
     * Create the JVM template in the db and in the templates path for a specific JVM entity target.
     * @param metaData the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData the template content/data
     * @param jvmName identifies the JVM to which the template is attached to
     */
    private CreateResourceTemplateApplicationResponseWrapper createJvmTemplate(final ResourceTemplateMetaData metaData,
                                                                               final InputStream templateData,
                                                                               final String jvmName) {
        final Jvm jvm = jvmPersistenceService.findJvmByExactName(jvmName);
        final Group parentGroup = groupPersistenceService.getGroup(metaData.getEntity().getGroup());
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
                jvm.getErrorStatus());
        final UploadJvmConfigTemplateRequest uploadJvmTemplateRequest = new UploadJvmConfigTemplateRequest(jvmWithParentGroup, metaData.getTemplateName(),
                templateData, convertResourceTemplateMetaDataToJson(metaData));
        uploadJvmTemplateRequest.setConfFileName(metaData.getDeployFileName());
        return new CreateResourceTemplateApplicationResponseWrapper(jvmPersistenceService.uploadJvmTemplateXml(uploadJvmTemplateRequest));
    }

    protected String convertResourceTemplateMetaDataToJson(final ResourceTemplateMetaData resourceTemplateMetaData) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(resourceTemplateMetaData);
        } catch (final IOException ioe) {
            throw new ResourceServiceException(ioe);
        }
    }

    /**
     * Create the JVM template in the db and in the templates path for all the JVMs.
     *
     * @param metaData     the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData the template content/data
     */
    // TODO: When the resource file is locked, don't overwrite!
    private CreateResourceTemplateApplicationResponseWrapper createGroupedJvmsTemplate(final ResourceTemplateMetaData metaData,
                                                                                       final InputStream templateData) throws IOException {
        final Set<Jvm> jvms = groupPersistenceService.getGroup(metaData.getEntity().getGroup()).getJvms();
        ConfigTemplate createdJpaJvmConfigTemplate = null;
        String templateContent = IOUtils.toString(templateData);

        for (final Jvm jvm : jvms) {
            UploadJvmConfigTemplateRequest uploadJvmTemplateRequest = new UploadJvmConfigTemplateRequest(jvm, metaData.getTemplateName(),
                    new ByteArrayInputStream(templateContent.getBytes()), convertResourceTemplateMetaDataToJson(metaData));
            uploadJvmTemplateRequest.setConfFileName(metaData.getDeployFileName());

            // Since we're just creating the same template for all the JVMs, we just keep one copy of the created
            // configuration template.
            createdJpaJvmConfigTemplate = jvmPersistenceService.uploadJvmTemplateXml(uploadJvmTemplateRequest);
        }
        final List<UploadJvmTemplateRequest> uploadJvmTemplateRequestList = new ArrayList<>();
        UploadJvmConfigTemplateRequest uploadJvmTemplateRequest = new UploadJvmConfigTemplateRequest(null, metaData.getTemplateName(),
                new ByteArrayInputStream(templateContent.getBytes()), convertResourceTemplateMetaDataToJson(metaData));
        uploadJvmTemplateRequest.setConfFileName(metaData.getDeployFileName());
        uploadJvmTemplateRequestList.add(uploadJvmTemplateRequest);
        groupPersistenceService.populateGroupJvmTemplates(metaData.getEntity().getGroup(), uploadJvmTemplateRequestList);
        return new CreateResourceTemplateApplicationResponseWrapper(createdJpaJvmConfigTemplate);
    }

    /**
     * Create the web server template in the db and in the templates path for a specific web server entity target.
     *  @param metaData the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData the template content/data
     * @param webServerName identifies the web server to which the template belongs to
     */
    private CreateResourceTemplateApplicationResponseWrapper createWebServerTemplate(final ResourceTemplateMetaData metaData,
                                                                                     final InputStream templateData,
                                                                                     final String webServerName) {
        final WebServer webServer = webServerPersistenceService.findWebServerByName(webServerName);
        final UploadWebServerTemplateRequest uploadWebArchiveRequest = new UploadWebServerTemplateRequest(webServer,
                metaData.getTemplateName(), convertResourceTemplateMetaDataToJson(metaData), templateData) {
            @Override
            public String getConfFileName() {
                return metaData.getDeployFileName();
            }
        };
        return new CreateResourceTemplateApplicationResponseWrapper(webServerPersistenceService.uploadWebserverConfigTemplate(uploadWebArchiveRequest));
    }

    /**
     * Create the web server template in the db and in the templates path for all the web servers.
     *
     * @param metaData     the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData the template content/data
     */
    private CreateResourceTemplateApplicationResponseWrapper createGroupedWebServersTemplate(final ResourceTemplateMetaData metaData,
                                                                                             final InputStream templateData) throws IOException {
        final Group group = groupPersistenceService.getGroupWithWebServers(metaData.getEntity().getGroup());
        final Set<WebServer> webServers = group.getWebServers();
        final List<UploadWebServerTemplateRequest> uploadWebServerTemplateRequestList = new ArrayList<>();
        ConfigTemplate createdConfigTemplate = null;
        String templateContent = IOUtils.toString(templateData);
        for (final WebServer webServer : webServers) {
            UploadWebServerTemplateRequest uploadWebServerTemplateRequest = new UploadWebServerTemplateRequest(webServer,
                    metaData.getTemplateName(), convertResourceTemplateMetaDataToJson(metaData), new ByteArrayInputStream(templateContent.getBytes())) {
                @Override
                public String getConfFileName() {
                    return metaData.getDeployFileName();
                }
            };

            // Since we're just creating the same template for all the JVMs, we just keep one copy of the created
            // configuration template.
            createdConfigTemplate = webServerPersistenceService.uploadWebserverConfigTemplate(uploadWebServerTemplateRequest);
        }

        UploadWebServerTemplateRequest uploadWebServerTemplateRequest = new UploadWebServerTemplateRequest(null,
                metaData.getTemplateName(), convertResourceTemplateMetaDataToJson(metaData), new ByteArrayInputStream(templateContent.getBytes())) {
            @Override
            public String getConfFileName() {
                return metaData.getDeployFileName();
            }
        };
        uploadWebServerTemplateRequestList.add(uploadWebServerTemplateRequest);
        groupPersistenceService.populateGroupWebServerTemplates(group.getName(), uploadWebServerTemplateRequestList);
        return new CreateResourceTemplateApplicationResponseWrapper(createdConfigTemplate);
    }

    /**
     * Create the application template in the db and in the templates path for a specific application entity target.
     *
     * @param metaData      the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData  the template content/data
     * @param targetJvmName the name of the JVM to associate with the application template
     */
    private CreateResourceTemplateApplicationResponseWrapper createApplicationTemplate(final ResourceTemplateMetaData metaData, final InputStream templateData, String targetJvmName) {
        final Application application = applicationPersistenceService.getApplication(metaData.getEntity().getTarget());
        UploadAppTemplateRequest uploadAppTemplateRequest = new UploadAppTemplateRequest(application, metaData.getTemplateName(),
                metaData.getDeployFileName(), targetJvmName, convertResourceTemplateMetaDataToJson(metaData), templateData);
        return new CreateResourceTemplateApplicationResponseWrapper(applicationService.uploadAppTemplate(uploadAppTemplateRequest));
    }

    /**
     * Create the application template in the db and in the templates path for all the application.
     *
     * @param metaData      the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData  the template content/data
     * @param targetAppName the application name
     */
    private CreateResourceTemplateApplicationResponseWrapper createGroupedApplicationsTemplate(final ResourceTemplateMetaData metaData,
                                                                                               final InputStream templateData,
                                                                                               final String targetAppName) throws IOException {
        final String groupName = metaData.getEntity().getGroup();
        Group group = groupPersistenceService.getGroup(groupName);
        final List<Application> applications = applicationPersistenceService.findApplicationsBelongingTo(groupName);
        ConfigTemplate createdConfigTemplate = null;
        String templateString = IOUtils.toString(templateData);
        for (final Application application : applications) {
            if (metaData.getEntity().getDeployToJvms() && application.getName().equals(targetAppName)) {
                final byte[] bytes = templateString.getBytes();
                for (final Jvm jvm : group.getJvms()) {
                    UploadAppTemplateRequest uploadAppTemplateRequest = new UploadAppTemplateRequest(application, metaData.getTemplateName(),
                            metaData.getDeployFileName(), jvm.getJvmName(), convertResourceTemplateMetaDataToJson(metaData), new ByteArrayInputStream(bytes)
                    );
                    applicationService.uploadAppTemplate(uploadAppTemplateRequest);
                }
            }
        }

        createdConfigTemplate = groupPersistenceService.populateGroupAppTemplate(group, metaData.getDeployFileName(),
                convertResourceTemplateMetaDataToJson(metaData), templateString);

        return new CreateResourceTemplateApplicationResponseWrapper(createdConfigTemplate);
    }

    @Override
    @Transactional
    public int removeTemplate(final String name) {
        return applicationPersistenceService.removeTemplate(name) + jvmPersistenceService.removeTemplate(name)
                + webServerPersistenceService.removeTemplate(name) + groupPersistenceService.removeAppTemplate(name) +
                groupPersistenceService.removeJvmTemplate(name) + groupPersistenceService.removeWeServerTemplate(name);
    }

    @Override
    public int removeTemplate(final String groupName, final EntityType entityType, final String templateNames) {
        final List<String> templateNameList = Arrays.asList(templateNames.split(","));
        int totalDeletedRecs = 0;
        for (final String templateName : templateNameList) {
            switch (entityType) {
                case GROUPED_JVMS:
                    totalDeletedRecs = groupPersistenceService.removeJvmTemplate(groupName, templateName);
                    break;
                case GROUPED_WEBSERVERS:
                    totalDeletedRecs = groupPersistenceService.removeWeServerTemplate(groupName, templateName);
                    break;
                default:
                    throw new ResourceServiceException("Invalid entity type parameter! Entity type can only be GROUPED_JVMS or GROUPED_WEBSERVERS.");
            }
        }
        return totalDeletedRecs;
    }

    @Override
    public int removeTemplate(final EntityType entityType, final String entityName, final String templateNames) {
        final List<String> templateNameList = Arrays.asList(templateNames.split(","));
        int totalDeletedRecs = 0;
        for (final String templateName : templateNameList) {
            switch (entityType) {
                case GROUPED_JVMS:
                    totalDeletedRecs = jvmPersistenceService.removeTemplate(entityName, templateName);
                    break;
                case GROUPED_WEBSERVERS:
                    totalDeletedRecs = webServerPersistenceService.removeTemplate(entityName, templateName);
                    break;
                default:
                    throw new ResourceServiceException("Invalid entity type parameter! Entity type can only be GROUPED_JVMS or GROUPED_WEBSERVERS.");
            }
        }
        return totalDeletedRecs;
    }

    @Override
    public ResourceGroup generateResourceGroup() {
        List<Group> groups = groupPersistenceService.getGroups();
        List<Group> groupsToBeAdded = null;
        for (Group group : groups) {
            if (groupsToBeAdded == null) {
                groupsToBeAdded = new ArrayList<>(groups.size());
            }
            List<Jvm> jvms = jvmPersistenceService.getJvmsAndWebAppsByGroupName(group.getName());
            List<WebServer> webServers = webServerPersistenceService.getWebServersByGroupName(group.getName());
            List<Application> applications = applicationPersistenceService.findApplicationsBelongingTo(group.getName());
            groupsToBeAdded.add(new Group(group.getId(),
                    group.getName(),
                    null != jvms ? new LinkedHashSet<>(jvms) : new LinkedHashSet<Jvm>(),
                    null != webServers ? new LinkedHashSet<>(webServers) : new LinkedHashSet<WebServer>(),
                    group.getCurrentState(),
                    group.getHistory(),
                    null != applications ? new LinkedHashSet<>(applications) : new LinkedHashSet<Application>()));
        }
        return new ResourceGroup(groupsToBeAdded);
    }

    @Override
    public <T> String generateResourceFile(final String template, final ResourceGroup resourceGroup, T selectedValue) {
        return ResourceFileGenerator.generateResourceConfig(template, resourceGroup, selectedValue);
    }
}
