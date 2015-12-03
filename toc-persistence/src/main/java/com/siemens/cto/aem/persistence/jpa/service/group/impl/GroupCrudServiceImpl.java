package com.siemens.cto.aem.persistence.jpa.service.group.impl;

import com.siemens.cto.aem.request.group.CreateGroupRequest;
import com.siemens.cto.aem.request.group.UpdateGroupRequest;
import com.siemens.cto.aem.request.state.SetStateRequest;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupState;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.service.group.GroupCrudService;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Calendar;
import java.util.List;

public class GroupCrudServiceImpl implements GroupCrudService {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;

    public GroupCrudServiceImpl() {
    }

    @Override
    public JpaGroup createGroup(final Event<CreateGroupRequest> aGroupToCreate) {

        try {
            final CreateGroupRequest createGroupCommand = aGroupToCreate.getRequest();
            final AuditEvent auditEvent = aGroupToCreate.getAuditEvent();
            final String userId = auditEvent.getUser().getUserId();
            final Calendar updateDate = auditEvent.getDateTime().getCalendar();

            final JpaGroup jpaGroup = new JpaGroup();
            jpaGroup.setName(createGroupCommand.getGroupName());
            jpaGroup.setCreateBy(userId);
            jpaGroup.setCreateDate(updateDate);
            jpaGroup.setUpdateBy(userId);
            jpaGroup.setLastUpdateDate(updateDate);
            jpaGroup.setState(null);
            jpaGroup.setStateUpdated(null);

            entityManager.persist(jpaGroup);
            entityManager.flush();

            return jpaGroup;
        } catch (final EntityExistsException eee) {
            throw new BadRequestException(AemFaultType.INVALID_GROUP_NAME,
                                          "Group Name already exists: " + aGroupToCreate.getRequest().getGroupName(),
                                          eee);
        }
    }

    @Override
    public void updateGroup(final Event<UpdateGroupRequest> aGroupToUpdate) {

        try {
            final UpdateGroupRequest updateGroupCommand = aGroupToUpdate.getRequest();
            final AuditEvent auditEvent = aGroupToUpdate.getAuditEvent();
            final Identifier<Group> groupId = updateGroupCommand.getId();
            final JpaGroup jpaGroup = getGroup(groupId);

            jpaGroup.setName(updateGroupCommand.getNewName());
            jpaGroup.setUpdateBy(auditEvent.getUser().getUserId());
            jpaGroup.setLastUpdateDate(auditEvent.getDateTime().getCalendar());

            entityManager.flush();
        } catch (final EntityExistsException eee) {
            throw new BadRequestException(AemFaultType.INVALID_GROUP_NAME,
                                          "Group Name already exists: " + aGroupToUpdate.getRequest().getNewName(),
                                          eee);
        }
    }

    @Override
    public JpaGroup getGroup(final Identifier<Group> aGroupId) throws NotFoundException {

        final JpaGroup jpaGroup = entityManager.find(JpaGroup.class,
                                                     aGroupId.getId());

        if (jpaGroup == null) {
            throw new NotFoundException(AemFaultType.GROUP_NOT_FOUND,
                                        "Group not found: " + aGroupId);
        }

        return jpaGroup;
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

        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<JpaGroup> criteria = builder.createQuery(JpaGroup.class);
        final Root<JpaGroup> root = criteria.from(JpaGroup.class);

        criteria.select(root);

        final TypedQuery<JpaGroup> query = entityManager.createQuery(criteria);

        return query.getResultList();
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
        entityManager.remove(group);
    }

    @Override
    public JpaGroup updateGroupStatus(Event<SetStateRequest<Group, GroupState>> aGroupToUpdate) {

        final SetStateRequest<Group, GroupState> updateGroupCommand = aGroupToUpdate.getRequest();
        final AuditEvent auditEvent = aGroupToUpdate.getAuditEvent();
        final CurrentState<Group, GroupState> newState = updateGroupCommand.getNewState();
        final Identifier<Group> groupId = newState.getId();
        final JpaGroup jpaGroup = getGroup(groupId);
        
        if (jpaGroup == null) {
            throw new NotFoundException(AemFaultType.GROUP_NOT_FOUND,
                                        "Group not found: " + groupId);
        }

        jpaGroup.setState(newState.getState());
        jpaGroup.setStateUpdated(DateTime.now().toCalendar(null));
        jpaGroup.setUpdateBy(auditEvent.getUser().getUserId());
        jpaGroup.setLastUpdateDate(auditEvent.getDateTime().getCalendar());

        entityManager.flush();
        
        return jpaGroup;
    }

    @Override
    public Long getGroupId(final String name) {
        final Query q = entityManager.createNamedQuery(JpaGroup.QUERY_GET_GROUP_ID);
        q.setParameter("name", name);
        return (Long) q.getSingleResult();
    }

}

