package com.siemens.cto.aem.persistence.jpa.service.impl;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.request.app.CreateApplicationRequest;
import com.siemens.cto.aem.common.request.app.UpdateApplicationRequest;
import com.siemens.cto.aem.common.request.app.UploadAppTemplateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplication;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplicationConfigTemplate;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaAppBuilder;
import com.siemens.cto.aem.persistence.jpa.service.ApplicationCrudService;
import com.siemens.cto.aem.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.siemens.cto.aem.persistence.jpa.service.exception.ResourceTemplateUpdateException;

import javax.persistence.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ApplicationCrudServiceImpl extends AbstractCrudServiceImpl<JpaApplication> implements ApplicationCrudService {

    public ApplicationCrudServiceImpl() {
    }

    @Override
    public JpaApplication createApplication(CreateApplicationRequest createApplicationRequest, JpaGroup jpaGroup) {


        final JpaApplication jpaApp = new JpaApplication();
        jpaApp.setName(createApplicationRequest.getName());
        jpaApp.setGroup(jpaGroup);
        jpaApp.setWebAppContext(createApplicationRequest.getWebAppContext());
        jpaApp.setSecure(createApplicationRequest.isSecure());
        jpaApp.setLoadBalanceAcrossServers(createApplicationRequest.isLoadBalanceAcrossServers());
        jpaApp.setUnpackWar(createApplicationRequest.isUnpackWar());

        try {
            return create(jpaApp);
        } catch (final EntityExistsException eee) {
            throw new BadRequestException(AemFaultType.DUPLICATE_APPLICATION,
                    "App already exists: " + createApplicationRequest.getName(),
                    eee);
        }
    }

    public JpaApplication getExisting(final Identifier<Application> anAppId) {
        return findById(anAppId.getId());
    }

    @Override
    public List<String> getResourceTemplateNames(final String appName) {
        final Query q = entityManager.createNamedQuery(JpaApplicationConfigTemplate.GET_APP_RESOURCE_TEMPLATE_NAMES);
        q.setParameter("appName", appName);
        return q.getResultList();
    }

    @Override
    public String getResourceTemplate(String appName, String resourceTemplateName, String jvmName, String groupName) {
        JpaJvm jpaJvm;
        Query jvmQuery = entityManager.createNamedQuery(JpaJvm.QUERY_FIND_JVM_BY_GROUP_AND_JVM_NAME);
        jvmQuery.setParameter("jvmName", jvmName);
        jvmQuery.setParameter("groupName", groupName);
        try{
            jpaJvm = (JpaJvm) jvmQuery.getSingleResult();
        } catch (NoResultException | NonUniqueResultException e){
            throw new NonRetrievableResourceTemplateContentException(appName, resourceTemplateName, e);
        }
        return getResourceTemplate(appName, resourceTemplateName, jpaJvm);
    }

    @Override
    public String getResourceTemplate(final String appName, final String resourceTemplateName, JpaJvm appJvm) {
        final Query q = entityManager.createNamedQuery(JpaApplicationConfigTemplate.GET_APP_TEMPLATE_CONTENT);
        q.setParameter("appName", appName);
        q.setParameter("templateName", resourceTemplateName);
        q.setParameter("templateJvm", appJvm);
        try {
            return (String) q.getSingleResult();
        } catch (NoResultException | NonUniqueResultException e) {
            throw new NonRetrievableResourceTemplateContentException(appName, resourceTemplateName, e);
        }
    }

    @Override
    public JpaApplication updateApplication(UpdateApplicationRequest updateApplicationRequest, JpaApplication jpaApp, JpaGroup jpaGroup) {

        final Identifier<Application> appId = updateApplicationRequest.getId();

        if (jpaApp != null) {
            jpaApp.setName(updateApplicationRequest.getNewName());
            jpaApp.setWebAppContext(updateApplicationRequest.getNewWebAppContext());
            jpaApp.setGroup(jpaGroup);
            jpaApp.setSecure(updateApplicationRequest.isNewSecure());
            jpaApp.setLoadBalanceAcrossServers(updateApplicationRequest.isNewLoadBalanceAcrossServers());
            jpaApp.setUnpackWar(updateApplicationRequest.isUnpackWar());

            return update(jpaApp);
        } else {
            throw new BadRequestException(AemFaultType.INVALID_APPLICATION_NAME,
                    "Application cannot be found: " + appId.getId());
        }
    }

    @Override
    public void removeApplication(final Identifier<Application> appId) {

        final JpaApplication jpaApp = findById(appId.getId());
        if (jpaApp != null) {
            remove(jpaApp);
        } else {
            throw new BadRequestException(AemFaultType.INVALID_APPLICATION_NAME,
                    "Application cannot be found: " + appId.getId());
        }
    }

    @Override
    public void updateResourceTemplate(final String appName, final String resourceTemplateName, final String template, JpaJvm jvm) {
        final Query q = entityManager.createNamedQuery(JpaApplicationConfigTemplate.UPDATE_APP_TEMPLATE_CONTENT);
        q.setParameter("appName", appName);
        q.setParameter("templateName", resourceTemplateName);
        q.setParameter("templateContent", template);
        q.setParameter("templateJvm", jvm);

        int numEntities;

        try {
            numEntities = q.executeUpdate();
        } catch (RuntimeException re) {
            throw new ResourceTemplateUpdateException(appName, resourceTemplateName, re);
        }

        if (numEntities == 0) {
            throw new ResourceTemplateUpdateException(appName, resourceTemplateName);
        }

    }

    @Override
    public void createConfigTemplate(final JpaApplication app,
                                     final String resourceTemplateName,
                                     final String metaData, final String resourceTemplateContent,
                                     final JpaJvm jvm) {
        final JpaApplicationConfigTemplate template = new JpaApplicationConfigTemplate();
        template.setApplication(app);
        template.setTemplateName(resourceTemplateName);
        template.setMetaData(metaData);
        template.setTemplateContent(resourceTemplateContent);
        template.setJvm(jvm);
        entityManager.persist(template);
        entityManager.flush();
    }

    @Override
    public JpaApplicationConfigTemplate uploadAppTemplate(UploadAppTemplateRequest uploadAppTemplateRequest, JpaJvm jpaJvm) {
        Application application = uploadAppTemplateRequest.getApp();
        Identifier<Application> id = application.getId();
        JpaApplication jpaApp = getExisting(id);

        InputStream inStream = uploadAppTemplateRequest.getData();
        Scanner scanner = new Scanner(inStream).useDelimiter("\\A");
        String templateContent = scanner.hasNext() ? scanner.next() : "";

        // get an instance and then do a create or update
        Query query = entityManager.createNamedQuery(JpaApplicationConfigTemplate.GET_APP_TEMPLATE_NO_JVM);
        if (jpaJvm != null) {
            query = entityManager.createNamedQuery(JpaApplicationConfigTemplate.GET_APP_TEMPLATE);
            query.setParameter("jvmName", jpaJvm.getName());
        }
        query.setParameter("appName", application.getName());
        query.setParameter("tempName", uploadAppTemplateRequest.getConfFileName());
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
            jpaConfigTemplate.setTemplateName(uploadAppTemplateRequest.getConfFileName());
            jpaConfigTemplate.setTemplateContent(templateContent);
            jpaConfigTemplate.setMetaData(uploadAppTemplateRequest.getMedataData());
            if (jpaJvm != null) {
                jpaConfigTemplate.setJvm(jpaJvm);
            }
            entityManager.persist(jpaConfigTemplate);
            entityManager.flush();
        } else {
            throw new BadRequestException(AemFaultType.APPLICATION_NOT_FOUND,
                    "Only expecting one template to be returned for application [" + application.getName() + "] but returned " + templates.size() + " templates");
        }

        return jpaConfigTemplate;
    }

    @Override
    public Application getApplication(Identifier<Application> aApplicationId) throws NotFoundException {
        JpaApplication jpaApp = entityManager.find(JpaApplication.class, aApplicationId.getId());
        if (jpaApp == null) {
            throw new NotFoundException(AemFaultType.APPLICATION_NOT_FOUND,
                    "Application not found: " + aApplicationId);
        }
        return JpaAppBuilder.appFrom(jpaApp);
    }

    @Override
    public List<Application> getApplications() {
        Query q = entityManager.createQuery("select a from JpaApplication a");
        return buildApplications(q.getResultList());
    }

    @SuppressWarnings("unchecked")
    private List<Application> buildApplications(List<?> resultList) {
        ArrayList<Application> apps = new ArrayList<>(resultList.size());
        for (JpaApplication jpa : (List<JpaApplication>) resultList) {
            apps.add(JpaAppBuilder.appFrom(jpa));
        }
        return apps;
    }

    @Override
    public List<Application> findApplicationsBelongingTo(Identifier<Group> aGroupId) {
        Query q = entityManager.createNamedQuery(JpaApplication.QUERY_BY_GROUP_ID);
        q.setParameter(JpaApplication.GROUP_ID_PARAM, aGroupId.getId());
        return buildApplications(q.getResultList());
    }

    @Override
    public List<Application> findApplicationsBelongingTo(final String groupName) {
        Query q = entityManager.createNamedQuery(JpaApplication.QUERY_BY_GROUP_NAME);
        q.setParameter(JpaApplication.GROUP_NAME_PARAM, groupName);
        return buildApplications(q.getResultList());
    }

    @Override
    public List<Application> findApplicationsBelongingToJvm(Identifier<Jvm> aJvmId) {
        Query q = entityManager.createNamedQuery(JpaApplication.QUERY_BY_JVM_ID);
        q.setParameter(JpaApplication.JVM_ID_PARAM, aJvmId.getId());
        return buildApplications(q.getResultList());
    }

    @Override
    public Application findApplication(final String appName, final String groupName, final String jvmName) {
        final Query q = entityManager.createNamedQuery(JpaApplication.QUERY_BY_GROUP_JVM_AND_APP_NAME);
        q.setParameter("appName", appName);
        q.setParameter("groupName", groupName);
        q.setParameter("jvmName", jvmName);
        return JpaAppBuilder.appFrom((JpaApplication) q.getSingleResult());
    }

    @Override
    public Application getApplication(final String name) {
        final Query q = entityManager.createNamedQuery(JpaApplication.QUERY_BY_NAME);
        q.setParameter("appName", name);
        return JpaAppBuilder.appFrom((JpaApplication) q.getSingleResult());
    }

    @Override
    public int removeTemplate(final String name) {
        final Query q = entityManager.createNamedQuery(JpaApplicationConfigTemplate.QUERY_DELETE_APP_TEMPLATE);
        q.setParameter(JpaApplicationConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, name);
        return q.executeUpdate();
    }
}

