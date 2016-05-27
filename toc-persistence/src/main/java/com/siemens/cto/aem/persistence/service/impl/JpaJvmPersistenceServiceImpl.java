package com.siemens.cto.aem.persistence.service.impl;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.request.jvm.CreateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UpdateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.JpaJvmConfigTemplate;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JvmBuilder;
import com.siemens.cto.aem.persistence.jpa.service.ApplicationCrudService;
import com.siemens.cto.aem.persistence.jpa.service.GroupJvmRelationshipService;
import com.siemens.cto.aem.persistence.jpa.service.JvmCrudService;
import com.siemens.cto.aem.persistence.service.JvmPersistenceService;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

public class JpaJvmPersistenceServiceImpl implements JvmPersistenceService {

    private final JvmCrudService jvmCrudService;
    private final ApplicationCrudService applicationCrudService;
    private final GroupJvmRelationshipService groupJvmRelationshipService;

    public JpaJvmPersistenceServiceImpl(final JvmCrudService jvmCrudService,
                                        final ApplicationCrudService applicationCrudService,
                                        final GroupJvmRelationshipService groupJvmRelationshipService) {
        this.jvmCrudService = jvmCrudService;
        this.applicationCrudService = applicationCrudService;
        this.groupJvmRelationshipService = groupJvmRelationshipService;
    }

    @Override
    public Jvm createJvm(CreateJvmRequest createJvmRequest) {
        final JpaJvm jpaJvm = jvmCrudService.createJvm(createJvmRequest);
        return jvmFrom(jpaJvm);
    }

    @Override
    public Jvm updateJvm(UpdateJvmRequest updateJvmRequest) {
        final JpaJvm jpaJvm = jvmCrudService.updateJvm(updateJvmRequest);
        return jvmFrom(jpaJvm);
    }

    @Override
    public Jvm getJvm(final Identifier<Jvm> aJvmId) throws NotFoundException {
        final JpaJvm jpaJvm = jvmCrudService.getJvm(aJvmId);
        return jvmFrom(jpaJvm);
    }

    @Override
    public JpaJvm getJpaJvm(final Identifier<Jvm> aJvmId, final boolean fetchGroups) {
        final JpaJvm jvm = jvmCrudService.getJvm(aJvmId);
        if (fetchGroups) {
            // groups are lazy loaded so we need this.
            jvm.getGroups().size();
        }
        return jvm;
    }

    @Override
    public List<Jvm> getJvms() {
        return jvmsFrom(jvmCrudService.getJvms());
    }

    @Override
    public List<Jvm> findJvms(final String aName) {
        return jvmsFrom(jvmCrudService.findJvms(aName));
    }

    @Override
    public List<Jvm> findJvmsBelongingTo(final Identifier<Group> aGroup) {
        return jvmsFrom(jvmCrudService.findJvmsBelongingTo(aGroup));
    }

    @Override
    public void removeJvm(final Identifier<Jvm> aJvmId) {
        groupJvmRelationshipService.removeRelationshipsForJvm(aJvmId);
        jvmCrudService.removeJvm(aJvmId);
    }

    @Override
    public Jvm removeJvmFromGroups(final Identifier<Jvm> aJvmId) {
        groupJvmRelationshipService.removeRelationshipsForJvm(aJvmId);
        return getJvm(aJvmId);
    }

    @Override
    public JpaJvmConfigTemplate uploadJvmTemplateXml(UploadJvmTemplateRequest uploadJvmTemplateRequest) {
        return jvmCrudService.uploadJvmTemplateXml(uploadJvmTemplateRequest);
    }

    @Override
    public String getJvmTemplate(String templateName, Identifier<Jvm> jvmId) {
        return jvmCrudService.getJvmTemplate(templateName, jvmId);
    }

    @Override
    public List<String> getResourceTemplateNames(final String jvmName) {
        return jvmCrudService.getResourceTemplateNames(jvmName);
    }

    @Override
    public String getResourceTemplate(final String jvmName, final String resourceTemplateName) {
        return jvmCrudService.getResourceTemplate(jvmName, resourceTemplateName);
    }

    @Override
    public String getResourceTemplateMetaData(String jvmName, String fileName) {
        return jvmCrudService.getResourceTemplateMetaData(jvmName, fileName);
    }

    @Override
    public String updateResourceTemplate(final String jvmName, final String resourceTemplateName, final String template) {
        jvmCrudService.updateResourceTemplate(jvmName, resourceTemplateName, template);
        return jvmCrudService.getResourceTemplate(jvmName, resourceTemplateName);
    }

    @Override
    public Jvm findJvm(final String jvmName, final String groupName) {
        return jvmCrudService.findJvm(jvmName, groupName);
    }

    @Override
    public Jvm findJvmByExactName(String jvmName) {
        return jvmCrudService.findJvmByExactName(jvmName);
    }

    @Override
    public void updateState(final Identifier<Jvm> id, final JvmState state) {
        jvmCrudService.updateState(id, state);
    }

    @Override
    public void updateErrorStatus(final Identifier<Jvm> id, final String errorStatus) {
        jvmCrudService.updateErrorStatus(id, errorStatus);
    }

    @Override
    @Transactional
    public void updateState(final Identifier<Jvm> id, final JvmState state, final String errorStatus) {
        jvmCrudService.updateState(id, state, errorStatus);
    }

    @Override
    public List<Group> findGroupsByJvm(Identifier<Jvm> id) {
        return groupJvmRelationshipService.findGroupsByJvm(id);
    }

    @Override
    public Long getJvmStartedCount(final String groupName) {
        return jvmCrudService.getJvmStartedCount(groupName);
    }

    @Override
    public Long getJvmCount(final String groupName) {
        return jvmCrudService.getJvmCount(groupName);
    }

    @Override
    public Long getJvmStoppedCount(final String groupName) {
        return jvmCrudService.getJvmStoppedCount(groupName);
    }

    @Override
    public Long getJvmForciblyStoppedCount(final String groupName) {
        return jvmCrudService.getJvmForciblyStoppedCount(groupName);
    }

    protected Jvm jvmFrom(final JpaJvm aJpaJvm) {
        return new JvmBuilder(aJpaJvm).build();
    }

    protected List<Jvm> jvmsFrom(final List<JpaJvm> someJpaJvms) {
        final List<Jvm> jvms = new ArrayList<>();
        for (final JpaJvm jpaJvm : someJpaJvms) {
            jvms.add(jvmFrom(jpaJvm));
        }
        return jvms;
    }

    @Override
    public int removeTemplate(final String name) {
        return jvmCrudService.removeTemplate(name);
    }

    @Override
    public int removeTemplate(final String jvmName, final String templateName) {
        return jvmCrudService.removeTemplate(jvmName, templateName);
    }

    @Override
    public List<JpaJvmConfigTemplate> getConfigTemplates(final String jvmName) {
        return jvmCrudService.getConfigTemplates(jvmName);
    }

    @Override
    public List<JpaJvm> getJpaJvmsByGroupName(final String groupName) {
        return jvmCrudService.getJvmsByGroupId(groupName);
    }

    @Override
    public List<Jvm> getJvmsByGroupName(final String groupName) {
        return jvmCrudService.getJvmsByGroupName(groupName);
    }

    @Override
    public List<Jvm> getJvmsAndWebAppsByGroupName(final String groupName) {
        final List<Jvm> jvms = jvmCrudService.getJvmsByGroupName(groupName);
        final List<Jvm> jvmsWithWebApps = new ArrayList<>();
        final com.siemens.cto.aem.common.domain.model.jvm.JvmBuilder jvmBuilder = new com.siemens.cto.aem.common.domain.model.jvm.JvmBuilder();
        for (Jvm jvm : jvms) {
            final List<Application> webApps = applicationCrudService.findApplicationsBelongingToJvm(jvm.getId());
            // TODO: Decide whether to use a builder or have a setter just to set the applications ?
            final Jvm jvmWithWebApps = jvmBuilder.setId(jvm.getId())
                                                 .setName(jvm.getJvmName())
                                                 .setHostName(jvm.getHostName())
                                                 .setStatusPath(jvm.getStatusPath())
                                                 .setGroups(jvm.getGroups())
                                                 .setHttpPort(jvm.getHttpPort())
                                                 .setHttpsPort(jvm.getHttpsPort())
                                                 .setRedirectPort(jvm.getRedirectPort())
                                                 .setShutdownPort(jvm.getShutdownPort())
                                                 .setAjpPort(jvm.getAjpPort())
                                                 .setSystemProperties(jvm.getSystemProperties())
                                                 .setState(jvm.getState())
                                                 .setErrorStatus(jvm.getErrorStatus())
                                                 .setWebApps(webApps).build();
            jvmsWithWebApps.add(jvmWithWebApps);
        }
        return jvmsWithWebApps;
    }
}
