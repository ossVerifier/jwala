package com.siemens.cto.aem.service.resource.impl;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.resource.*;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.properties.ExternalProperties;
import com.siemens.cto.aem.common.request.app.RemoveWebArchiveRequest;
import com.siemens.cto.aem.common.request.app.UploadAppTemplateRequest;
import com.siemens.cto.aem.common.request.app.UploadWebArchiveRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmConfigTemplateRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.common.request.resource.ResourceInstanceRequest;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.siemens.cto.aem.persistence.service.*;
import com.siemens.cto.aem.service.app.PrivateApplicationService;
import com.siemens.cto.aem.service.exception.ResourceServiceException;
import com.siemens.cto.aem.service.resource.ResourceHandler;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.template.ResourceFileGenerator;
import com.siemens.cto.toc.files.RepositoryFileInformation;
import com.siemens.cto.toc.files.WebArchiveManager;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
    public static final String WAR_FILE_EXTENSION = ".war";
    public static final String ERR_DELETING_WAR_MSG = "Error deleting WAR file resource!";

    private final SpelExpressionParser expressionParser;
    private final Expression encryptExpression;
    private final ResourcePersistenceService resourcePersistenceService;
    private final GroupPersistenceService groupPersistenceService;
    private PrivateApplicationService privateApplicationService;

    private final String encryptExpressionString = "new com.siemens.cto.infrastructure.StpCryptoService().encryptToBase64( #stringToEncrypt )";

    private ApplicationPersistenceService applicationPersistenceService;

    private JvmPersistenceService jvmPersistenceService;

    private WebServerPersistenceService webServerPersistenceService;

    private final ResourceDao resourceDao;

    private final WebArchiveManager webArchiveManager;

    private final ResourceHandler resourceHandler;

    @Value("${paths.resource-templates}")
    private String templatePath;

    public ResourceServiceImpl(final ResourcePersistenceService resourcePersistenceService,
                               final GroupPersistenceService groupPersistenceService,
                               final ApplicationPersistenceService applicationPersistenceService,
                               final JvmPersistenceService jvmPersistenceService,
                               final WebServerPersistenceService webServerPersistenceService,
                               final PrivateApplicationService privateApplicationService,
                               final ResourceDao resourceDao,
                               final WebArchiveManager webArchiveManager,
                               final ResourceHandler resourceHandler) {
        this.resourcePersistenceService = resourcePersistenceService;
        this.groupPersistenceService = groupPersistenceService;
        this.privateApplicationService = privateApplicationService;
        expressionParser = new SpelExpressionParser();
        encryptExpression = expressionParser.parseExpression(encryptExpressionString);
        this.applicationPersistenceService = applicationPersistenceService;
        this.jvmPersistenceService = jvmPersistenceService;
        this.webServerPersistenceService = webServerPersistenceService;
        this.resourceDao = resourceDao;
        this.webArchiveManager = webArchiveManager;
        this.resourceHandler = resourceHandler;
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
    public String encryptUsingPlatformBean(String cleartext) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("stringToEncrypt", cleartext);
        return encryptExpression.getValue(context, String.class);
    }

    @Override
    @Transactional
    public CreateResourceTemplateApplicationResponseWrapper createTemplate(final InputStream metaData,
                                                                           InputStream templateData,
                                                                           final String targetName,
                                                                           final User user) {
        final ObjectMapper mapper = new ObjectMapper();
        final ResourceTemplateMetaData resourceTemplateMetaData;
        final CreateResourceTemplateApplicationResponseWrapper responseWrapper;

        try {
            resourceTemplateMetaData = mapper.readValue(IOUtils.toString(metaData), ResourceTemplateMetaData.class);
            if (resourceTemplateMetaData.getContentType().equals(ContentType.APPLICATION_BINARY.contentTypeStr)){
                // TODO create new API that doesn't use UploadWebArchiveRequest - just do this for now
                Application fakedApplication = new Application(new Identifier<Application>(0L), resourceTemplateMetaData.getDeployFileName(), resourceTemplateMetaData.getDeployPath(), "", null, true, true, false, resourceTemplateMetaData.getDeployFileName());
                UploadWebArchiveRequest uploadWebArchiveRequest = new UploadWebArchiveRequest( fakedApplication, resourceTemplateMetaData.getDeployFileName(), -1L, templateData);
                RepositoryFileInformation fileInfo = privateApplicationService.uploadWebArchiveData(uploadWebArchiveRequest);

                templateData = new ByteArrayInputStream(fileInfo.getPath().toString().getBytes());
            }

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
                    responseWrapper = createWebServerTemplate(resourceTemplateMetaData, templateData, targetName, user);
                    break;
                case GROUPED_WEBSERVERS:
                    responseWrapper = createGroupedWebServersTemplate(resourceTemplateMetaData, templateData, user);
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

    @Override
    @Transactional
    public CreateResourceTemplateApplicationResponseWrapper createJvmResource(final ResourceTemplateMetaData metaData,
                                                                              InputStream templateData,
                                                                              final String jvmName) {

        templateData = uploadIfBinaryData(metaData, templateData);

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
                                               jvm.getErrorStatus(),
                                               jvm.getLastUpdatedDate(),
                jvm.getUserName(),
                jvm.getEncryptedPassword());

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

    @Override
    // TODO: When the resource file is locked, don't overwrite!
    @Transactional
    public CreateResourceTemplateApplicationResponseWrapper createGroupLevelJvmResource(final ResourceTemplateMetaData metaData,
                                                                                        InputStream resourceData,
                                                                                        final String groupName) throws IOException {

        resourceData = uploadIfBinaryData(metaData, resourceData);

        final Set<Jvm> jvms = groupPersistenceService.getGroup(groupName).getJvms();
        ConfigTemplate createdJpaJvmConfigTemplate = null;
        String templateContent = IOUtils.toString(resourceData);

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

    @Override
    @Transactional
    public CreateResourceTemplateApplicationResponseWrapper createWebServerResource(final ResourceTemplateMetaData metaData,
                                                                                    InputStream templateData,
                                                                                    final String webServerName,
                                                                                    final User user) {

        templateData = uploadIfBinaryData(metaData, templateData);

        final WebServer webServer = webServerPersistenceService.findWebServerByName(webServerName);
        final UploadWebServerTemplateRequest uploadWebArchiveRequest = new UploadWebServerTemplateRequest(webServer,
                metaData.getTemplateName(), convertResourceTemplateMetaDataToJson(metaData), templateData) {
            @Override
            public String getConfFileName() {
                return metaData.getDeployFileName();
            }
        };
        final String generatedDeployPath = generateResourceFile(metaData.getDeployPath(), generateResourceGroup(), webServer);
        return new CreateResourceTemplateApplicationResponseWrapper(webServerPersistenceService
                .uploadWebServerConfigTemplate(uploadWebArchiveRequest, generatedDeployPath + "/" + metaData.getDeployFileName(),user.getId()));
    }

    @Override
    @Transactional
    public CreateResourceTemplateApplicationResponseWrapper createGroupLevelWebServerResource(final ResourceTemplateMetaData metaData,
                                                                                              InputStream templateData,
                                                                                              final String groupName,
                                                                                              final User user) throws IOException {

        templateData = uploadIfBinaryData(metaData, templateData);

        final Group group = groupPersistenceService.getGroupWithWebServers(groupName);
        final Set<WebServer> webServers = group.getWebServers();
        final Map<String, UploadWebServerTemplateRequest> uploadWebServerTemplateRequestMap = new HashMap<>();
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
            String generatedDeployPath = generateResourceFile(metaData.getDeployPath(), generateResourceGroup(), webServer);
            createdConfigTemplate = webServerPersistenceService.uploadWebServerConfigTemplate(uploadWebServerTemplateRequest, generatedDeployPath + "/" + metaData.getDeployFileName(), user.getId());
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
        return new CreateResourceTemplateApplicationResponseWrapper(createdConfigTemplate);
    }

    @Override
    @Transactional
    public CreateResourceTemplateApplicationResponseWrapper createAppResource(final ResourceTemplateMetaData metaData,
                                                                              InputStream templateData,
                                                                              final String jvmName,
                                                                              final String appName) {

        templateData = uploadIfBinaryData(metaData, templateData);

        final Application application = applicationPersistenceService.getApplication(appName);
        final UploadAppTemplateRequest uploadAppTemplateRequest = new UploadAppTemplateRequest(application, metaData.getTemplateName(),
                metaData.getDeployFileName(), jvmName, convertResourceTemplateMetaDataToJson(metaData), templateData);
        final JpaJvm jpaJvm = jvmPersistenceService.getJpaJvm(jvmPersistenceService.findJvmByExactName(jvmName).getId(), false);
        return new CreateResourceTemplateApplicationResponseWrapper(applicationPersistenceService.uploadAppTemplate(uploadAppTemplateRequest, jpaJvm));
    }

    @Override
    @Transactional
    public CreateResourceTemplateApplicationResponseWrapper createGroupedLevelAppResource(final ResourceTemplateMetaData metaData,
                                                                                          InputStream templateData,
                                                                                          final String targetAppName) throws IOException {
        templateData = uploadIfBinaryData(metaData, templateData);

        final String groupName = metaData.getEntity().getGroup();
        final Group group = groupPersistenceService.getGroup(groupName);
        final ConfigTemplate createdConfigTemplate;
        final String templateString = IOUtils.toString(templateData);

        if (metaData.getContentType().equals(ContentType.APPLICATION_BINARY.contentTypeStr) &&
                templateString.toLowerCase().endsWith(WAR_FILE_EXTENSION)){
            final Application app = applicationPersistenceService.getApplication(targetAppName);
            if (StringUtils.isEmpty(app.getWarName())) {
                applicationPersistenceService.updateWarInfo(targetAppName, metaData.getTemplateName(), templateString);
            } else {
                throw new ResourceServiceException("A web application can only have 1 war file. To change it, delete the war file first before uploading a new one.");
            }
        }

        createdConfigTemplate = groupPersistenceService.populateGroupAppTemplate(groupName, targetAppName, metaData.getDeployFileName(),
                convertResourceTemplateMetaDataToJson(metaData), templateString);

        // Can't we just get the application using the group name and target app name instead of getting all the applications
        // then iterating it to compare with the target app name ???
        // If we can do that then TODO: Refactor this to return only one application and remove the iteration!
        final List<Application> applications = applicationPersistenceService.findApplicationsBelongingTo(groupName);

        for (final Application application : applications) {
            if (metaData.getEntity().getDeployToJvms() && application.getName().equals(targetAppName)) {
                final byte[] bytes = templateString.getBytes();
                for (final Jvm jvm : group.getJvms()) {
                    UploadAppTemplateRequest uploadAppTemplateRequest = new UploadAppTemplateRequest(application, metaData.getTemplateName(),
                            metaData.getDeployFileName(), jvm.getJvmName(), convertResourceTemplateMetaDataToJson(metaData), new ByteArrayInputStream(bytes)
                    );
                    JpaJvm jpaJvm = jvmPersistenceService.getJpaJvm(jvm.getId(), false);
                    applicationPersistenceService.uploadAppTemplate(uploadAppTemplateRequest, jpaJvm);
                }
            }
        }

        return new CreateResourceTemplateApplicationResponseWrapper(createdConfigTemplate);
    }

    /**
     * If content type is binary, the binary data (e.g. war) is uploaded and returns the upload path.
     * @param resourceTemplateMetaData the meta data
     * @param resourceDataIn the resource data input stream
     * @return upload path if the file is binary else it just returns the resource data input stream
     */
    private InputStream uploadIfBinaryData(final ResourceTemplateMetaData resourceTemplateMetaData, final InputStream resourceDataIn) {
        if (resourceTemplateMetaData.getContentType().equals(ContentType.APPLICATION_BINARY.contentTypeStr)){
            // TODO create new API that doesn't use UploadWebArchiveRequest - just do this for now
            Application fakedApplication = new Application(new Identifier<Application>(0L), resourceTemplateMetaData.getDeployFileName(),
                    resourceTemplateMetaData.getDeployPath(), "", null, true, true, false, resourceTemplateMetaData.getDeployFileName());
            UploadWebArchiveRequest uploadWebArchiveRequest = new UploadWebArchiveRequest(fakedApplication,
                    resourceTemplateMetaData.getDeployFileName(), -1L, resourceDataIn);
            RepositoryFileInformation fileInfo = privateApplicationService.uploadWebArchiveData(uploadWebArchiveRequest);

            return new ByteArrayInputStream(fileInfo.getPath().toString().getBytes());
        }
        return resourceDataIn;
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

    @Override
    public List<String> getApplicationResourceNames(final String groupName, final String appName) {
        return resourcePersistenceService.getApplicationResourceNames(groupName, appName);
    }

    @Override
    public String getAppTemplate(final String groupName, final String appName, final String templateName) {
        return resourcePersistenceService.getAppTemplate(groupName, appName, templateName);
    }

    @Override
    public Map<String, String> checkFileExists(final String groupName, final String jvmName, final String webappName, final String webserverName, final String fileName) {
        boolean resultBoolean = false;
        if(groupName!=null && !groupName.isEmpty() && fileName!=null && !fileName.isEmpty()) {
            if(jvmName!=null && !jvmName.isEmpty()) {
                // Search for file in jvms
                LOGGER.debug("searching for resource {} in group {} and jvm {}", fileName, groupName, jvmName);
                resultBoolean = groupPersistenceService.checkGroupJvmResourceFileName(groupName, fileName) ||
                        jvmPersistenceService.checkJvmResourceFileName(groupName, jvmName, fileName);
            } else if(webappName!=null && !webappName.isEmpty()) {
                // Search for file in webapps
                LOGGER.debug("searching for resource {} in group {} and webapp {}", fileName, groupName, webappName);
                resultBoolean = groupPersistenceService.checkGroupAppResourceFileName(groupName, fileName) ||
                        applicationPersistenceService.checkAppResourceFileName(groupName, webappName, fileName);
            } else if(webserverName!=null && !webserverName.isEmpty()) {
                // Search for file in webservers
                LOGGER.debug("searching for resource {} in group {} and webserver {}", fileName, groupName, webserverName);
                resultBoolean = groupPersistenceService.checkGroupWebServerResourceFileName(groupName, fileName) ||
                        webServerPersistenceService.checkWebServerResourceFileName(groupName, webserverName, fileName);
            }
        }
        Map<String, String> result = new HashMap<>();
        result.put("fileName", fileName);
        result.put("exists", Boolean.toString(resultBoolean));
        LOGGER.debug("result: {}", result.toString());
        return result;
    }

    @Override
    @Transactional
    public int deleteWebServerResource(final String templateName, final String webServerName) {
        return resourceDao.deleteWebServerResource(templateName, webServerName);
    }

    @Override
    @Transactional
    public int deleteGroupLevelWebServerResource(final String templateName, final String groupName) {
        return resourceDao.deleteGroupLevelWebServerResource(templateName, groupName);
    }

    @Override
    @Transactional
    public int deleteJvmResource(final String templateName, final String jvmName) {
        return resourceDao.deleteJvmResource(templateName, jvmName);
    }

    @Override
    @Transactional
    public int deleteGroupLevelJvmResource(final String templateName, final String groupName) {
        return resourceDao.deleteGroupLevelJvmResource(templateName, groupName);
    }

    @Override
    @Transactional
    public int deleteAppResource(final String templateName, final String appName, final String jvmName) {
        return resourceDao.deleteAppResource(templateName, appName, jvmName);
    }

    @Override
    @Transactional
    public int deleteGroupLevelAppResource(final String appName, final String templateName) {
        final Application application = applicationPersistenceService.getApplication(appName);
        return resourceDao.deleteGroupLevelAppResource(application.getName(), application.getGroup().getName(), templateName);
    }


    /****** OLD CREATE RESOURCE PRIVATE METHODS TO BE REMOVED AFTER THE NEW CREATE RESOURCE HAS BEEN IMPLEMENTED ******/

    /**
     * Create the JVM template in the db and in the templates path for a specific JVM entity target.
     * @param metaData the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData the template content/data
     * @param jvmName identifies the JVM to which the template is attached to
     */
    @Deprecated
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
                jvm.getErrorStatus(),
                jvm.getLastUpdatedDate(),
                jvm.getUserName(),
                jvm.getEncryptedPassword());

        final UploadJvmConfigTemplateRequest uploadJvmTemplateRequest = new UploadJvmConfigTemplateRequest(jvmWithParentGroup, metaData.getTemplateName(),
                templateData, convertResourceTemplateMetaDataToJson(metaData));
        uploadJvmTemplateRequest.setConfFileName(metaData.getDeployFileName());
        return new CreateResourceTemplateApplicationResponseWrapper(jvmPersistenceService.uploadJvmTemplateXml(uploadJvmTemplateRequest));
    }

    /**
     * Create the JVM template in the db and in the templates path for all the JVMs.
     *
     * @param metaData     the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData the template content/data
     */
    // TODO: When the resource file is locked, don't overwrite!
    @Deprecated
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
     * @param metaData the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData the template content/data
     * @param webServerName identifies the web server to which the template belongs to
     * @param user
     */
    @Deprecated
    private CreateResourceTemplateApplicationResponseWrapper createWebServerTemplate(final ResourceTemplateMetaData metaData,
                                                                                     final InputStream templateData,
                                                                                     final String webServerName,
                                                                                     final User user) {
        final WebServer webServer = webServerPersistenceService.findWebServerByName(webServerName);
        final UploadWebServerTemplateRequest uploadWebArchiveRequest = new UploadWebServerTemplateRequest(webServer,
                metaData.getTemplateName(), convertResourceTemplateMetaDataToJson(metaData), templateData) {
            @Override
            public String getConfFileName() {
                return metaData.getDeployFileName();
            }
        };
        String generatedDeployPath = generateResourceFile(metaData.getDeployPath(), generateResourceGroup(), webServer);
        return new CreateResourceTemplateApplicationResponseWrapper(webServerPersistenceService.uploadWebServerConfigTemplate(uploadWebArchiveRequest, generatedDeployPath + "/" + metaData.getDeployFileName(),user.getId()));
    }

    /**
     * Create the web server template in the db and in the templates path for all the web servers.
     *  @param metaData     the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData the template content/data
     * @param user
     */
    @Deprecated
    private CreateResourceTemplateApplicationResponseWrapper createGroupedWebServersTemplate(final ResourceTemplateMetaData metaData,
                                                                                             final InputStream templateData,
                                                                                             final User user) throws IOException {
        final Group group = groupPersistenceService.getGroupWithWebServers(metaData.getEntity().getGroup());
        final Set<WebServer> webServers = group.getWebServers();
        final Map<String, UploadWebServerTemplateRequest> uploadWebServerTemplateRequestMap = new HashMap<>();
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
            String generatedDeployPath = generateResourceFile(metaData.getDeployPath(), generateResourceGroup(), webServer);
            createdConfigTemplate = webServerPersistenceService.uploadWebServerConfigTemplate(uploadWebServerTemplateRequest, generatedDeployPath + "/" + metaData.getDeployFileName(), user.getId());
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
        return new CreateResourceTemplateApplicationResponseWrapper(createdConfigTemplate);
    }

    /**
     * Create the application template in the db and in the templates path for a specific application entity target.
     *
     * @param metaData      the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData  the template content/data
     * @param targetJvmName the name of the JVM to associate with the application template
     */
    @Deprecated
    private CreateResourceTemplateApplicationResponseWrapper createApplicationTemplate(final ResourceTemplateMetaData metaData, final InputStream templateData, String targetJvmName) {
        final Application application = applicationPersistenceService.getApplication(metaData.getEntity().getTarget());
        UploadAppTemplateRequest uploadAppTemplateRequest = new UploadAppTemplateRequest(application, metaData.getTemplateName(),
                metaData.getDeployFileName(), targetJvmName, convertResourceTemplateMetaDataToJson(metaData), templateData);
        JpaJvm jpaJvm = jvmPersistenceService.getJpaJvm(jvmPersistenceService.findJvmByExactName(targetJvmName).getId(), false);
        return new CreateResourceTemplateApplicationResponseWrapper(applicationPersistenceService.uploadAppTemplate(uploadAppTemplateRequest, jpaJvm));
    }

    /**
     * Create the application template in the db and in the templates path for all the application.
     *
     * @param metaData      the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData  the template content/data
     * @param targetAppName the application name
     */
    @Deprecated
    private CreateResourceTemplateApplicationResponseWrapper createGroupedApplicationsTemplate(final ResourceTemplateMetaData metaData,
                                                                                               final InputStream templateData,
                                                                                               final String targetAppName) throws IOException {
        final String groupName = metaData.getEntity().getGroup();
        Group group = groupPersistenceService.getGroup(groupName);
        final List<Application> applications = applicationPersistenceService.findApplicationsBelongingTo(groupName);
        ConfigTemplate createdConfigTemplate = null;
        String templateString = IOUtils.toString(templateData);

        if (ContentType.APPLICATION_BINARY.contentTypeStr.equalsIgnoreCase(metaData.getContentType()) &&
            metaData.getTemplateName().toLowerCase().endsWith(WAR_FILE_EXTENSION)) {
                applicationPersistenceService.updateWarInfo(targetAppName, metaData.getTemplateName(), templateString);
        }

        for (final Application application : applications) {
            if (metaData.getEntity().getDeployToJvms() && application.getName().equals(targetAppName)) {
                final byte[] bytes = templateString.getBytes();
                for (final Jvm jvm : group.getJvms()) {
                    UploadAppTemplateRequest uploadAppTemplateRequest = new UploadAppTemplateRequest(application, metaData.getTemplateName(),
                            metaData.getDeployFileName(), jvm.getJvmName(), convertResourceTemplateMetaDataToJson(metaData), new ByteArrayInputStream(bytes)
                    );
                    JpaJvm jpaJvm = jvmPersistenceService.getJpaJvm(jvm.getId(), false);
                    applicationPersistenceService.uploadAppTemplate(uploadAppTemplateRequest, jpaJvm);
                }
            }
        }

        createdConfigTemplate = groupPersistenceService.populateGroupAppTemplate(groupName, targetAppName, metaData.getDeployFileName(),
                convertResourceTemplateMetaDataToJson(metaData), templateString);

        return new CreateResourceTemplateApplicationResponseWrapper(createdConfigTemplate);
    }

    @Override
    @Transactional
    public int deleteWebServerResources(List<String> templateNameList, String webServerName) {
        return resourceDao.deleteWebServerResources(templateNameList, webServerName);
    }

    @Override
    @Transactional
    public int deleteGroupLevelWebServerResources(List<String> templateNameList, String groupName) {
        return resourceDao.deleteGroupLevelWebServerResources(templateNameList, groupName);
    }

    @Override
    @Transactional
    public int deleteJvmResources(List<String> templateNameList, String jvmName) {
        return resourceDao.deleteJvmResources(templateNameList, jvmName);
    }

    @Override
    @Transactional
    public int deleteGroupLevelJvmResources(List<String> templateNameList, String groupName) {
        return resourceDao.deleteGroupLevelJvmResources(templateNameList, groupName);
    }

    @Override
    @Transactional
    public int deleteAppResources(List<String> templateNameList, String appName, String jvmName) {
        return resourceDao.deleteAppResources(templateNameList, appName, jvmName);
    }

    @Override
    @Transactional
    public int deleteGroupLevelAppResources(final String appName, final String groupName, final List<String> templateNameList) {
        final int deletedCount = resourceDao.deleteGroupLevelAppResources(appName, groupName, templateNameList);
        if (deletedCount > 0) {
            final List<Jvm> jvms = jvmPersistenceService.getJvmsByGroupName(groupName);
            for(Jvm jvm:jvms) {
                resourceDao.deleteAppResources(templateNameList, appName, jvm.getJvmName());
            }
            for (final String templateName : templateNameList) {
                if (templateName.toLowerCase().endsWith(".war")) {
                    final Application app = applicationPersistenceService.getApplication(appName);
                    final RemoveWebArchiveRequest removeWarRequest = new RemoveWebArchiveRequest(app);

                    // An app is only assigned to one group as of July 7, 2016 so we don't need the group to delete the
                    // war info of an app
                    applicationPersistenceService.deleteWarInfo(appName);

                    try {
                        final RepositoryFileInformation result = webArchiveManager.remove(removeWarRequest);
                        if (result.getType() != RepositoryFileInformation.Type.DELETED) {
                            // If the physical file can't be deleted for some reason don't throw an exception since
                            // there's no outright ill effect if the war file is not removed in the file system.
                            // The said file might not exist anymore also which is the reason for the error.
                            LOGGER.error("Failed to delete the archive {}! WebArchiveManager remove result type = {}" , app.getWarPath(),
                                    result.getType());
                        }
                    } catch (final IOException ioe) {
                        // If the physical file can't be deleted for some reason don't throw an exception since
                        // there's no outright ill effect if the war file is not removed in the file system.
                        LOGGER.error("Failed to delete the archive {}!" , app.getWarPath(), ioe);
                    }
                }
            }
        }

        return deletedCount;
    }

    @Override
    public ResourceContent getResourceContent(final ResourceIdentifier resourceIdentifier) {
        final ConfigTemplate configTemplate = resourceHandler.fetchResource(resourceIdentifier);
        if (configTemplate != null) {
            return new ResourceContent(configTemplate.getMetaData(), configTemplate.getTemplateContent());
        }
        return null;
    }

    @Override
    public void uploadExternalProperties(String fileName, InputStream propertiesFileIn) {
        // TODO instead of uploading the file to the archive directory put it in the resource database
        Application fakedApplication = new Application(new Identifier<Application>(0L), fileName, "", "", null, true, true, false, fileName);
        UploadWebArchiveRequest uploadWebArchiveRequest = new UploadWebArchiveRequest(fakedApplication,fileName, -1L, propertiesFileIn);
        RepositoryFileInformation fileInfo = privateApplicationService.uploadWebArchiveData(uploadWebArchiveRequest);

        // upload the external properties file to the database
//        resourcePersistenceService.

        // TODO wait until properties file is deployed before setting the properties file path?
        String uploadFilePath = fileInfo.getPath().toString();
        ExternalProperties.setPropertiesFilePath(uploadFilePath);
    }

    @Override
    public String getExternalPropertiesFile() {
        // TODO add external property to a resource table
        return "external.properties";
    }

    @Override
    public Properties getExternalProperties() {
        return ExternalProperties.getProperties();
    }
}
