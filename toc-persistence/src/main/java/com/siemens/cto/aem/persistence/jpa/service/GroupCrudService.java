package com.siemens.cto.aem.persistence.jpa.service;

import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.request.group.CreateGroupRequest;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupState;
import com.siemens.cto.aem.common.request.group.UpdateGroupRequest;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;

import java.util.List;

public interface GroupCrudService extends CrudService<JpaGroup, Group> {

    JpaGroup createGroup(final Event<CreateGroupRequest> aGroupToCreate);

    void updateGroup(final Event<UpdateGroupRequest> aGroupToUpdate);

    JpaGroup getGroup(final Identifier<Group> aGroupId) throws NotFoundException;

    JpaGroup getGroup(final String name) throws NotFoundException;

    List<JpaGroup> getGroups();

    List<JpaGroup> findGroups(final String aName);

    void removeGroup(final Identifier<Group> aGroupId);
    
    JpaGroup updateGroupStatus(Event<SetStateRequest<Group, GroupState>> aGroupToUpdate);

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

}
