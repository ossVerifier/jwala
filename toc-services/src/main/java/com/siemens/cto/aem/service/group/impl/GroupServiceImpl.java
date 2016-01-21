package com.siemens.cto.aem.service.group.impl;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.request.group.*;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.common.rule.group.GroupNameRule;
import com.siemens.cto.aem.persistence.service.ApplicationPersistenceService;
import com.siemens.cto.aem.persistence.service.GroupPersistenceService;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.template.jvm.TomcatJvmConfigFileGenerator;
import com.siemens.cto.aem.template.webserver.ApacheWebServerConfigFileGenerator;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

public class GroupServiceImpl implements GroupService {

    private final GroupPersistenceService groupPersistenceService;
    private final WebServerService webServerService;
    private ApplicationPersistenceService applicationPersistenceService;

    public GroupServiceImpl(final GroupPersistenceService theGroupPersistenceService,
                            final WebServerService wSService,
                            final ApplicationPersistenceService applicationPersistenceService) {
        groupPersistenceService = theGroupPersistenceService;
        webServerService = wSService;
        this.applicationPersistenceService = applicationPersistenceService;
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
        webServerService.populateWebServerConfig(uploadWSTemplateCommands, user, overwriteExisting);
        return groupPersistenceService.getGroup(aGroupId);
    }

    @Override
    @Transactional
    public Group populateGroupJvmTemplates(String groupName, List<UploadJvmTemplateRequest> uploadJvmTemplateCommands, User user) {
        return groupPersistenceService.populateGroupJvmTemplates(groupName, uploadJvmTemplateCommands, user);
    }

    @Override
    @Transactional
    public Group populateGroupWebServerTemplates(String groupName, List<UploadWebServerTemplateRequest> uploadWSTemplateCommands, User user) {
        return groupPersistenceService.populateGroupWebServerTemplates(groupName, uploadWSTemplateCommands, user);
    }

    @Override
    public List<String> getGroupJvmsResourceTemplateNames(String groupName) {
        return groupPersistenceService.getGroupJvmsResourceTemplateNames(groupName);
    }

    @Override
    public List<String> getGroupWebServersResourceTemplateNames(String groupName) {
        return groupPersistenceService.getGroupWebServersResourceTemplateNames(groupName);
    }

    @Override
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
    public String previewGroupJvmResourceTemplate(String groupName, String template) {
        final Set<Jvm> jvms = groupPersistenceService.getGroup(groupName).getJvms();
        if (jvms != null && jvms.size() > 0) {
            return TomcatJvmConfigFileGenerator.getJvmConfigFromText(template, jvms.iterator().next(), new ArrayList<Jvm>(jvms));
        }
        return template;
    }

    @Override
    public String getGroupWebServerResourceTemplate(final String groupName,
                                                    final String resourceTemplateName,
                                                    final boolean tokensReplaced) {
        final String template = groupPersistenceService.getGroupWebServerResourceTemplate(groupName, resourceTemplateName);
        if (tokensReplaced) {
            // TODO returns the tokenized version of a dummy JVM, but make sure that when deployed each instance is tokenized per JVM
            final Group group = groupPersistenceService.getGroup(groupName);
            Set<WebServer> webservers = groupPersistenceService.getGroupWithWebServers(group.getId()).getWebServers();
            if (webservers != null && !webservers.isEmpty()) {
                final WebServer webServer = webservers.iterator().next();
                return ApacheWebServerConfigFileGenerator.getHttpdConfFromText(webServer.getName(), template, webServer, new ArrayList(group.getJvms()), applicationPersistenceService.findApplicationsBelongingTo(group.getId()));
            }
        }
        return template;
    }

    @Override
    public void populateGroupAppTemplates(Application application, String appContext, String roleMappingProperties, String appProperties) {
        final Group group = application.getGroup();
        final int idx = application.getWebAppContext().lastIndexOf('/');
        final String resourceName = idx == -1 ? application.getWebAppContext() : application.getWebAppContext().substring(idx + 1);

        final String appRoleMappingPropertiesFileName = resourceName + "RoleMapping.properties";
        groupPersistenceService.populateGroupAppTemplate(group, appRoleMappingPropertiesFileName, roleMappingProperties);
        final String appPropertiesFileName = resourceName + ".properties";
        groupPersistenceService.populateGroupAppTemplate(group, appPropertiesFileName, appProperties);
        final String appContextFileName = resourceName + ".xml";
        groupPersistenceService.populateGroupAppTemplate(group, appContextFileName, appContext);
    }
}
