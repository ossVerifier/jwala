package com.siemens.cto.aem.persistence.dao.resource.impl.jpa;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.CreateGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.UpdateGroupCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.domain.model.resource.command.CreateResourceInstanceCommand;
import com.siemens.cto.aem.domain.model.resource.command.UpdateResourceInstanceCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.persistence.dao.resource.ResourceInstanceDao;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.domain.JpaResourceInstance;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaResourceInstanceBuilder;
import com.siemens.cto.aem.persistence.jpa.service.JpaQueryPaginator;

import javax.naming.NameNotFoundException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Calendar;
import java.util.List;

/**
 * Created by z003e5zv on 3/16/2015.
 */
public class JpaResourceInstanceDaoImpl implements ResourceInstanceDao {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;

    private final JpaQueryPaginator paginator;

    public JpaResourceInstanceDaoImpl() {
        paginator = new JpaQueryPaginator();
    }

    @Override
    public ResourceInstance createResourceInstance(Event<CreateResourceInstanceCommand> aResourceInstanceToCreate) {

        JpaResourceInstance jpaResourceInstance = new JpaResourceInstance();
        final AuditEvent auditEvent = aResourceInstanceToCreate.getAuditEvent();
        final Calendar updateTime = auditEvent.getDateTime().getCalendar();
        final String userId = auditEvent.getUser().getUserId();
        CreateResourceInstanceCommand command = aResourceInstanceToCreate.getCommand();

        jpaResourceInstance.setName(command.getResourceTypeName());
        jpaResourceInstance.setParentId(command.getParentId());
        jpaResourceInstance.setParentType(command.getParentType());
        jpaResourceInstance.setAttributes(command.getAttributes());
        jpaResourceInstance.setResourceTypeName(command.getResourceTypeName());

        jpaResourceInstance.setCreateBy(userId);
        jpaResourceInstance.setCreateDate(updateTime);
        jpaResourceInstance.setUpdateBy(userId);
        jpaResourceInstance.setLastUpdateDate(updateTime);

        entityManager.persist(jpaResourceInstance);
        entityManager.flush();
        JpaResourceInstanceBuilder builder = new JpaResourceInstanceBuilder(jpaResourceInstance);
        return builder.build();

    }

    @Override
    public ResourceInstance updateResourceInstance(Event<UpdateResourceInstanceCommand> aResourceInstanceToUpdate) {
        UpdateResourceInstanceCommand command = aResourceInstanceToUpdate.getCommand();

        JpaResourceInstance jpaResourceInstance = getJpaResourceInstance(command.getResourceInstanceId());
        jpaResourceInstance.setAttributes(command.getAttributes());
        entityManager.persist(jpaResourceInstance);
        entityManager.flush();
        JpaResourceInstanceBuilder builder = new JpaResourceInstanceBuilder(jpaResourceInstance);
        return builder.build();
    }

    @Override
    public ResourceInstance getResourceInstance(Identifier<ResourceInstance> aResourceInstanceId) throws NotFoundException {
        JpaResourceInstance jpaResourceInstance = getJpaResourceInstance(aResourceInstanceId);
        JpaResourceInstanceBuilder builder = new JpaResourceInstanceBuilder(jpaResourceInstance);
        return builder.build();
    }


    @Override
    public void removeResourceInstance(Identifier<ResourceInstance> aResourceInstanceId) {
        try {
            JpaResourceInstance jpaResourceInstance = getJpaResourceInstance(aResourceInstanceId);
            entityManager.remove(jpaResourceInstance);
            entityManager.flush();
        }
        catch(NotFoundException nfe) {
            System.out.println("A resource instance that should be deleted does not exist, the horror!");
        }
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
