package com.siemens.cto.aem.service.group;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.request.group.*;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.common.domain.model.group.*;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;

import java.util.List;

public interface GroupService {

    Group createGroup(final CreateGroupRequest aCreateGroupCommand, final User aCreatingUser);

    Group getGroup(final Identifier<Group> aGroupId);

    Group getGroupWithWebServers(final Identifier<Group> aGroupId);

    Group getGroup(final String name);

    List<Group> getGroups();

    List<Group> getGroups(final boolean fetchWebServers);

    List<Group> findGroups(final String aGroupNameFragment);

    Group updateGroup(final UpdateGroupRequest anUpdateGroupCommand, final User anUpdatingUser);

    void removeGroup(final Identifier<Group> aGroupId);

    void removeGroup(String name);

    Group addJvmToGroup(final AddJvmToGroupRequest addJvmToGroupRequest,final User anAddingUser);

    Group addJvmsToGroup(final AddJvmsToGroupRequest addJvmsToGroupRequest, final User anAddingUser);

    Group removeJvmFromGroup(final RemoveJvmFromGroupRequest removeJvmFromGroupRequest,final User aRemovingUser);

    /**
     * Gets the connection details of JVMs under a group specified by id.
     * @param id the group id
     * @return JVMs that are members of more than one group.
     */
    List<Jvm> getOtherGroupingDetailsOfJvms(final Identifier<Group> id);

    /**
     * Gets the connection details of Web Servers under a group specified by id.
     * @param id the group id
     * @return Web Servers that are members of more than one group.
     */
    List<WebServer> getOtherGroupingDetailsOfWebServers(final Identifier<Group> id);

    Group populateJvmConfig(Identifier<Group> aGroupId, List<UploadJvmTemplateRequest> uploadJvmTemplateCommands, User user, boolean overwriteExisting);

    Group populateWebServerConfig(Identifier<Group> aGroupId, List<UploadWebServerTemplateRequest> uploadWSTemplateCommands, User user, boolean overwriteExisting);

    Group populateGroupJvmTemplates(String groupName, List<UploadJvmTemplateRequest> uploadJvmTemplateCommands, User user);

    Group populateGroupWebServerTemplates(String groupName, List<UploadWebServerTemplateRequest> uploadWebServerTemplateRequests, User user);

    List<String> getGroupJvmsResourceTemplateNames(String groupName);

    List<String> getGroupWebServersResourceTemplateNames(String groupName);

    String getGroupJvmResourceTemplate(String groupName, String resourceTemplateName, boolean tokensReplaced);

    String getGroupWebServerResourceTemplate(String groupName, String resourceTemplateName, boolean tokensReplaced);

    String previewGroupJvmResourceTemplate(String groupName, String template);

    String previewGroupWebServerResourceTemplate(String groupName, String template);

    String updateGroupJvmResourceTemplate(String groupName, String resourceTemplateName, String content);

    String updateGroupWebServerResourceTemplate(String groupName, String resourceTemplateName, String content);

    void populateGroupAppTemplates(Application application, String appContext, String roleMappingProperties, String appProperties);

    List<String> getGroupAppsResourceTemplateNames(String groupName);

    String getGroupAppResourceTemplate(String groupName, String appName, String resourceTemplateName, boolean tokensReplaced);

    String updateGroupAppResourceTemplate(String groupName, String resourceTemplateName, String content);

    String previewGroupAppResourceTemplate(String groupName, String appName, String template);

    String populateGroupAppTemplate(String groupName, String templateName, String content);
}