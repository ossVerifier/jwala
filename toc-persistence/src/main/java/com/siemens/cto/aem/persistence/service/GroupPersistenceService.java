package com.siemens.cto.aem.persistence.service;

import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.request.group.*;
import com.siemens.cto.aem.common.domain.model.group.*;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;

import java.util.List;
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

    Group populateGroupJvmTemplates(String groupName, List<UploadJvmTemplateRequest> uploadJvmTemplateCommands, User user);

    Group populateGroupWebServerTemplates(String groupName, List<UploadWebServerTemplateRequest> uploadWSTemplateCommands, User user);

    List<String> getGroupJvmsResourceTemplateNames(String groupName);

    List<String> getGroupWebServersResourceTemplateNames(String groupName);

    String getGroupJvmResourceTemplate(String groupName, String resourceTemplateName);

    String getGroupWebServerResourceTemplate(String groupName, String resourceTemplateName);

    String updateGroupJvmResourceTemplate(String groupName, String resourceTemplateName, String content);

    String updateGroupWebServerResourceTemplate(String groupName, String resourceTemplateName, String content);

    Group populateGroupAppTemplate(Group group, String templateFileName, String templateContent);

    List<String> getGroupAppsResourceTemplateNames(String groupName);

    String getGroupAppResourceTemplate(String groupName, String resourceTemplateName);

    String updateGroupAppResourceTemplate(String groupName, String resourceTemplateName, String content);

    void updateState(Identifier<Group> id, GroupState state);

}
