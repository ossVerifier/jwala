package com.siemens.cto.aem.persistence.jpa.service.group.impl;

import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.CreateGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.UpdateGroupCommand;
import com.siemens.cto.aem.domain.model.group.command.SetGroupStateCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.service.JpaQueryPaginator;
import com.siemens.cto.aem.persistence.jpa.service.group.GroupCrudService;

public class GroupCrudServiceImpl implements GroupCrudService {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;

    private final JpaQueryPaginator paginator;

    public GroupCrudServiceImpl() {
        paginator = new JpaQueryPaginator();
    }

    @Override
    public JpaGroup createGroup(final Event<CreateGroupCommand> aGroupToCreate) {

        try {
            final CreateGroupCommand createGroupCommand = aGroupToCreate.getCommand();
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
                                          "Group Name already exists: " + aGroupToCreate.getCommand().getGroupName(),
                                          eee);
        }
    }

    @Override
    public void updateGroup(final Event<UpdateGroupCommand> aGroupToUpdate) {

        try {
            final UpdateGroupCommand updateGroupCommand = aGroupToUpdate.getCommand();
            final AuditEvent auditEvent = aGroupToUpdate.getAuditEvent();
            final Identifier<Group> groupId = updateGroupCommand.getId();
            final JpaGroup jpaGroup = getGroup(groupId);

            jpaGroup.setName(updateGroupCommand.getNewName());
            jpaGroup.setUpdateBy(auditEvent.getUser().getUserId());
            jpaGroup.setLastUpdateDate(auditEvent.getDateTime().getCalendar());

            entityManager.flush();
        } catch (final EntityExistsException eee) {
            throw new BadRequestException(AemFaultType.INVALID_GROUP_NAME,
                                          "Group Name already exists: " + aGroupToUpdate.getCommand().getNewName(),
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

    @Override
    public List<JpaGroup> getGroups(final PaginationParameter somePagination) {

        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<JpaGroup> criteria = builder.createQuery(JpaGroup.class);
        final Root<JpaGroup> root = criteria.from(JpaGroup.class);

        criteria.select(root);

        final TypedQuery<JpaGroup> query = entityManager.createQuery(criteria);

        paginator.paginate(query,
                           somePagination);

        return query.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<JpaGroup> findGroups(final String aName,
                                     final PaginationParameter somePagination) {

        final Query query = entityManager.createQuery("SELECT g FROM JpaGroup g WHERE g.name LIKE :groupName");
        query.setParameter("groupName", "%" + aName + "%");

        paginator.paginate(query,
                           somePagination);

        return query.getResultList();
    }

    @Override
    public void removeGroup(final Identifier<Group> aGroupId) {

        final JpaGroup group = getGroup(aGroupId);
        entityManager.remove(group);
    }

    @Override
    public JpaGroup updateGroupStatus(Event<SetGroupStateCommand> aGroupToUpdate) {

        final SetGroupStateCommand updateGroupCommand = aGroupToUpdate.getCommand();
        final AuditEvent auditEvent = aGroupToUpdate.getAuditEvent();
        final Identifier<Group> groupId = updateGroupCommand.getId();
        final JpaGroup jpaGroup = getGroup(groupId);
        
        if (jpaGroup == null) {
            throw new NotFoundException(AemFaultType.GROUP_NOT_FOUND,
                                        "Group not found: " + groupId);
        }

        jpaGroup.setState(updateGroupCommand.getNewGroupState());
        jpaGroup.setUpdateBy(auditEvent.getUser().getUserId());
        jpaGroup.setLastUpdateDate(auditEvent.getDateTime().getCalendar());

        entityManager.flush();
        
        return jpaGroup;
    }

}

