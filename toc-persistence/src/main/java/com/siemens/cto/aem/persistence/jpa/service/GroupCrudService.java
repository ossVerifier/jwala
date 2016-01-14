package com.siemens.cto.aem.persistence.jpa.service;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupState;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.request.group.CreateGroupRequest;
import com.siemens.cto.aem.common.request.group.UpdateGroupRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;

import java.util.List;

public interface GroupCrudService extends CrudService<JpaGroup> {

    JpaGroup createGroup(CreateGroupRequest createGroupRequest);

    void updateGroup(UpdateGroupRequest updateGroupRequest);

    JpaGroup getGroup(final Identifier<Group> aGroupId) throws NotFoundException;

    JpaGroup getGroup(final String name) throws NotFoundException;

    List<JpaGroup> getGroups();

    List<JpaGroup> findGroups(final String aName);

    void removeGroup(final Identifier<Group> aGroupId);
    
    JpaGroup updateGroupStatus(SetStateRequest<Group, GroupState> setStateRequest);

    Long getGroupId(String name);

    /**
     * Link a web server to to a collection of groups.
     * @param webServer the web server to link.
     */
    void linkWebServer(WebServer webServer);

    /**
     * Link a newly created web server to a collection of groups.
     * @param id id of the newly created web server to link.
     * @param webServer wrapper for the web server details.
     */
    void linkWebServer(Identifier<WebServer> id, WebServer webServer);

    void uploadGroupJvmTemplate(UploadJvmTemplateRequest uploadRequest, JpaGroup group);

    void uploadGroupWebServerTemplate(UploadWebServerTemplateRequest uploadRequest, JpaGroup group);

    List getGroupJvmsResourceTemplateNames(String groupName);

    List getGroupWebServersResourceTemplateNames(String groupName);

    String getGroupJvmResourceTemplate(String groupName, String resourceTemplateName);

    String getGroupWebServerResourceTemplate(String groupName, String resourceTemplateName);

    void updateGroupJvmResourceTemplate(String groupName, String resourceTemplateName, String content);

    void updateGroupWebServerResourceTemplate(String groupName, String resourceTemplateName, String content);
}
