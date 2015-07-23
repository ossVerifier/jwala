package com.siemens.cto.aem.persistence.jpa.service.jvm.impl;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.UpdateJvmCommand;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.service.jvm.JvmCrudService;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Calendar;
import java.util.List;

public class JvmCrudServiceImpl implements JvmCrudService {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;

    @Override
    public JpaJvm createJvm(final Event<CreateJvmCommand> aJvmToCreate) {

        try {
            final JpaJvm jpaJvm = new JpaJvm();
            final AuditEvent auditEvent = aJvmToCreate.getAuditEvent();
            final Calendar updateTime = auditEvent.getDateTime().getCalendar();
            final String userId = auditEvent.getUser().getUserId();
            final CreateJvmCommand command = aJvmToCreate.getCommand();

            jpaJvm.setName(command.getJvmName());
            jpaJvm.setHostName(command.getHostName());
            jpaJvm.setHttpPort(command.getHttpPort());
            jpaJvm.setHttpsPort(command.getHttpsPort());
            jpaJvm.setRedirectPort(command.getRedirectPort());
            jpaJvm.setShutdownPort(command.getShutdownPort());
            jpaJvm.setAjpPort(command.getAjpPort());
            jpaJvm.setStatusPath(command.getStatusPath().getPath());
            jpaJvm.setSystemProperties(command.getSystemProperties());

            jpaJvm.setCreateBy(userId);
            jpaJvm.setCreateDate(updateTime);
            jpaJvm.setUpdateBy(userId);
            jpaJvm.setLastUpdateDate(updateTime);

            entityManager.persist(jpaJvm);
            entityManager.flush();

            return jpaJvm;
        } catch (final EntityExistsException eee) {
            throw new BadRequestException(AemFaultType.INVALID_JVM_NAME,
                                          "JVM with name already exists: " + aJvmToCreate.getCommand().getJvmName(),
                                          eee);
        }
    }

    @Override
    public JpaJvm updateJvm(final Event<UpdateJvmCommand> aJvmToUpdate) {

        try {
            final UpdateJvmCommand command = aJvmToUpdate.getCommand();
            final Identifier<Jvm> jvmId = command.getId();
            final JpaJvm jvm = getJvm(jvmId);

            jvm.setName(command.getNewJvmName());
            jvm.setHostName(command.getNewHostName());
            jvm.setHttpPort(command.getNewHttpPort());
            jvm.setHttpsPort(command.getNewHttpsPort());
            jvm.setRedirectPort(command.getNewRedirectPort());
            jvm.setShutdownPort(command.getNewShutdownPort());
            jvm.setAjpPort(command.getNewAjpPort());
            jvm.setStatusPath(command.getNewStatusPath().getPath());
            jvm.setSystemProperties(command.getNewSystemProperties());

            entityManager.flush();

            return jvm;
        } catch (final EntityExistsException eee) {
            throw new BadRequestException(AemFaultType.INVALID_JVM_NAME,
                                          "JVM with name already exists: " + aJvmToUpdate.getCommand().getNewJvmName(),
                                          eee);
        }
    }

    @Override
    public JpaJvm getJvm(final Identifier<Jvm> aJvmId) throws NotFoundException {

        final JpaJvm jvm = entityManager.find(JpaJvm.class,
                                              aJvmId.getId());

        if (jvm == null) {
            throw new NotFoundException(AemFaultType.JVM_NOT_FOUND,
                                        "Jvm not found: " + aJvmId);
        }

        return jvm;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<JpaJvm> getJvms() {

        final Query query = entityManager.createQuery("SELECT j FROM JpaJvm j");

        return query.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<JpaJvm> findJvms(final String aName) {

        final Query query = entityManager.createQuery("SELECT j FROM JpaJvm j WHERE j.name LIKE :jvmName ORDER BY j.name");

        query.setParameter("jvmName",
                           "%" + aName + "%");

        return query.getResultList();
    }

    @Override
    public void removeJvm(final Identifier<Jvm> aJvmId) {

        final JpaJvm jvm = getJvm(aJvmId);

        entityManager.remove(jvm);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<JpaJvm> findJvmsBelongingTo(final Identifier<Group> aGroup) {
        final Query query = entityManager.createQuery("SELECT j FROM JpaGroup g join g.jvms j WHERE g.id = :groupId ORDER BY j.name");

        query.setParameter("groupId", aGroup.getId());

        return query.getResultList();
    }
}
