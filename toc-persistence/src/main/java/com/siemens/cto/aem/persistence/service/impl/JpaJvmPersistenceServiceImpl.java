package com.siemens.cto.aem.persistence.service.impl;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.request.jvm.CreateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UpdateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvmConfigTemplate;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaJvmBuilder;
import com.siemens.cto.aem.persistence.jpa.service.GroupJvmRelationshipService;
import com.siemens.cto.aem.persistence.jpa.service.JvmCrudService;
import com.siemens.cto.aem.persistence.service.JvmPersistenceService;

import java.util.ArrayList;
import java.util.List;

public class JpaJvmPersistenceServiceImpl implements JvmPersistenceService {

    private final JvmCrudService jvmCrudService;
    private final GroupJvmRelationshipService groupJvmRelationshipService;

    public JpaJvmPersistenceServiceImpl(final JvmCrudService theJvmCrudService,
                                        final GroupJvmRelationshipService theGroupJvmRelationshipService) {
        jvmCrudService = theJvmCrudService;
        groupJvmRelationshipService = theGroupJvmRelationshipService;
    }

    @Override
    public Jvm createJvm(final Event<CreateJvmRequest> aJvmToCreate) {
        final JpaJvm jpaJvm = jvmCrudService.createJvm(aJvmToCreate);
        return jvmFrom(jpaJvm);
    }

    @Override
    public Jvm updateJvm(final Event<UpdateJvmRequest> aJvmToUpdate) {
        final JpaJvm jpaJvm = jvmCrudService.updateJvm(aJvmToUpdate);
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
    public JpaJvmConfigTemplate uploadJvmTemplateXml(Event<UploadJvmTemplateRequest> event) {
        final JpaJvmConfigTemplate jpaJvmConfigTemplate = jvmCrudService.uploadJvmTemplateXml(event);
        return jpaJvmConfigTemplate;
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
    public String updateResourceTemplate(final String jvmName, final String resourceTemplateName, final String template) {
        jvmCrudService.updateResourceTemplate(jvmName, resourceTemplateName, template);
        return jvmCrudService.getResourceTemplate(jvmName, resourceTemplateName);
    }

    @Override
    public Jvm findJvm(final String jvmName, final String groupName) {
        return jvmCrudService.findJvm(jvmName, groupName);
    }

    protected Jvm jvmFrom(final JpaJvm aJpaJvm) {
        return new JpaJvmBuilder(aJpaJvm).build();
    }

    protected List<Jvm> jvmsFrom(final List<JpaJvm> someJpaJvms) {
        final List<Jvm> jvms = new ArrayList<>();
        for (final JpaJvm jpaJvm : someJpaJvms) {
            jvms.add(jvmFrom(jpaJvm));
        }
        return jvms;
    }

}
