package com.siemens.cto.aem.persistence.dao.impl;

import com.siemens.cto.aem.common.request.jvm.CreateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UpdateJvmRequest;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.persistence.dao.JvmDao;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaJvmBuilder;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class JpaJvmDaoImpl implements JvmDao {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;


    public JpaJvmDaoImpl() {
    }

    @Override
    public Jvm createJvm(final Event<CreateJvmRequest> aJvmToCreate) {

        try {
            final JpaJvm jpaJvm = new JpaJvm();
            final AuditEvent auditEvent = aJvmToCreate.getAuditEvent();
            final Calendar updateTime = auditEvent.getDateTime().getCalendar();
            final String userId = auditEvent.getUser().getUserId();
            final CreateJvmRequest command = aJvmToCreate.getRequest();

            jpaJvm.setName(command.getJvmName());
            jpaJvm.setHostName(command.getHostName());
            jpaJvm.setHttpPort(command.getHttpPort());
            jpaJvm.setHttpsPort(command.getHttpsPort());
            jpaJvm.setRedirectPort(command.getRedirectPort());
            jpaJvm.setShutdownPort(command.getShutdownPort());
            jpaJvm.setAjpPort(command.getAjpPort());
            jpaJvm.setStatusPath(command.getStatusPath().getUriPath());
            jpaJvm.setSystemProperties(command.getSystemProperties());

            jpaJvm.setCreateBy(userId);
            jpaJvm.setCreateDate(updateTime);
            jpaJvm.setUpdateBy(userId);
            jpaJvm.setLastUpdateDate(updateTime);

            entityManager.persist(jpaJvm);
            entityManager.flush();

            return jvmFrom(jpaJvm);
        } catch (final EntityExistsException eee) {
            throw new BadRequestException(AemFaultType.INVALID_JVM_NAME,
                                          "JVM with name already exists: " + aJvmToCreate.getRequest().getJvmName(),
                                          eee);
        }
    }

    @Override
    public Jvm updateJvm(final Event<UpdateJvmRequest> aJvmToUpdate) {

        try {
            final UpdateJvmRequest command = aJvmToUpdate.getRequest();
            final Identifier<Jvm> jvmId = command.getId();
            final JpaJvm jvm = getJpaJvm(jvmId);

            jvm.setName(command.getNewJvmName());
            jvm.setHostName(command.getNewHostName());
            jvm.setHttpPort(command.getNewHttpPort());
            jvm.setHttpsPort(command.getNewHttpsPort());
            jvm.setRedirectPort(command.getNewRedirectPort());
            jvm.setShutdownPort(command.getNewShutdownPort());
            jvm.setAjpPort(command.getNewAjpPort());
            jvm.setStatusPath(command.getNewStatusPath().getUriPath());
            jvm.setSystemProperties(command.getNewSystemProperties());

            entityManager.flush();

            return getJvm(jvmId);
        } catch (final EntityExistsException eee) {
            throw new BadRequestException(AemFaultType.INVALID_JVM_NAME,
                                          "JVM with name already exists: " + aJvmToUpdate.getRequest().getNewJvmName(),
                                          eee);
        }
    }

    @Override
    public Jvm getJvm(final Identifier<Jvm> aJvmId) throws NotFoundException {

        final JpaJvm jpaJvm = getJpaJvm(aJvmId);

        return jvmFrom(jpaJvm);
    }

    @Override
    public List<Jvm> getJvms() {

        final Query query = entityManager.createQuery("SELECT j FROM JpaJvm j");

        return jvmsFrom(query.getResultList());
    }

    @Override
    public List<Jvm> findJvms(final String aName) {

        final Query query = entityManager.createQuery("SELECT j FROM JpaJvm j WHERE j.name LIKE :jvmName ORDER BY j.name");

        query.setParameter("jvmName", "%" + aName + "%");

        return jvmsFrom(query.getResultList());
    }

    @Override
    public List<Jvm> findJvmsBelongingTo(final Identifier<Group> aGroup) {

        final Query query = entityManager.createQuery("SELECT j FROM JpaJvm j JOIN j.groups g WHERE g.id = :groupId ORDER BY j.name");

        query.setParameter("groupId", aGroup.getId());

        return jvmsFrom(query.getResultList());
    }

    @Override
    public void removeJvm(final Identifier<Jvm> aJvmId) {

        final JpaJvm jvm = getJpaJvm(aJvmId);

        entityManager.remove(jvm);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void removeJvmsBelongingTo(final Identifier<Group> aGroupId) {

        final Query query = entityManager.createQuery("SELECT j FROM JpaJvm j JOIN j.groups g WHERE g.id = :groupId");
        query.setParameter("groupId", aGroupId.getId());

        final List<JpaJvm> jvms = query.getResultList();
        for (final JpaJvm jvm : jvms) {
            entityManager.remove(jvm);
        }
    }

    @Override
    public Jvm findJvm(String jvmName, String groupName) {
        final Query q = entityManager.createNamedQuery(JpaJvm.QUERY_FIND_JVM_BY_GROUP_AND_JVM_NAME);
        q.setParameter("jvmName", jvmName);
        q.setParameter("groupName", groupName);
        return jvmFrom((JpaJvm) q.getSingleResult());
    }

    protected Jvm jvmFrom(final JpaJvm aJpaJvm) {

        final JpaJvmBuilder builder = new JpaJvmBuilder(aJpaJvm);

        return builder.build();
    }

    protected JpaJvm getJpaJvm(final Identifier<Jvm> aJvmId) throws NotFoundException {

        final JpaJvm jvm = entityManager.find(JpaJvm.class,
                                              aJvmId.getId());

        if (jvm == null) {
            throw new NotFoundException(AemFaultType.JVM_NOT_FOUND,
                                        "Jvm not found: " + aJvmId);
        }

        return jvm;
    }

    protected List<Jvm> jvmsFrom(final List<?> someJpaJvms) {

        final List<Jvm> jvms = new ArrayList<>();

        for (final Object jpaJvm : someJpaJvms) {

            assert jpaJvm instanceof JpaJvm;

            jvms.add(jvmFrom((JpaJvm)jpaJvm));
        }

        return jvms;
    }
}
