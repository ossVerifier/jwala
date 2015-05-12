package com.siemens.cto.aem.persistence.jpa.service.resource.impl;

import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.exception.NotUniqueException;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.domain.model.resource.command.ResourceInstanceCommand;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaResourceInstance;
import com.siemens.cto.aem.persistence.jpa.service.group.GroupCrudService;
import com.siemens.cto.aem.persistence.jpa.service.resource.ResourceInstanceCrudService;

/**
 * Created by z003e5zv on 3/25/2015.
 */
public class ResourceInstanceCrudServiceImpl implements ResourceInstanceCrudService {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;
    private final GroupCrudService groupCrudService;

    public ResourceInstanceCrudServiceImpl(GroupCrudService groupCrudService) {
        this.groupCrudService = groupCrudService;
    }

    @Override
    public JpaResourceInstance createResourceInstance(final Event<ResourceInstanceCommand> aResourceInstanceToCreate) throws NotUniqueException {

        JpaResourceInstance jpaResourceInstance = new JpaResourceInstance();
        final AuditEvent auditEvent = aResourceInstanceToCreate.getAuditEvent();
        final Calendar updateTime = auditEvent.getDateTime().getCalendar();
        final String userId = auditEvent.getUser().getUserId();
        ResourceInstanceCommand command = aResourceInstanceToCreate.getCommand();

        final JpaGroup group = groupCrudService.getGroup(command.getGroupName());

        jpaResourceInstance.setName(command.getName());
        jpaResourceInstance.setGroup(group);
        jpaResourceInstance.setAttributes(command.getAttributes());
        jpaResourceInstance.setResourceTypeName(command.getResourceTypeName());

        jpaResourceInstance.setCreateBy(userId);
        jpaResourceInstance.setCreateDate(updateTime);
        jpaResourceInstance.setUpdateBy(userId);
        jpaResourceInstance.setLastUpdateDate(updateTime);

        entityManager.persist(jpaResourceInstance);
        entityManager.flush();
        return jpaResourceInstance;
    }

    @Override
    public JpaResourceInstance updateResourceInstanceAttributes(final Identifier<ResourceInstance> resourceInstanceId, final Event<ResourceInstanceCommand> aResourceInstanceToUpdate) {
        ResourceInstanceCommand command = aResourceInstanceToUpdate.getCommand();

        JpaResourceInstance jpaResourceInstance = getJpaResourceInstance(resourceInstanceId);
        jpaResourceInstance.setAttributes(command.getAttributes());
        entityManager.persist(jpaResourceInstance);
        entityManager.flush();
        return jpaResourceInstance;
    }
    @Override
    public JpaResourceInstance updateResourceInstanceName(final Identifier<ResourceInstance> resourceInstanceId, final Event<ResourceInstanceCommand> updateResourceInstanceNameCommandEvent) {
        ResourceInstanceCommand command = updateResourceInstanceNameCommandEvent.getCommand();

        JpaResourceInstance jpaResourceInstance = getJpaResourceInstance(resourceInstanceId);
        jpaResourceInstance.setName(command.getName());
        entityManager.persist(jpaResourceInstance);
        entityManager.flush();
        return jpaResourceInstance;
    }


    @Override
    public JpaResourceInstance getResourceInstance(final Identifier<ResourceInstance> aResourceInstanceId) throws NotFoundException {
        return getJpaResourceInstance(aResourceInstanceId);
    }

    @SuppressWarnings("unchecked")
	@Override
    public List<JpaResourceInstance> getResourceInstancesByGroupId(final Long groupId) {
        final Query query = entityManager.createQuery("SELECT r from JpaResourceInstance r where r.group.id = :groupId");
        query.setParameter("groupId", groupId);
        return query.getResultList();
    }


    @Override
    public JpaResourceInstance getResourceInstanceByGroupIdAndName(final Long groupId, final String name) throws NotFoundException, NotUniqueException {
        final Query query = entityManager.createQuery("SELECT r from JpaResourceInstance r where r.resourceInstanceName = :name and r.group.id = :groupId");
        query.setParameter("name", name);
        query.setParameter("groupId", groupId);
        @SuppressWarnings("unchecked")
		List<JpaResourceInstance> list = query.getResultList();
        if (list == null || list.size() < 1) {
            throw new NotFoundException(AemFaultType.RESOURCE_INSTANCE_NOT_FOUND, "ResourceInstance (group: " + groupId + ", name: " + name + ") not found");
        } else if (list.size() > 1){
            throw new NotUniqueException(AemFaultType.DATA_CONTROL_ERROR, "ResourceInstance (group: " + groupId + ", name: " + name + ") not unique");
        }
        return list.get(0);
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public List<JpaResourceInstance> getResourceInstancesByGroupIdAndResourceTypeName(final Long groupId, final String resourceTypeName) {
        final Query query = entityManager.createQuery("SELECT r from JpaResourceInstance r where r.resourceTypeName = :resourceTypeName and r.group.id = :groupId");
        query.setParameter("resourceTypeName", resourceTypeName);
        query.setParameter("groupId", groupId);
        return query.getResultList();
    }

    @Override
    public void deleteResourceInstance(final Identifier<ResourceInstance> aResourceInstanceId) {
        JpaResourceInstance jpaResourceInstance = getJpaResourceInstance(aResourceInstanceId);
        entityManager.remove(jpaResourceInstance);
        entityManager.flush();
    }

    protected JpaResourceInstance getJpaResourceInstance(final Identifier<ResourceInstance> aResourceInstanceId) throws NotFoundException {

        final JpaResourceInstance jpaResourceInstance = entityManager.find(JpaResourceInstance.class,
                aResourceInstanceId.getId());

        if (jpaResourceInstance == null) {
            throw new NotFoundException(AemFaultType.RESOURCE_INSTANCE_NOT_FOUND,
                    "Resource Instance not found: " + jpaResourceInstance);
        }
        return jpaResourceInstance;
    }
}
