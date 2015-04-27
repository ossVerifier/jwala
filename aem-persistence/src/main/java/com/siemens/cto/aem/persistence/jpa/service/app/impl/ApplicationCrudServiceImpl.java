package com.siemens.cto.aem.persistence.jpa.service.app.impl;

import java.util.Calendar;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.app.CreateApplicationCommand;
import com.siemens.cto.aem.domain.model.app.UpdateApplicationCommand;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplication;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.service.app.ApplicationCrudService;

public class ApplicationCrudServiceImpl implements ApplicationCrudService {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;

    public ApplicationCrudServiceImpl() {
    }

    @Override
    public JpaApplication createApplication(final Event<CreateApplicationCommand> anAppToCreate, JpaGroup jpaGroup) {

        try {
            final CreateApplicationCommand createAppCommand = anAppToCreate.getCommand();
            final AuditEvent auditEvent = anAppToCreate.getAuditEvent();
            final String userId = auditEvent.getUser().getUserId();
            final Calendar updateDate = auditEvent.getDateTime().getCalendar();

            final JpaApplication jpaApp = new JpaApplication();
            jpaApp.setName(createAppCommand.getName());
            jpaApp.setGroup(jpaGroup);
            jpaApp.setWebAppContext(createAppCommand.getWebAppContext());
            jpaApp.setSecure(createAppCommand.isSecure());
            jpaApp.setLoadBalanceAcrossServers(createAppCommand.isLoadBalanceAcrossServers());

            jpaApp.setCreateBy(userId);
            jpaApp.setCreateDate(updateDate);
            jpaApp.setUpdateBy(userId);
            jpaApp.setLastUpdateDate(updateDate);

            entityManager.persist(jpaApp);
            entityManager.flush();

            return jpaApp;
        } catch (final EntityExistsException eee) {
            throw new BadRequestException(AemFaultType.DUPLICATE_APPLICATION,
                                          "App already exists: " + anAppToCreate.getCommand().getName(),
                                          eee);
        }
    }

    public JpaApplication getExisting(final Identifier<Application> anAppId) {
        JpaApplication jpaApp = entityManager.find(JpaApplication.class, anAppId.getId());
        if(jpaApp == null) {
            throw new BadRequestException(AemFaultType.APPLICATION_NOT_FOUND, "Failed to locate application for update");
        }
        return jpaApp;
    }

    
    @Override
    public JpaApplication updateApplication(final Event<UpdateApplicationCommand> anApplicationToUpdate, JpaApplication jpaApp, JpaGroup jpaGroup) {

        final UpdateApplicationCommand updateApplicationCommand = anApplicationToUpdate.getCommand();
        final AuditEvent auditEvent = anApplicationToUpdate.getAuditEvent();
        final Identifier<Application> appId = updateApplicationCommand.getId();

        if(jpaApp != null) {
            jpaApp.setName(updateApplicationCommand.getNewName());
            jpaApp.setUpdateBy(auditEvent.getUser().getUserId());
            jpaApp.setWebAppContext(updateApplicationCommand.getNewWebAppContext());
            jpaApp.setLastUpdateDate(auditEvent.getDateTime().getCalendar());
            jpaApp.setGroup(jpaGroup);
            jpaApp.setSecure(updateApplicationCommand.isNewSecure());
            jpaApp.setLoadBalanceAcrossServers(updateApplicationCommand.isNewLoadBalanceAcrossServers());

            entityManager.flush();
            
            return jpaApp;
        } else {
            throw new BadRequestException(AemFaultType.INVALID_APPLICATION_NAME,
                    "Application cannot be found: " + appId.getId());
        }
    }

    @Override
    public void removeApplication(final Identifier<Application> appId) {

        final JpaApplication jpaApp = entityManager.find(JpaApplication.class, appId.getId());
        if(jpaApp != null) {
            entityManager.remove(jpaApp);
        } else {
            throw new BadRequestException(AemFaultType.INVALID_APPLICATION_NAME,
                    "Application cannot be found: " + appId.getId());
        }
    }
}

