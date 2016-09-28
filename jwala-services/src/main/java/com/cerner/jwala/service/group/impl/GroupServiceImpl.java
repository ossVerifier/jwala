package com.cerner.jwala.service.group.impl;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.app.ApplicationControlOperation;
import com.cerner.jwala.common.domain.model.binarydistribution.BinaryDistributionControlOperation;
import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.group.GroupState;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.resource.ContentType;
import com.cerner.jwala.common.domain.model.resource.ResourceGroup;
import com.cerner.jwala.common.domain.model.resource.ResourceTemplateMetaData;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.exception.ApplicationException;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.group.*;
import com.cerner.jwala.common.request.jvm.UploadJvmTemplateRequest;
import com.cerner.jwala.common.request.webserver.UploadWebServerTemplateRequest;
import com.cerner.jwala.common.rule.group.GroupNameRule;
import com.cerner.jwala.control.application.command.impl.WindowsApplicationPlatformCommandProvider;
import com.cerner.jwala.control.command.RemoteCommandExecutorImpl;
import com.cerner.jwala.control.command.impl.WindowsBinaryDistributionPlatformCommandProvider;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.persistence.service.ApplicationPersistenceService;
import com.cerner.jwala.persistence.service.GroupPersistenceService;
import com.cerner.jwala.persistence.service.WebServerPersistenceService;
import com.cerner.jwala.service.app.impl.DeployApplicationConfException;
import com.cerner.jwala.service.binarydistribution.BinaryDistributionService;
import com.cerner.jwala.service.exception.GroupServiceException;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.template.ResourceFileGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class GroupServiceImpl implements GroupService {

    private final GroupPersistenceService groupPersistenceService;
    private final WebServerPersistenceService webServerPersistenceService;
    private final RemoteCommandExecutorImpl remoteCommandExecutor;
    private final BinaryDistributionService binaryDistributionService;
    private ApplicationPersistenceService applicationPersistenceService;

    private static final String GENERATED_RESOURCE_DIR = "paths.generated.resource.dir";
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupServiceImpl.class);
    private static final String UNZIPEXE = "unzip.exe";


    public GroupServiceImpl(final GroupPersistenceService groupPersistenceService,
                            final WebServerPersistenceService webServerPersistenceService,
                            final ApplicationPersistenceService applicationPersistenceService, RemoteCommandExecutorImpl remoteCommandExecutor, BinaryDistributionService binaryDistributionService) {
        this.groupPersistenceService = groupPersistenceService;
        this.webServerPersistenceService = webServerPersistenceService;
        this.applicationPersistenceService = applicationPersistenceService;
        this.remoteCommandExecutor = remoteCommandExecutor;
        this.binaryDistributionService = binaryDistributionService;
    }

    @Override
    @Transactional
    public Group createGroup(final CreateGroupRequest createGroupRequest,
                             final User aCreatingUser) {

        createGroupRequest.validate();

        return groupPersistenceService.createGroup(createGroupRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public Group getGroup(final Identifier<Group> aGroupId) {
        return groupPersistenceService.getGroup(aGroupId);
    }

    @Override
    @Transactional(readOnly = true)
    public Group getGroupWithWebServers(Identifier<Group> aGroupId) {
        return groupPersistenceService.getGroupWithWebServers(aGroupId);
    }

    @Override
    @Transactional(readOnly = true)
    public Group getGroup(final String name) {
        return groupPersistenceService.getGroup(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> getGroups() {
        return groupPersistenceService.getGroups();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> getGroups(final boolean fetchWebServers) {
        return groupPersistenceService.getGroups(fetchWebServers);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Group> findGroups(final String aGroupNameFragment) {

        new GroupNameRule(aGroupNameFragment).validate();
        return groupPersistenceService.findGroups(aGroupNameFragment);
    }

    @Override
    @Transactional
    public Group updateGroup(final UpdateGroupRequest anUpdateGroupRequest,
                             final User anUpdatingUser) {

        anUpdateGroupRequest.validate();
        Group group = groupPersistenceService.updateGroup(anUpdateGroupRequest);

        return group;
    }

    @Override
    @Transactional
    public void removeGroup(final Identifier<Group> aGroupId) {
        groupPersistenceService.removeGroup(aGroupId);
    }

    @Override
    @Transactional
    public void removeGroup(final String name) {
        try {
            groupPersistenceService.removeGroup(name);
        } catch (PersistenceException e) {
            if (e.getMessage().contains("The transaction has been rolled back.  See the nested exceptions for details on the errors that occurred.")) {
                throw new GroupServiceException("Please check for group dependents Web Apps, Web Servers or JVMs before deleting " + name);
            }
        }
    }

    @Override
    @Transactional
    public Group addJvmToGroup(final AddJvmToGroupRequest addJvmToGroupRequest,
                               final User anAddingUser) {

        addJvmToGroupRequest.validate();
        return groupPersistenceService.addJvmToGroup(addJvmToGroupRequest);
    }

    @Override
    @Transactional
    public Group addJvmsToGroup(final AddJvmsToGroupRequest addJvmsToGroupRequest,
                                final User anAddingUser) {

        addJvmsToGroupRequest.validate();
        for (final AddJvmToGroupRequest command : addJvmsToGroupRequest.toRequests()) {
            addJvmToGroup(command,
                    anAddingUser);
        }

        return getGroup(addJvmsToGroupRequest.getGroupId());
    }

    @Override
    @Transactional
    public Group removeJvmFromGroup(final RemoveJvmFromGroupRequest removeJvmFromGroupRequest,
                                    final User aRemovingUser) {

        removeJvmFromGroupRequest.validate();
        return groupPersistenceService.removeJvmFromGroup(removeJvmFromGroupRequest);
    }

    @Override
    @Transactional
    public List<Jvm> getOtherGroupingDetailsOfJvms(Identifier<Group> id) {
        final List<Jvm> otherGroupConnectionDetails = new LinkedList<>();
        final Group group = groupPersistenceService.getGroup(id, false);
        final Set<Jvm> jvms = group.getJvms();

        for (Jvm jvm : jvms) {
            final Set<Group> tmpGroup = new LinkedHashSet<>();
            if (jvm.getGroups() != null && !jvm.getGroups().isEmpty()) {
                for (Group liteGroup : jvm.getGroups()) {
                    if (!id.getId().equals(liteGroup.getId().getId())) {
                        tmpGroup.add(liteGroup);
                    }
                }
                if (!tmpGroup.isEmpty()) {
                    otherGroupConnectionDetails.add(new Jvm(jvm.getId(), jvm.getJvmName(), tmpGroup));
                }
            }
        }
        return otherGroupConnectionDetails;
    }

    @Override
    @Transactional
    public List<WebServer> getOtherGroupingDetailsOfWebServers(Identifier<Group> id) {
        final List<WebServer> otherGroupConnectionDetails = new ArrayList<>();
        final Group group = groupPersistenceService.getGroup(id, true);
        final Set<WebServer> webServers = group.getWebServers();

        for (WebServer webServer : webServers) {
            final Set<Group> tmpGroup = new LinkedHashSet<>();
            if (webServer.getGroups() != null && !webServer.getGroups().isEmpty()) {
                for (Group webServerGroup : webServer.getGroups()) {
                    if (!id.getId().equals(webServerGroup.getId().getId())) {
                        tmpGroup.add(webServerGroup);
                    }
                }
                if (!tmpGroup.isEmpty()) {
                    otherGroupConnectionDetails.add(new WebServer(webServer.getId(),
                            webServer.getGroups(),
                            webServer.getName()));
                }
            }
        }

        return otherGroupConnectionDetails;
    }

    @Override
    @Transactional
    public Group populateJvmConfig(Identifier<Group> aGroupId, List<UploadJvmTemplateRequest> uploadJvmTemplateRequests, User user, boolean overwriteExisting) {
        return groupPersistenceService.populateJvmConfig(aGroupId, uploadJvmTemplateRequests, user, overwriteExisting);
    }

    @Override
    @Transactional
    public Group populateGroupJvmTemplates(String groupName, List<UploadJvmTemplateRequest> uploadJvmTemplateCommands, User user) {
        return groupPersistenceService.populateGroupJvmTemplates(groupName, uploadJvmTemplateCommands);
    }

    @Override
    @Transactional
    public Group populateGroupWebServerTemplates(String groupName, Map<String, UploadWebServerTemplateRequest> uploadWSTemplateCommands, User user) {
        return groupPersistenceService.populateGroupWebServerTemplates(groupName, uploadWSTemplateCommands);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getGroupJvmsResourceTemplateNames(String groupName) {
        List<String> retVal = new ArrayList<>();
        final List<String> groupJvmsResourceTemplateNames = groupPersistenceService.getGroupJvmsResourceTemplateNames(groupName);
        for (String jvmResourceName : groupJvmsResourceTemplateNames) {
            retVal.add(jvmResourceName);
        }
        return retVal;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getGroupWebServersResourceTemplateNames(String groupName) {
        return groupPersistenceService.getGroupWebServersResourceTemplateNames(groupName);
    }

    @Override
    @Transactional
    public String getGroupJvmResourceTemplate(final String groupName,
                                              final String resourceTemplateName,
                                              final ResourceGroup resourceGroup,
                                              final boolean tokensReplaced) {

        final String template = groupPersistenceService.getGroupJvmResourceTemplate(groupName, resourceTemplateName);
        if (tokensReplaced) {
            // TODO returns the tokenized version of a dummy JVM, but make sure that when deployed each instance is tokenized per JVM
            final Set<Jvm> jvms = groupPersistenceService.getGroup(groupName).getJvms();
            if (jvms != null && !jvms.isEmpty()) {
                return ResourceFileGenerator.generateResourceConfig(template, resourceGroup, jvms.iterator().next());
            }
        }
        return template;
    }

    @Override
    public String getGroupJvmResourceTemplateMetaData(String groupName, String fileName) {
        return groupPersistenceService.getGroupJvmResourceTemplateMetaData(groupName, fileName);
    }

    @Override
    @Transactional
    public String updateGroupJvmResourceTemplate(String groupName, String resourceTemplateName, String content) {
        return groupPersistenceService.updateGroupJvmResourceTemplate(groupName, resourceTemplateName, content);
    }

    @Override
    @Transactional
    public String updateGroupWebServerResourceTemplate(String groupName, String resourceTemplateName, String content) {
        return groupPersistenceService.updateGroupWebServerResourceTemplate(groupName, resourceTemplateName, content);
    }

    @Override
    @Transactional
    public String previewGroupWebServerResourceTemplate(String groupName, String template, ResourceGroup resourceGroup) {
        final Group group = groupPersistenceService.getGroup(groupName);
        Set<WebServer> webservers = groupPersistenceService.getGroupWithWebServers(group.getId()).getWebServers();
        if (webservers != null && !webservers.isEmpty()) {
            final WebServer webServer = webservers.iterator().next();
            return ResourceFileGenerator.generateResourceConfig(template, resourceGroup, webServer);
        }
        return template;
    }

    @Override
    @Transactional
    public String getGroupWebServerResourceTemplate(final String groupName,
                                                    final String resourceTemplateName,
                                                    final boolean tokensReplaced,
                                                    final ResourceGroup resourceGroup) {
        final String template = groupPersistenceService.getGroupWebServerResourceTemplate(groupName, resourceTemplateName);
        if (tokensReplaced) {
            final Group group = groupPersistenceService.getGroup(groupName);
            Set<WebServer> webservers = groupPersistenceService.getGroupWithWebServers(group.getId()).getWebServers();
            if (webservers != null && !webservers.isEmpty()) {
                final WebServer webServer = webservers.iterator().next();
                return ResourceFileGenerator.generateResourceConfig(template, resourceGroup, webServer);
            }
        }
        return template;
    }

    @Override
    public String getGroupWebServerResourceTemplateMetaData(String groupName, String fileName) {
        return groupPersistenceService.getGroupWebServerResourceTemplateMetaData(groupName, fileName);
    }

    @Override
    @Transactional
    public void populateGroupAppTemplates(final Application application, final String appContextMetaData, final String appContext,
                                          final String roleMappingPropsMetaData, final String roleMappingProperties,
                                          final String appPropsMetaData, final String appProperties) {
        final int idx = application.getWebAppContext().lastIndexOf('/');
        final String resourceName = idx == -1 ? application.getWebAppContext() : application.getWebAppContext().substring(idx + 1);

        final String appRoleMappingPropertiesFileName = resourceName + "RoleMapping.properties";
        groupPersistenceService.populateGroupAppTemplate(application.getGroup().getName(), application.getName(),
                appRoleMappingPropertiesFileName, roleMappingPropsMetaData, roleMappingProperties);
        final String appPropertiesFileName = resourceName + ".properties";
        groupPersistenceService.populateGroupAppTemplate(application.getGroup().getName(), application.getName(),
                appPropertiesFileName, appPropsMetaData, appProperties);
        final String appContextFileName = resourceName + ".xml";
        groupPersistenceService.populateGroupAppTemplate(application.getGroup().getName(), application.getName(),
                appContextFileName, appContextMetaData, appContext);
    }

    @Override
    @Transactional
    public String populateGroupAppTemplate(final String groupName, String appName, final String templateName,
                                           final String metaData, final String content) {
        groupPersistenceService.populateGroupAppTemplate(groupName, appName, templateName, metaData, content);
        return groupPersistenceService.getGroupAppResourceTemplate(groupName, appName, templateName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getGroupAppsResourceTemplateNames(String groupName) {
        return groupPersistenceService.getGroupAppsResourceTemplateNames(groupName);
    }

    @Override
    @Transactional
    public String updateGroupAppResourceTemplate(String groupName, String appName, String resourceTemplateName, String content) {
        return groupPersistenceService.updateGroupAppResourceTemplate(groupName, appName, resourceTemplateName, content);
    }

    @Override
    @Transactional
    public String previewGroupAppResourceTemplate(String groupName, String resourceTemplateName, String template, ResourceGroup resourceGroup) {
        final Set<Jvm> jvms = groupPersistenceService.getGroup(groupName).getJvms();
        Jvm jvm = jvms != null && jvms.size() > 0 ? jvms.iterator().next() : null;
        String metaDataStr = groupPersistenceService.getGroupAppResourceTemplateMetaData(groupName, resourceTemplateName);
        try {
            ResourceTemplateMetaData metaData = new ObjectMapper().readValue(metaDataStr, ResourceTemplateMetaData.class);
            Application app = applicationPersistenceService.getApplication(metaData.getEntity().getTarget());
            app.setParentJvm(jvm);
            return ResourceFileGenerator.generateResourceConfig(template, resourceGroup, app);
        } catch (Exception x) {
            LOGGER.error("Failed to generate preview for template {} in  group {}", resourceTemplateName, groupName, x);
            throw new ApplicationException("Template token replacement failed.", x);
        }
    }

    @Override
    public String getGroupAppResourceTemplateMetaData(String groupName, String fileName) {
        return groupPersistenceService.getGroupAppResourceTemplateMetaData(groupName, fileName);
    }

    @Override
    @Transactional
    public String getGroupAppResourceTemplate(String groupName, String appName, String resourceTemplateName, boolean tokensReplaced, ResourceGroup resourceGroup) {
        final String template = groupPersistenceService.getGroupAppResourceTemplate(groupName, appName, resourceTemplateName);
        if (tokensReplaced) {
            String metaDataStr = groupPersistenceService.getGroupAppResourceTemplateMetaData(groupName, resourceTemplateName);
            try {
                ResourceTemplateMetaData metaData = new ObjectMapper().readValue(metaDataStr, ResourceTemplateMetaData.class);
                Application app = applicationPersistenceService.getApplication(metaData.getEntity().getTarget());
                return ResourceFileGenerator.generateResourceConfig(template, resourceGroup, app);
            } catch (Exception x) {
                LOGGER.error("Failed to tokenize template {} in group {}", resourceTemplateName, groupName, x);
                throw new ApplicationException("Template token replacement failed.", x);
            }
        }
        return template;
    }

    @Override
    @Transactional
    public void updateState(Identifier<Group> id, GroupState state) {
        groupPersistenceService.updateState(id, state);
    }

    @Override
    public CommandOutput deployGroupAppTemplate(String groupName, String fileName, ResourceGroup resourceGroup, Application application, Jvm jvm) {
        return executeDeployGroupAppTemplate(groupName, fileName, resourceGroup, application, jvm.getJvmName(), jvm.getHostName());
    }

    @Override
    public CommandOutput deployGroupAppTemplate(String groupName, String fileName, ResourceGroup resourceGroup, Application application, String hostName) {
        return executeDeployGroupAppTemplate(groupName, fileName, resourceGroup, application, null, hostName);
    }

    /**
     * This method executes all the commands for copying the template over to the destination for a group app config file
     *
     * @param groupName     name of the group in which the application can be found
     * @param fileName      name of the file that needs to deployed
     * @param resourceGroup the resource group object that contains all the groups, jvms, webservers and webapps
     * @param application   the application object for the application to deploy the config file too
     * @param jvmName       name of the jvm for which the config file needs to be deployed
     * @param hostName      name of the host which needs the application file
     * @return returns a command output object
     */
    protected CommandOutput executeDeployGroupAppTemplate(final String groupName, final String fileName, final ResourceGroup resourceGroup,
                                                          final Application application, final String jvmName, final String hostName) {
        String metaDataStr = getGroupAppResourceTemplateMetaData(groupName, fileName);
        ResourceTemplateMetaData metaData;
        try {
            final String tokenizedMetaData = ResourceFileGenerator.generateResourceConfig(metaDataStr, resourceGroup, application);
            LOGGER.info("tokenized metadata is : {}", tokenizedMetaData);
            metaData = new ObjectMapper().readValue(tokenizedMetaData, ResourceTemplateMetaData.class);
            final String destPath = metaData.getDeployPath() + '/' + metaData.getDeployFileName();
            File confFile = createConfFile(metaData.getEntity().getTarget(), groupName, metaData.getDeployFileName(), resourceGroup);
            String srcPath, standardError;
            if (metaData.getContentType().equals(ContentType.APPLICATION_BINARY.contentTypeStr)) {
                srcPath = getGroupAppResourceTemplate(groupName, application.getName(), fileName, false, resourceGroup);
            } else {
                srcPath = confFile.getAbsolutePath().replace("\\", "/");
            }
            final String parentDir = new File(destPath).getParentFile().getAbsolutePath().replaceAll("\\\\", "/");
            CommandOutput commandOutput = remoteCommandExecutor.executeRemoteCommand(
                    jvmName,
                    hostName,
                    ApplicationControlOperation.CREATE_DIRECTORY,
                    new WindowsApplicationPlatformCommandProvider(),
                    parentDir
            );
            if (commandOutput.getReturnCode().wasSuccessful()) {
                LOGGER.info("Successfully created the parent dir {} on host {}", parentDir, hostName);
            } else {
                final String stdErr = commandOutput.getStandardError().isEmpty() ? commandOutput.getStandardOutput() : commandOutput.getStandardError();
                LOGGER.error("Error in creating parent dir {} on host {}:: ERROR : {}", parentDir, hostName, stdErr);
                throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, stdErr);
            }
            LOGGER.debug("checking if file: {} exists on remote location", destPath);
            commandOutput = remoteCommandExecutor.executeRemoteCommand(
                    jvmName,
                    hostName,
                    ApplicationControlOperation.CHECK_FILE_EXISTS,
                    new WindowsApplicationPlatformCommandProvider(),
                    destPath);
            if (commandOutput.getReturnCode().wasSuccessful()) {
                if (metaData.isUnpack() && !metaData.isOverwrite()) {
                    standardError = "Destination zip: " + destPath + " exists and isOverwrite = false, so no unzip perform.";
                    LOGGER.error(standardError);
                    throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, standardError);
                }
                LOGGER.debug("backing up file: {}", destPath);
                String currentDateSuffix = new SimpleDateFormat(".yyyyMMdd_HHmmss").format(new Date());
                final String destPathBackup = destPath + currentDateSuffix;
                commandOutput = remoteCommandExecutor.executeRemoteCommand(
                        jvmName,
                        hostName,
                        ApplicationControlOperation.BACK_UP_FILE,
                        new WindowsApplicationPlatformCommandProvider(),
                        destPath,
                        destPathBackup);
                if (!commandOutput.getReturnCode().wasSuccessful()) {
                    standardError = commandOutput.getStandardError().isEmpty() ? commandOutput.getStandardOutput() : commandOutput.getStandardError();
                    LOGGER.error("Error in backing up older file {} :: ERROR: {}", destPath, standardError);
                    throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to back up " + destPath + " for " + jvmName);
                }
            }
            LOGGER.debug("copying file over to location {}", destPath);
            commandOutput = remoteCommandExecutor.executeRemoteCommand(
                    jvmName,
                    hostName,
                    ApplicationControlOperation.SECURE_COPY,
                    new WindowsApplicationPlatformCommandProvider(),
                    srcPath,
                    destPath);
            if (!commandOutput.getReturnCode().wasSuccessful()) {
                standardError = commandOutput.getStandardError().isEmpty() ? commandOutput.getStandardOutput() : commandOutput.getStandardError();
                LOGGER.error("Copy command completed with error trying to copy {} to {} :: ERROR: {}",
                        metaData.getDeployFileName(), application.getName(), standardError);
                throw new DeployApplicationConfException(standardError);
            }
            if (metaData.isUnpack()) {
                binaryDistributionService.prepareUnzip(hostName);
                commandOutput = remoteCommandExecutor.executeRemoteCommand(null,
                        hostName,
                        BinaryDistributionControlOperation.UNZIP_BINARY,
                        new WindowsBinaryDistributionPlatformCommandProvider(),
                        ApplicationProperties.get("remote.commands.user-scripts") + "/" + UNZIPEXE,
                        destPath,
                        parentDir,
                        "");
                LOGGER.info("commandOutput.getReturnCode().toString(): " + commandOutput.getReturnCode().toString());
                if (!commandOutput.getReturnCode().wasSuccessful()) {
                    standardError = "Cannot unzip " + destPath + " to " + parentDir + ", please check the log for more information.";
                    LOGGER.error(standardError);
                    throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, standardError);
                }
            }
            return commandOutput;
        } catch (IOException e) {
            LOGGER.error("Failed to deploy file {} in group {}", fileName, groupName, e);
            throw new ApplicationException("Failed to deploy " + fileName + " in group " + groupName, e);
        } catch (CommandFailureException e) {
            LOGGER.error("Failed to execute remote command when deploying {} for group {}", fileName, groupName, e);
            throw new ApplicationException("Failed to execute remote command when deploying " + fileName + " for group " + groupName, e);
        }
    }

    protected File createConfFile(final String appName, final String groupName,
                                  final String resourceTemplateName, final ResourceGroup resourceGroup)
            throws FileNotFoundException {
        PrintWriter out = null;
        final StringBuilder fileNameBuilder = new StringBuilder();

        createPathIfItDoesNotExists(ApplicationProperties.get(GENERATED_RESOURCE_DIR));
        createPathIfItDoesNotExists(ApplicationProperties.get(GENERATED_RESOURCE_DIR) + "/"
                + groupName.replace(" ", "-"));

        fileNameBuilder.append(ApplicationProperties.get(GENERATED_RESOURCE_DIR))
                .append('/')
                .append(groupName.replace(" ", "-"))
                .append('/')
                .append(appName.replace(" ", "-"))
                .append('.')
                .append(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()))
                .append('_')
                .append(resourceTemplateName);

        final File appConfFile = new File(fileNameBuilder.toString());
        try {
            out = new PrintWriter(appConfFile.getAbsolutePath());
            out.println(getGroupAppResourceTemplate(groupName, appName, resourceTemplateName, true, resourceGroup));
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return appConfFile;
    }

    protected static void createPathIfItDoesNotExists(String path) {
        if (!Files.exists(Paths.get(path))) {
            new File(path).mkdir();
        }
    }

    @Override
    public List<String> getHosts(final String groupName) {
        return groupPersistenceService.getHosts(groupName);
    }

    @Override
    public List<String> getAllHosts() {
        Set<String> allHosts = new TreeSet<>();
        for (Group group : groupPersistenceService.getGroups()) {
            allHosts.addAll(groupPersistenceService.getHosts(group.getName()));
        }
        return new ArrayList<>(allHosts);
    }
}
