package com.siemens.cto.aem.service.resource.impl;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.app.ApplicationControlOperation;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.resource.*;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.common.properties.ExternalProperties;
import com.siemens.cto.aem.common.request.app.RemoveWebArchiveRequest;
import com.siemens.cto.aem.common.request.app.UploadAppTemplateRequest;
import com.siemens.cto.aem.common.request.app.UploadWebArchiveRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmConfigTemplateRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.control.application.command.impl.WindowsApplicationPlatformCommandProvider;
import com.siemens.cto.aem.control.command.RemoteCommandExecutorImpl;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.ConfigTemplate;
import com.siemens.cto.aem.persistence.service.*;
import com.siemens.cto.aem.service.app.PrivateApplicationService;
import com.siemens.cto.aem.service.exception.ResourceServiceException;
import com.siemens.cto.aem.service.resource.ResourceHandler;
import com.siemens.cto.aem.service.resource.ResourceService;
import com.siemens.cto.aem.service.resource.impl.handler.exception.ResourceHandlerException;
import com.siemens.cto.aem.template.ResourceFileGenerator;
import com.siemens.cto.toc.files.RepositoryFileInformation;
import com.siemens.cto.toc.files.WebArchiveManager;
import org.apache.commons.io.FileUtils;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ResourceServiceImpl implements ResourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceServiceImpl.class);
    public static final String WAR_FILE_EXTENSION = ".war";
    public static final String ERR_DELETING_WAR_MSG = "Error deleting WAR file resource!";

    private final SpelExpressionParser expressionParser;
    private final Expression encryptExpression;
    private final ResourcePersistenceService resourcePersistenceService;
    private final GroupPersistenceService groupPersistenceService;
    private PrivateApplicationService privateApplicationService;
    // TODO replace ApplicationControlOperation (and all operation classes) with ResourceControlOperation
    private RemoteCommandExecutorImpl<ApplicationControlOperation> remoteCommandExecutor;
    private Map<String, ReentrantReadWriteLock> resourceWriteLockMap;

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
                               final ResourceHandler resourceHandler,
                               final RemoteCommandExecutorImpl remoteCommandExecutor,
                               final Map<String, ReentrantReadWriteLock> resourceWriteLockMap) {
        this.resourcePersistenceService = resourcePersistenceService;
        this.groupPersistenceService = groupPersistenceService;
        this.privateApplicationService = privateApplicationService;
        this.remoteCommandExecutor = remoteCommandExecutor;
        this.resourceWriteLockMap = resourceWriteLockMap;
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
    public String encryptUsingPlatformBean(String cleartext) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("stringToEncrypt", cleartext);
        return encryptExpression.getValue(context, String.class);
    }

    @Override
    @Transactional
    public CreateResourceResponseWrapper createTemplate(final InputStream metaData,
                                                        InputStream templateData,
                                                        final String targetName,
                                                        final User user) {
        final ObjectMapper mapper = new ObjectMapper();
        final ResourceTemplateMetaData resourceTemplateMetaData;
        final CreateResourceResponseWrapper responseWrapper;

        try {
            resourceTemplateMetaData = mapper.readValue(IOUtils.toString(metaData), ResourceTemplateMetaData.class);
            if (resourceTemplateMetaData.getContentType().equals(ContentType.APPLICATION_BINARY.contentTypeStr)) {
                // TODO create new API that doesn't use UploadWebArchiveRequest - just do this for now
                Application fakedApplication = new Application(new Identifier<Application>(0L), resourceTemplateMetaData.getDeployFileName(), resourceTemplateMetaData.getDeployPath(), "", null, true, true, false, resourceTemplateMetaData.getDeployFileName());
                UploadWebArchiveRequest uploadWebArchiveRequest = new UploadWebArchiveRequest(fakedApplication, resourceTemplateMetaData.getDeployFileName(), -1L, templateData);
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

    protected String convertResourceTemplateMetaDataToJson(final ResourceTemplateMetaData resourceTemplateMetaData) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(resourceTemplateMetaData);
        } catch (final IOException ioe) {
            throw new ResourceServiceException(ioe);
        }
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
        if (groupName != null && !groupName.isEmpty() && fileName != null && !fileName.isEmpty()) {
            if (jvmName != null && !jvmName.isEmpty()) {
                // Search for file in jvms
                LOGGER.debug("searching for resource {} in group {} and jvm {}", fileName, groupName, jvmName);
                resultBoolean = groupPersistenceService.checkGroupJvmResourceFileName(groupName, fileName) ||
                        jvmPersistenceService.checkJvmResourceFileName(groupName, jvmName, fileName);
            } else if (webappName != null && !webappName.isEmpty()) {
                // Search for file in webapps
                LOGGER.debug("searching for resource {} in group {} and webapp {}", fileName, groupName, webappName);
                resultBoolean = groupPersistenceService.checkGroupAppResourceFileName(groupName, fileName) ||
                        applicationPersistenceService.checkAppResourceFileName(groupName, webappName, fileName);
            } else if (webserverName != null && !webserverName.isEmpty()) {
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
     *
     * @param metaData     the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData the template content/data
     * @param jvmName      identifies the JVM to which the template is attached to
     */
    @Deprecated
    private CreateResourceResponseWrapper createJvmTemplate(final ResourceTemplateMetaData metaData,
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
        return new CreateResourceResponseWrapper(jvmPersistenceService.uploadJvmTemplateXml(uploadJvmTemplateRequest));
    }

    /**
     * Create the JVM template in the db and in the templates path for all the JVMs.
     *
     * @param metaData     the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData the template content/data
     */
    // TODO: When the resource file is locked, don't overwrite!
    @Deprecated
    private CreateResourceResponseWrapper createGroupedJvmsTemplate(final ResourceTemplateMetaData metaData,
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
        return new CreateResourceResponseWrapper(createdJpaJvmConfigTemplate);
    }

    /**
     * Create the web server template in the db and in the templates path for a specific web server entity target.
     *
     * @param metaData      the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData  the template content/data
     * @param webServerName identifies the web server to which the template belongs to
     * @param user
     */
    @Deprecated
    private CreateResourceResponseWrapper createWebServerTemplate(final ResourceTemplateMetaData metaData,
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
        return new CreateResourceResponseWrapper(webServerPersistenceService.uploadWebServerConfigTemplate(uploadWebArchiveRequest, generatedDeployPath + "/" + metaData.getDeployFileName(), user.getId()));
    }

    /**
     * Create the web server template in the db and in the templates path for all the web servers.
     *
     * @param metaData     the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData the template content/data
     * @param user
     */
    @Deprecated
    private CreateResourceResponseWrapper createGroupedWebServersTemplate(final ResourceTemplateMetaData metaData,
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
        return new CreateResourceResponseWrapper(createdConfigTemplate);
    }

    /**
     * Create the application template in the db and in the templates path for a specific application entity target.
     *
     * @param metaData      the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData  the template content/data
     * @param targetJvmName the name of the JVM to associate with the application template
     */
    @Deprecated
    private CreateResourceResponseWrapper createApplicationTemplate(final ResourceTemplateMetaData metaData, final InputStream templateData, String targetJvmName) {
        final Application application = applicationPersistenceService.getApplication(metaData.getEntity().getTarget());
        UploadAppTemplateRequest uploadAppTemplateRequest = new UploadAppTemplateRequest(application, metaData.getTemplateName(),
                metaData.getDeployFileName(), targetJvmName, convertResourceTemplateMetaDataToJson(metaData), templateData);
        JpaJvm jpaJvm = jvmPersistenceService.getJpaJvm(jvmPersistenceService.findJvmByExactName(targetJvmName).getId(), false);
        return new CreateResourceResponseWrapper(applicationPersistenceService.uploadAppTemplate(uploadAppTemplateRequest, jpaJvm));
    }

    /**
     * Create the application template in the db and in the templates path for all the application.
     *
     * @param metaData      the data that describes the template, please see {@link ResourceTemplateMetaData}
     * @param templateData  the template content/data
     * @param targetAppName the application name
     */
    @Deprecated
    private CreateResourceResponseWrapper createGroupedApplicationsTemplate(final ResourceTemplateMetaData metaData,
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

        return new CreateResourceResponseWrapper(createdConfigTemplate);
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
    public int deleteExternalProperties() {
        return resourceDao.deleteExternalProperties();
    }

    @Override
    @Transactional
    public int deleteGroupLevelAppResources(final String appName, final String groupName, final List<String> templateNameList) {
        final int deletedCount = resourceDao.deleteGroupLevelAppResources(appName, groupName, templateNameList);
        if (deletedCount > 0) {
            final List<Jvm> jvms = jvmPersistenceService.getJvmsByGroupName(groupName);
            for (Jvm jvm : jvms) {
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
                            LOGGER.error("Failed to delete the archive {}! WebArchiveManager remove result type = {}", app.getWarPath(),
                                    result.getType());
                        }
                    } catch (final IOException ioe) {
                        // If the physical file can't be deleted for some reason don't throw an exception since
                        // there's no outright ill effect if the war file is not removed in the file system.
                        LOGGER.error("Failed to delete the archive {}!", app.getWarPath(), ioe);
                    }
                }
            }
        }


        return deletedCount;
    }

    @Override
    @Transactional
    public ResourceContent getResourceContent(final ResourceIdentifier resourceIdentifier) {
        final ConfigTemplate configTemplate = resourceHandler.fetchResource(resourceIdentifier);
        if (configTemplate != null) {
            return new ResourceContent(configTemplate.getMetaData(), configTemplate.getTemplateContent());
        }
        return null;
    }

    @Override
    @Transactional
    public String updateResourceContent(ResourceIdentifier resourceIdentifier, String templateContent) {
        // TODO make this more generic for updating other resources
        // TODO user resource dao - not persistence
        resourcePersistenceService.updateResource(resourceIdentifier, EntityType.EXT_PROPERTIES, templateContent);
        // if the external properties resource was just saved then update properties
        // TODO should we be passing the EntityType here instead?
        checkResourceExternalProperties(resourceIdentifier, templateContent);
        final ConfigTemplate configTemplate = resourceHandler.fetchResource(resourceIdentifier);
        return configTemplate.getTemplateContent();
    }

    protected void checkResourceExternalProperties(ResourceIdentifier resourceIdentifier, String templateContent) {
        if (StringUtils.isNotEmpty(resourceIdentifier.resourceName) &&
                StringUtils.isEmpty(resourceIdentifier.webAppName) &&
                StringUtils.isEmpty(resourceIdentifier.jvmName) &&
                StringUtils.isEmpty(resourceIdentifier.groupName) &&
                StringUtils.isEmpty(resourceIdentifier.webServerName)) {
            ExternalProperties.loadFromInputStream(new ByteArrayInputStream(templateContent.getBytes()));
        }

    }

    @Override
    @Transactional
    // TODO make this more generic to use the new resources identifier
    // TODO move to the Resource DAO
    @Deprecated
    public Object uploadExternalProperties(String fileName, InputStream propertiesFileIn) {
        Long entityId = null;
        Long groupId = null;
        Long appId = null;
        // TODO user resource dao - not persistence
        return resourcePersistenceService.createResource(entityId, groupId, appId, EntityType.EXT_PROPERTIES, fileName, propertiesFileIn);

        // TODO wait until properties file is deployed before setting the properties file path?
//        String uploadFilePath = fileInfo.getPath().toString();
//        ExternalProperties.setPropertiesFilePath(uploadFilePath);
    }

    @Override
    public String previewResourceContent(ResourceIdentifier resourceIdentifier, String content) {
        // TODO make the selected value object non-null based on the resource identifier
        return generateResourceFile(content, generateResourceGroup(), null);
    }

    @Override
    public void deployTemplateToAllHosts(String fileName, ResourceIdentifier resourceIdentifier) {
        Set<String> allHosts = new TreeSet<>();
        for (Group group : groupPersistenceService.getGroups()) {
            allHosts.addAll(groupPersistenceService.getHosts(group.getName()));
        }
        for (String hostName : new ArrayList<>(allHosts)) {
            // TODO deploy each template on its own thread
            CommandOutput commandOutput = deployTemplateToHost(fileName, hostName, resourceIdentifier);
            if (!commandOutput.getReturnCode().wasSuccessful()) {
                LOGGER.error("Failed to deploy {} to host {}", fileName, hostName);
                throw new InternalErrorException(AemFaultType.CONTROL_OPERATION_UNSUCCESSFUL, "Failed to deploy the template " + fileName + " to host " + hostName);
            }
        }
    }

    @Override
    public CommandOutput deployTemplateToHost(String fileName, String hostName, ResourceIdentifier resourceIdentifier) {
        // only one at a time per jvm
        if (!resourceWriteLockMap.containsKey(hostName)) {
            resourceWriteLockMap.put(hostName, new ReentrantReadWriteLock());
        }
        resourceWriteLockMap.get(hostName).writeLock().lock();

        final String badStreamMessage = "Bad Stream: ";
        CommandOutput commandOutput;
        try {
            final String metaDataPath;
            final ResourceContent resourceContent = getResourceContent(resourceIdentifier);
            String metaDataStr = resourceContent.getMetaData();
            ResourceTemplateMetaData resourceTemplateMetaData = new ObjectMapper().readValue(metaDataStr, ResourceTemplateMetaData.class);
            metaDataPath = resourceTemplateMetaData.getDeployPath();
            String resourceSourceCopy;
            // TODO set the selected object value, for now make it null for the external properties
            String resourceDestPath = ResourceFileGenerator.generateResourceConfig(metaDataPath, generateResourceGroup(), null) + "/" + fileName;
            if (resourceTemplateMetaData.getContentType().equals(ContentType.APPLICATION_BINARY.contentTypeStr)) {
                resourceSourceCopy = resourceContent.getContent();
            } else {
                String fileContent = resourceContent.getContent();
                // TODO make the copy source generic (not external-properties directory)
                String jvmResourcesNameDir = ApplicationProperties.get("paths.generated.resource.dir") + "/external-properties";
                // TODO add timestamp to fileName
                resourceSourceCopy = jvmResourcesNameDir + "/" + fileName;
                createConfigFile(jvmResourcesNameDir + "/", fileName, fileContent);
            }

            LOGGER.info("Copying {} to {} on host {}", resourceSourceCopy, resourceDestPath, hostName);
            // TODO backup existing file
            // TODO replace ApplicationControlOperation (and all operation classes) with ResourceControlOperation
            // TODO replace WindowsApplicationPlatformCommandProvider (and all platform command provider) with WindowsResourcePlatformCommandProvider
            commandOutput = remoteCommandExecutor.executeRemoteCommand(null, hostName, ApplicationControlOperation.SECURE_COPY, new WindowsApplicationPlatformCommandProvider(), resourceSourceCopy, resourceDestPath);

        } catch (IOException e) {
            String message = "Failed to write file";
            LOGGER.error(badStreamMessage + message, e);
            throw new InternalErrorException(AemFaultType.BAD_STREAM, message, e);
        } catch (CommandFailureException ce) {
            String message = "Failed to copy file";
            LOGGER.error(badStreamMessage + message, ce);
            throw new InternalErrorException(AemFaultType.BAD_STREAM, message, ce);
        } finally {
            resourceWriteLockMap.get(hostName).writeLock().unlock();
        }
        return commandOutput;
    }

    protected void createConfigFile(String path, String configFileName, String templateContent) throws IOException {
        File configFile = new File(path + configFileName);
        if (configFileName.endsWith(".bat")) {
            templateContent = templateContent.replaceAll("\n", "\r\n");
        }
        FileUtils.writeStringToFile(configFile, templateContent);
    }

    @Override
    public String getExternalPropertiesFile() {
        // TODO make generic getResourceNames API
        ResourceIdentifier identifier = null;
        final List<String> resourceNames = resourceDao.getResourceNames(identifier, EntityType.EXT_PROPERTIES);
        return resourceNames.size() > 0 ? resourceNames.get(0) : "";
    }

    @Override
    public Properties getExternalProperties() {
        return ExternalProperties.getProperties();
    }

    @Override
    @Transactional
    public CreateResourceResponseWrapper createResource(final ResourceIdentifier resourceIdentifier,
                                                        final ResourceTemplateMetaData metaData,
                                                        final InputStream templateData) {
        try {
            return resourceHandler.createResource(resourceIdentifier, metaData, templateData);
        } catch (final ResourceHandlerException rhe) {
            throw new ResourceServiceException(rhe);
        }
    }

    @Override
    public String uploadResource(final ResourceTemplateMetaData resourceTemplateMetaData, final InputStream resourceDataIn) {
        final Application fakedApplication = new Application(new Identifier<Application>(0L), resourceTemplateMetaData.getDeployFileName(),
                resourceTemplateMetaData.getDeployPath(), "", null, true, true, false, resourceTemplateMetaData.getDeployFileName());
        final UploadWebArchiveRequest uploadWebArchiveRequest = new UploadWebArchiveRequest(fakedApplication,
                resourceTemplateMetaData.getDeployFileName(), -1L, resourceDataIn);
        final RepositoryFileInformation fileInfo = privateApplicationService.uploadWebArchiveData(uploadWebArchiveRequest);

        return fileInfo.getPath().toString();
    }
}
