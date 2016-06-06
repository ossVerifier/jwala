package com.siemens.cto.aem.persistence.service;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupState;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.request.group.*;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.ConfigTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GroupPersistenceService extends StatePersistenceService<Group, GroupState> {

    Group updateGroup(UpdateGroupRequest updateGroupRequest) throws NotFoundException;

    Group createGroup(CreateGroupRequest createGroupRequest);

    Group getGroup(final Identifier<Group> aGroupId) throws NotFoundException;

    Group getGroupWithWebServers(final Identifier<Group> aGroupId) throws NotFoundException;

    Group getGroupWithWebServers(String groupName) throws NotFoundException;

    Group getGroup(final String name) throws NotFoundException;

    List<Group> getGroups();

    List<Group> getGroups(final boolean fetchWebServers);

    List<Group> findGroups(final String aName);

    void removeGroup(final Identifier<Group> aGroupId) throws NotFoundException;

    void removeGroup(String name) throws NotFoundException;

    Group addJvmToGroup(AddJvmToGroupRequest addJvmToGroupRequest) throws NotFoundException;

    Group removeJvmFromGroup(RemoveJvmFromGroupRequest removeJvmFromGroupRequest) throws NotFoundException;

    Group updateGroupStatus(SetGroupStateRequest setGroupStateRequest);

    Group getGroup(final Identifier<Group> aGroupId, final boolean fetchWebServers) throws NotFoundException;

    Set<CurrentState<Group, GroupState>> getAllKnownStates();

    Group populateJvmConfig(Identifier<Group> aGroupId, List<UploadJvmTemplateRequest> uploadJvmTemplateCommands, User user, boolean overwriteExisting);

    Group populateGroupJvmTemplates(String groupName, List<UploadJvmTemplateRequest> uploadJvmTemplateCommands);

    Group populateGroupWebServerTemplates(String groupName, Map<String, UploadWebServerTemplateRequest> uploadWSTemplateCommands);

    List<String> getGroupJvmsResourceTemplateNames(String groupName);

    List<String> getGroupWebServersResourceTemplateNames(String groupName);

    String getGroupJvmResourceTemplate(String groupName, String resourceTemplateName);

    String getGroupWebServerResourceTemplate(String groupName, String resourceTemplateName);

    String updateGroupJvmResourceTemplate(String groupName, String resourceTemplateName, String content);

    String updateGroupWebServerResourceTemplate(String groupName, String resourceTemplateName, String content);

    ConfigTemplate populateGroupAppTemplate(String groupName, String appName, String templateFileName, String metaData, String templateContent);

    List<String> getGroupAppsResourceTemplateNames(String groupName);

    String getGroupAppResourceTemplate(String groupName, String appName, String resourceTemplateName);

    String getGroupAppResourceTemplateMetaData(String groupName, String fileName);

    String updateGroupAppResourceTemplate(String groupName, String appName, String resourceTemplateName, String content);

    void updateState(Identifier<Group> id, GroupState state);

    int removeAppTemplate(String name);

    int removeJvmTemplate(String name);

    int removeWeServerTemplate(String name);

    int removeJvmTemplate(String groupName, String templateName);

    int removeWeServerTemplate(String groupName, String templateName);

    String getGroupJvmResourceTemplateMetaData(String groupName, String fileName);

    String getGroupWebServerResourceTemplateMetaData(String groupName, String resourceTemplateName);

    /**
     *
     * @param groupName
     * @param fileName
     * @return
     */
    boolean checkGroupJvmResourceFileName(String groupName, String fileName);

    /**
     *
     * @param groupName
     * @param fileName
     * @return
     */
    boolean checkGroupAppResourceFileName(String groupName, String fileName);

    /**
     *
     * @param groupName
     * @param fileName
     * @return
     */
    boolean checkGroupWebServerResourceFileName(String groupName, String fileName);
}
