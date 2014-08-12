package com.siemens.cto.aem.persistence.dao.jvm.impl.jpa;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.UpdateJvmCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.persistence.dao.jvm.JvmDao;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaJvmBuilder;
import com.siemens.cto.aem.persistence.jpa.service.JpaQueryPaginator;

public class JpaJvmDaoImpl implements JvmDao {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;

    private final JpaQueryPaginator paginator;

    public JpaJvmDaoImpl() {
        paginator = new JpaQueryPaginator();
    }

    @Override
    public Jvm createJvm(final Event<CreateJvmCommand> aJvmToCreate) {

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

            jpaJvm.setCreateBy(userId);
            jpaJvm.setCreateDate(updateTime);
            jpaJvm.setUpdateBy(userId);
            jpaJvm.setLastUpdateDate(updateTime);

            entityManager.persist(jpaJvm);
            entityManager.flush();

            return jvmFrom(jpaJvm);
        } catch (final EntityExistsException eee) {
            throw new BadRequestException(AemFaultType.INVALID_JVM_NAME,
                                          "JVM with name already exists: " + aJvmToCreate.getCommand().getJvmName(),
                                          eee);
        }
    }

    @Override
    public Jvm updateJvm(final Event<UpdateJvmCommand> aJvmToUpdate) {

        try {
            final UpdateJvmCommand command = aJvmToUpdate.getCommand();
            final Identifier<Jvm> jvmId = command.getId();
            final JpaJvm jvm = getJpaJvm(jvmId);

            jvm.setName(command.getNewJvmName());
            jvm.setHostName(command.getNewHostName());
            jvm.setHttpPort(command.getNewHttpPort());
            jvm.setHttpsPort(command.getNewHttpsPort());
            jvm.setRedirectPort(command.getNewRedirectPort());
            jvm.setShutdownPort(command.getNewShutdownPort());
            jvm.setAjpPort(command.getNewAjpPort());
            jvm.setStatusPath(command.getNewStatusPath().getPath());

            entityManager.flush();

            return getJvm(jvmId);
        } catch (final EntityExistsException eee) {
            throw new BadRequestException(AemFaultType.INVALID_JVM_NAME,
                                          "JVM with name already exists: " + aJvmToUpdate.getCommand().getNewJvmName(),
                                          eee);
        }
    }

    @Override
    public Jvm getJvm(final Identifier<Jvm> aJvmId) throws NotFoundException {

        final JpaJvm jpaJvm = getJpaJvm(aJvmId);

        return jvmFrom(jpaJvm);
    }

    @Override
    public List<Jvm> getJvms(final PaginationParameter somePagination) {

        final Query query = entityManager.createQuery("SELECT j FROM JpaJvm j");

        paginator.paginate(query,
                           somePagination);

        return jvmsFrom(query.getResultList());
    }

    @Override
    public List<Jvm> findJvms(final String aName,
                              final PaginationParameter somePagination) {

        final Query query = entityManager.createQuery("SELECT j FROM JpaJvm j WHERE j.name LIKE :jvmName ORDER BY j.name");

        query.setParameter("jvmName", "%" + aName + "%");
        paginator.paginate(query,
                           somePagination);

        return jvmsFrom(query.getResultList());
    }

    @Override
    public List<Jvm> findJvmsBelongingTo(final Identifier<Group> aGroup,
                                         final PaginationParameter somePagination) {

        final Query query = entityManager.createQuery("SELECT j FROM JpaJvm j JOIN j.groups g WHERE g.id = :groupId ORDER BY j.name");

        query.setParameter("groupId", aGroup.getId());
        paginator.paginate(query,
                           somePagination);

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
