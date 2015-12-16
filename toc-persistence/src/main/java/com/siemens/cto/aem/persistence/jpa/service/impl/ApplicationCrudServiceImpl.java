package com.siemens.cto.aem.persistence.jpa.service.impl;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.request.app.CreateApplicationRequest;
import com.siemens.cto.aem.common.request.app.UpdateApplicationRequest;
import com.siemens.cto.aem.common.request.app.UploadAppTemplateRequest;
import com.siemens.cto.aem.common.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.persistence.jpa.domain.*;
import com.siemens.cto.aem.persistence.jpa.service.ApplicationCrudService;
import com.siemens.cto.aem.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.siemens.cto.aem.persistence.jpa.service.exception.ResourceTemplateUpdateException;

import javax.persistence.*;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

public class ApplicationCrudServiceImpl implements ApplicationCrudService {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;

    public ApplicationCrudServiceImpl() {
    }

    @Override
    public JpaApplication createApplication(final Event<CreateApplicationRequest> anAppToCreate, JpaGroup jpaGroup) {

        try {
            final CreateApplicationRequest createAppCommand = anAppToCreate.getRequest();
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
                                          "App already exists: " + anAppToCreate.getRequest().getName(),
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
    public List<String> getResourceTemplateNames(final String appName) {
        final Query q = entityManager.createNamedQuery(JpaApplicationConfigTemplate.GET_APP_RESOURCE_TEMPLATE_NAMES);
        q.setParameter("appName", appName);
        return q.getResultList();
    }

    @Override
    public String getResourceTemplate(final String appName, final String resourceTemplateName) {
        final Query q = entityManager.createNamedQuery(JpaApplicationConfigTemplate.GET_APP_TEMPLATE_CONTENT);
        q.setParameter("appName", appName);
        q.setParameter("templateName", resourceTemplateName);
        try {
            return (String) q.getSingleResult();
        } catch (NoResultException | NonUniqueResultException e) {
            throw new NonRetrievableResourceTemplateContentException(appName, resourceTemplateName, e);
        }
    }

    @Override
    public JpaApplication updateApplication(final Event<UpdateApplicationRequest> anApplicationToUpdate, JpaApplication jpaApp, JpaGroup jpaGroup) {

        final UpdateApplicationRequest updateApplicationCommand = anApplicationToUpdate.getRequest();
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

    @Override
    public void updateResourceTemplate(final String appName, final String resourceTemplateName, final String template) {
        final Query q = entityManager.createNamedQuery(JpaApplicationConfigTemplate.UPDATE_APP_TEMPLATE_CONTENT);
        q.setParameter("appName", appName);
        q.setParameter("templateName", resourceTemplateName);
        q.setParameter("templateContent", template);

        try {
            if (q.executeUpdate() == 0) {
                throw new ResourceTemplateUpdateException(appName, resourceTemplateName);
            }
        } catch (RuntimeException re) {
            throw new ResourceTemplateUpdateException(appName, resourceTemplateName, re);
        }
    }

    @Override
    public void createConfigTemplate(final JpaApplication app,
                                     final String resourceTemplateName,
                                     final String resourceTemplateContent) {
        final JpaApplicationConfigTemplate template = new JpaApplicationConfigTemplate();
        template.setApplication(app);
        template.setTemplateName(resourceTemplateName);
        template.setTemplateContent(resourceTemplateContent);
        entityManager.persist(template);
        entityManager.flush();
    }

    @Override
    public JpaApplicationConfigTemplate uploadAppTemplate(Event<UploadAppTemplateRequest> event) {
        UploadAppTemplateRequest command = event.getRequest();
        Application application = command.getApp();
        Identifier<Application> id = application.getId();
        JpaApplication jpaApp = getExisting(id);

        InputStream inStream = command.getData();
        Scanner scanner = new Scanner(inStream).useDelimiter("\\A");
        String templateContent = scanner.hasNext() ? scanner.next() : "";

        // get an instance and then do a create or update
        Query query = entityManager.createQuery("SELECT t FROM JpaApplicationConfigTemplate t where t.templateName = :tempName and t.app.name = :appName");
        query.setParameter("appName", application.getName());
        query.setParameter("tempName", command.getConfFileName());
        List<JpaApplicationConfigTemplate> templates = query.getResultList();
        JpaApplicationConfigTemplate jpaConfigTemplate;
        if (templates.size() == 1) {
            //update
            jpaConfigTemplate = templates.get(0);
            jpaConfigTemplate.setTemplateContent(templateContent);
            entityManager.flush();
        } else if (templates.isEmpty()) {
            //create
            jpaConfigTemplate = new JpaApplicationConfigTemplate();
            jpaConfigTemplate.setApplication(jpaApp);
            jpaConfigTemplate.setTemplateName(command.getConfFileName());
            jpaConfigTemplate.setTemplateContent(templateContent);
            entityManager.persist(jpaConfigTemplate);
            entityManager.flush();
        } else {
            throw new BadRequestException(AemFaultType.APPLICATION_NOT_FOUND,
                    "Only expecting one template to be returned for application [" + application.getName() + "] but returned " + templates.size() + " templates");
        }

        return jpaConfigTemplate;
    }

}

