package com.siemens.cto.aem.service.group.impl;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.app.ApplicationControlOperation;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupState;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.resource.ResourceGroup;
import com.siemens.cto.aem.common.domain.model.resource.ResourceTemplateMetaData;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.exception.ApplicationException;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.common.request.group.*;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.common.rule.group.GroupNameRule;
import com.siemens.cto.aem.control.application.command.impl.WindowsApplicationPlatformCommandProvider;
import com.siemens.cto.aem.control.command.RemoteCommandExecutorImpl;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.service.ApplicationPersistenceService;
import com.siemens.cto.aem.persistence.service.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.WebServerPersistenceService;
import com.siemens.cto.aem.service.app.impl.DeployApplicationConfException;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.template.ResourceFileGenerator;
import com.siemens.cto.aem.template.jvm.TomcatJvmConfigFileGenerator;
import com.siemens.cto.aem.template.webserver.ApacheWebServerConfigFileGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

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
    private ApplicationPersistenceService applicationPersistenceService;

    private static final String GENERATED_RESOURCE_DIR = "stp.generated.resource.dir";
    private static final Logger LOGGER = LoggerFactory.getLogger(GroupServiceImpl.class);

    public GroupServiceImpl(final GroupPersistenceService groupPersistenceService,
                            final WebServerPersistenceService webServerPersistenceService,
                            final ApplicationPersistenceService applicationPersistenceService, RemoteCommandExecutorImpl remoteCommandExecutor) {
        this.groupPersistenceService = groupPersistenceService;
        this.webServerPersistenceService = webServerPersistenceService;
        this.applicationPersistenceService = applicationPersistenceService;
        this.remoteCommandExecutor = remoteCommandExecutor;
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
        groupPersistenceService.removeGroup(name);
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
    public Group populateWebServerConfig(Identifier<Group> aGroupId, List<UploadWebServerTemplateRequest> uploadWSTemplateCommands, User user, boolean overwriteExisting) {
        webServerPersistenceService.populateWebServerConfig(uploadWSTemplateCommands, user, overwriteExisting);
        return groupPersistenceService.getGroup(aGroupId);
    }

    @Override
    @Transactional
    public Group populateGroupJvmTemplates(String groupName, List<UploadJvmTemplateRequest> uploadJvmTemplateCommands, User user) {
        return groupPersistenceService.populateGroupJvmTemplates(groupName, uploadJvmTemplateCommands);
    }

    @Override
    @Transactional
    public Group populateGroupWebServerTemplates(String groupName, List<UploadWebServerTemplateRequest> uploadWSTemplateCommands, User user) {
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
                                              final boolean tokensReplaced) {

        final String template = groupPersistenceService.getGroupJvmResourceTemplate(groupName, resourceTemplateName);
        if (tokensReplaced) {
            // TODO returns the tokenized version of a dummy JVM, but make sure that when deployed each instance is tokenized per JVM
            final Set<Jvm> jvms = groupPersistenceService.getGroup(groupName).getJvms();
            if (jvms != null && !jvms.isEmpty()) {
                return TomcatJvmConfigFileGenerator.getJvmConfigFromText(template, jvms.iterator().next(), new ArrayList<Jvm>(jvms));
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
    public String previewGroupWebServerResourceTemplate(String groupName, String template) {
        final Group group = groupPersistenceService.getGroup(groupName);
        Set<WebServer> webservers = groupPersistenceService.getGroupWithWebServers(group.getId()).getWebServers();
        if (webservers != null && !webservers.isEmpty()) {
            final WebServer webServer = webservers.iterator().next();
            return ApacheWebServerConfigFileGenerator.getHttpdConfFromText(webServer.getName(), template, webServer, new ArrayList(group.getJvms()), applicationPersistenceService.findApplicationsBelongingTo(group.getId()));
        }
        return template;
    }

    @Override
    @Transactional
    public String previewGroupJvmResourceTemplate(String groupName, String template) {
        final Set<Jvm> jvms = groupPersistenceService.getGroup(groupName).getJvms();
        if (jvms != null && jvms.size() > 0) {
            return TomcatJvmConfigFileGenerator.getJvmConfigFromText(template, jvms.iterator().next(), new ArrayList<Jvm>(jvms));
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
        final Group group = application.getGroup();
        final int idx = application.getWebAppContext().lastIndexOf('/');
        final String resourceName = idx == -1 ? application.getWebAppContext() : application.getWebAppContext().substring(idx + 1);

        final String appRoleMappingPropertiesFileName = resourceName + "RoleMapping.properties";
        groupPersistenceService.populateGroupAppTemplate(group, appRoleMappingPropertiesFileName, roleMappingPropsMetaData,
                roleMappingProperties);
        final String appPropertiesFileName = resourceName + ".properties";
        groupPersistenceService.populateGroupAppTemplate(group, appPropertiesFileName, appPropsMetaData, appProperties);
        final String appContextFileName = resourceName + ".xml";
        groupPersistenceService.populateGroupAppTemplate(group, appContextFileName, appContextMetaData, appContext);
    }

    @Override
    @Transactional
    public String populateGroupAppTemplate(final String groupName, final String templateName, final String metaData,
                                           final String content) {
        Group group = groupPersistenceService.getGroup(groupName);
        groupPersistenceService.populateGroupAppTemplate(group, templateName, metaData, content);
        return groupPersistenceService.getGroupAppResourceTemplate(groupName, templateName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getGroupAppsResourceTemplateNames(String groupName) {
        return groupPersistenceService.getGroupAppsResourceTemplateNames(groupName);
    }

    @Override
    @Transactional
    public String updateGroupAppResourceTemplate(String groupName, String resourceTemplateName, String content) {
        return groupPersistenceService.updateGroupAppResourceTemplate(groupName, resourceTemplateName, content);
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
    public String getGroupAppResourceTemplate(String groupName, String resourceTemplateName, boolean tokensReplaced, ResourceGroup resourceGroup) {
        final String template = groupPersistenceService.getGroupAppResourceTemplate(groupName, resourceTemplateName);
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
        String metaDataStr = getGroupAppResourceTemplateMetaData(groupName, fileName);
        ResourceTemplateMetaData metaData;
        try {
            metaData = new ObjectMapper().readValue(metaDataStr, ResourceTemplateMetaData.class);
            File confFile = createConfFile(metaData.getEntity().getTarget(), groupName, fileName, resourceGroup);

            final String destPath = ResourceFileGenerator.generateResourceConfig(metaData.getDeployPath(), resourceGroup, application) + '/' + fileName;
            final String srcPath = confFile.getAbsolutePath().replace("\\", "/");
            final String jvmName = jvm.getJvmName();
            final String hostName = jvm.getHostName();
            CommandOutput commandOutput = remoteCommandExecutor.executeRemoteCommand(
                    jvmName,
                    hostName,
                    ApplicationControlOperation.CHECK_FILE_EXISTS,
                    new WindowsApplicationPlatformCommandProvider(),
                    destPath);
            if (commandOutput.getReturnCode().wasSuccessful()) {
                String currentDateSuffix = new SimpleDateFormat(".yyyyMMdd_HHmmss").format(new Date());
                final String destPathBackup = destPath + currentDateSuffix;
                commandOutput = remoteCommandExecutor.executeRemoteCommand(
                        jvmName,
                        hostName,
                        ApplicationControlOperation.BACK_UP_CONFIG_FILE,
                        new WindowsApplicationPlatformCommandProvider(),
                        destPath,
                        destPathBackup);
                if (!commandOutput.getReturnCode().wasSuccessful()) {
                    throw new InternalErrorException(AemFaultType.REMOTE_COMMAND_FAILURE, "Failed to back up " + destPath + " for " + jvm);
                }

            }
            final CommandOutput execData = remoteCommandExecutor.executeRemoteCommand(
                    jvmName,
                    hostName,
                    ApplicationControlOperation.SECURE_COPY,
                    new WindowsApplicationPlatformCommandProvider(),
                    srcPath,
                    destPath);
            if (execData.getReturnCode().wasSuccessful()) {
                LOGGER.info("Copy of {} successful: {}", fileName, confFile.getAbsolutePath());
                return execData;
            } else {
                String standardError = execData.getStandardError().isEmpty() ? execData.getStandardOutput() : execData.getStandardError();
                LOGGER.error("Copy command completed with error trying to copy {} to {} :: ERROR: {}",
                        fileName, application.getName(), standardError);
                throw new DeployApplicationConfException(standardError);
            }
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
            out.println(getGroupAppResourceTemplate(groupName, resourceTemplateName, true, resourceGroup));
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

}
