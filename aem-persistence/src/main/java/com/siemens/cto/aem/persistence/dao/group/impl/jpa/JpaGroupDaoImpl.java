package com.siemens.cto.aem.persistence.dao.group.impl.jpa;

import java.util.ArrayList;
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
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.persistence.dao.group.GroupDao;
import com.siemens.cto.aem.persistence.domain.JpaGroup;

public class JpaGroupDaoImpl implements GroupDao {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;

    @Override
    public Group createGroup(final Event<CreateGroupCommand> aGroupToCreate) {

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

            entityManager.persist(jpaGroup);
            entityManager.flush();

            return groupFrom(jpaGroup);
        } catch (final EntityExistsException eee) {
            throw new BadRequestException(AemFaultType.INVALID_GROUP_NAME,
                                          "Group Name already exists: " + aGroupToCreate.getCommand().getGroupName());
        }
    }

    @Override
    public Group updateGroup(final Event<UpdateGroupCommand> aGroupToUpdate) {

        try {
            final UpdateGroupCommand updateGroupCommand = aGroupToUpdate.getCommand();
            final AuditEvent auditEvent = aGroupToUpdate.getAuditEvent();
            final Identifier<Group> groupId = updateGroupCommand.getId();
            final JpaGroup jpaGroup = getJpaGroup(groupId);

            jpaGroup.setName(updateGroupCommand.getNewName());
            jpaGroup.setUpdateBy(auditEvent.getUser().getUserId());
            jpaGroup.setLastUpdateDate(auditEvent.getDateTime().getCalendar());

            entityManager.flush();

            return groupFrom(jpaGroup);
        } catch (final EntityExistsException eee) {
            throw new BadRequestException(AemFaultType.INVALID_GROUP_NAME,
                                          "Group Name already exists: " + aGroupToUpdate.getCommand().getNewName());
        }
    }

    @Override
    public Group getGroup(final Identifier<Group> aGroupId) throws NotFoundException {
        return groupFrom(getJpaGroup(aGroupId));
    }

    @Override
    public List<Group> getGroups(final PaginationParameter somePagination) {

        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<JpaGroup> criteria = builder.createQuery(JpaGroup.class);
        final Root<JpaGroup> root = criteria.from(JpaGroup.class);

        criteria.select(root);

        final TypedQuery<JpaGroup> query = entityManager.createQuery(criteria);

        query.setFirstResult(somePagination.getOffset());
        if(somePagination.getLimit() != PaginationParameter.NO_LIMIT) {
            query.setMaxResults(somePagination.getLimit());
        }

        return groupsFrom(query.getResultList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Group> findGroups(final String aName,
                                  final PaginationParameter somePagination) {

        final Query query = entityManager.createQuery("SELECT g FROM JpaGroup g WHERE g.name LIKE :groupName");
        query.setParameter("groupName", "?" + aName + "?");

        query.setFirstResult(somePagination.getOffset());
        if(somePagination.getLimit() != PaginationParameter.NO_LIMIT) {
            query.setMaxResults(somePagination.getLimit());
        }

        return groupsFrom(query.getResultList());
    }

    @Override
    public void removeGroup(final Identifier<Group> aGroupId) {

        final JpaGroup group = getJpaGroup(aGroupId);
        entityManager.remove(group);
    }

    @Override
    public Group getGroup(String aGroupName) throws NotFoundException {
        return groupFrom(getJpaGroup(aGroupName));
    }

    protected List<Group> groupsFrom(final List<JpaGroup> someJpaGroups) {

        final List<Group> groups = new ArrayList<>();

        for (final JpaGroup jpaGroup : someJpaGroups) {
            groups.add(groupFrom(jpaGroup));
        }

        return groups;
    }

    protected JpaGroup getJpaGroup(final Identifier<Group> aGroup) {

        final JpaGroup jpaGroup = entityManager.find(JpaGroup.class,
                                                     aGroup.getId());

        if (jpaGroup == null) {
            throw new NotFoundException(AemFaultType.GROUP_NOT_FOUND,
                                        "Group not found: " + aGroup);
        }

        return jpaGroup;
    }

    protected JpaGroup getJpaGroup(final String aGroupName) {

        final JpaGroup jpaGroup = entityManager.find(JpaGroup.class,
                aGroupName);

        if (jpaGroup == null) {
            throw new NotFoundException(AemFaultType.GROUP_NOT_FOUND,
                    "Group not found: " + aGroupName);
        }

        return jpaGroup;
    }

    protected Group groupFrom(final JpaGroup aJpaGroup) {
        return new JpaGroupBuilder(aJpaGroup).build();
    }
}
