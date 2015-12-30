package com.siemens.cto.aem.persistence.jpa.service.impl;

import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.request.group.CreateGroupRequest;
import com.siemens.cto.aem.common.request.group.UpdateGroupRequest;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupState;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServer;
import com.siemens.cto.aem.persistence.jpa.service.GroupCrudService;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.util.List;

public class GroupCrudServiceImpl extends AbstractCrudServiceImpl<JpaGroup, Group> implements GroupCrudService {

    public GroupCrudServiceImpl() {
    }

    @Override
    public JpaGroup createGroup(final Event<CreateGroupRequest> aGroupToCreate) {
        final CreateGroupRequest createGroupRequest = aGroupToCreate.getRequest();

        final JpaGroup jpaGroup = new JpaGroup();
        jpaGroup.setName(createGroupRequest.getGroupName());

        try {
            return create(jpaGroup);
        } catch (final EntityExistsException eee) {
            throw new BadRequestException(AemFaultType.INVALID_GROUP_NAME,
                    "Group Name already exists: " + aGroupToCreate.getRequest().getGroupName(),
                    eee);
        }
    }

    @Override
    public void updateGroup(final Event<UpdateGroupRequest> aGroupToUpdate) {

        final UpdateGroupRequest aGroupToUpdateRequest = aGroupToUpdate.getRequest();
        final JpaGroup jpaGroup = getGroup(aGroupToUpdateRequest.getId());

        jpaGroup.setName(aGroupToUpdateRequest.getNewName());

        try {
            update(jpaGroup);
        } catch (final EntityExistsException eee) {
            throw new BadRequestException(AemFaultType.INVALID_GROUP_NAME,
                    "Group Name already exists: " + aGroupToUpdate.getRequest().getNewName(),
                    eee);
        }
    }

    @Override
    public JpaGroup getGroup(final Identifier<Group> aGroupId) throws NotFoundException {
        return findById(aGroupId.getId());
    }

    @SuppressWarnings("unchecked")
    @Override
    public JpaGroup getGroup(final String name) throws NotFoundException {
        final Query query = entityManager.createQuery("SELECT g FROM JpaGroup g WHERE g.name = :groupName");
        query.setParameter("groupName", name);
        List<JpaGroup> jpaGroups = query.getResultList();
        if (jpaGroups == null || jpaGroups.size() < 1) {
            throw new NotFoundException(AemFaultType.GROUP_NOT_FOUND, "Group not found: " + name);
        }
        else if (jpaGroups.size() > 1) {
            throw new NotFoundException(AemFaultType.DATA_CONTROL_ERROR, "Too many groups found for " + name + " code is set to only use one");
        }
        return jpaGroups.get(0);
    }

    @Override
    public List<JpaGroup> getGroups() {
        return findAll();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<JpaGroup> findGroups(final String aName) {

        final Query query = entityManager.createQuery("SELECT g FROM JpaGroup g WHERE g.name LIKE :groupName");
        query.setParameter("groupName", "%" + aName + "%");

        return query.getResultList();
    }

    @Override
    public void removeGroup(final Identifier<Group> aGroupId) {
        final JpaGroup group = getGroup(aGroupId);
        remove(group);
    }

    @Override
    public JpaGroup updateGroupStatus(Event<SetStateRequest<Group, GroupState>> aGroupToUpdate) {
        final SetStateRequest<Group, GroupState> setStateRequest = aGroupToUpdate.getRequest();
        final JpaGroup jpaGroup = getGroup(setStateRequest.getNewState().getId());

        jpaGroup.setState(setStateRequest.getNewState().getState());
        jpaGroup.setStateUpdated(DateTime.now().toCalendar(null));

        return update(jpaGroup);
    }

    @Override
    public Long getGroupId(final String name) {
        final Query q = entityManager.createNamedQuery(JpaGroup.QUERY_GET_GROUP_ID);
        q.setParameter("name", name);
        return (Long) q.getSingleResult();
    }

    @Override
    public void linkWebServer(final WebServer webServer) {
        linkWebServer(webServer.getId(), webServer);
    }

    @Override
    public void linkWebServer(final Identifier<WebServer> id, final WebServer webServer) {
        final JpaWebServer jpaWebServer = entityManager.find(JpaWebServer.class, id.getId());
        final List<JpaGroup> jpaGroups = getGroupsWithWebServer(jpaWebServer);

        // Unlink web server from all the groups.
        for (JpaGroup jpaGroup: jpaGroups) {
            jpaGroup.getWebServers().remove(jpaWebServer);
        }

        // Link web server's newly defined groups.
        for (Group group: webServer.getGroups()) {
            final JpaGroup jpaGroup = getGroup(group.getId());
            jpaGroup.getWebServers().add(jpaWebServer);
        }

        entityManager.flush();
    }

    @SuppressWarnings("unchecked")
    private List<JpaGroup> getGroupsWithWebServer(final JpaWebServer jpaWebServer) {
        final Query q = entityManager.createNamedQuery(JpaGroup.QUERY_GET_GROUPS_WITH_WEBSERVER);
        q.setParameter("webServer", jpaWebServer);
        return q.getResultList();
    }

}

