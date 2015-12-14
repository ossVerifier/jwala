package com.siemens.cto.aem.persistence.jpa.service.groupjvm.impl;

import com.siemens.cto.aem.common.request.group.AddJvmToGroupRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.common.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.request.group.RemoveJvmFromGroupRequest;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.service.group.GroupCrudService;
import com.siemens.cto.aem.persistence.jpa.service.groupjvm.GroupJvmRelationshipService;
import com.siemens.cto.aem.persistence.jpa.service.jvm.JvmCrudService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GroupJvmRelationshipServiceImpl implements GroupJvmRelationshipService {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;

    private final GroupCrudService groupCrudService;
    private final JvmCrudService jvmCrudService;

    public GroupJvmRelationshipServiceImpl(final GroupCrudService theGroupCrudService,
                                           final JvmCrudService theJvmCrudService) {
        groupCrudService = theGroupCrudService;
        jvmCrudService = theJvmCrudService;
    }

    @Override
    public void addJvmToGroup(final Event<AddJvmToGroupRequest> anEvent) {

        final JpaGroup group = groupCrudService.getGroup(anEvent.getRequest().getGroupId());
        final JpaJvm jvm = jvmCrudService.getJvm(anEvent.getRequest().getJvmId());

        final List<JpaJvm> jvms;
        if (group.getJvms() != null) {
            jvms = group.getJvms();
        } else {
            jvms = new ArrayList<>();
            group.setJvms(jvms);
        }
        jvms.add(jvm);

        final List<JpaGroup> groups;
        if (jvm.getGroups() != null) {
            groups = jvm.getGroups();
        } else {
            groups = new ArrayList<>();
            jvm.setGroups(groups);
        }
        groups.add(group);

        entityManager.flush();
    }

    @Override
    public void removeJvmFromGroup(final Event<RemoveJvmFromGroupRequest> anEvent) {

        final JpaGroup group = groupCrudService.getGroup(anEvent.getRequest().getGroupId());
        final JpaJvm jvm = jvmCrudService.getJvm(anEvent.getRequest().getJvmId());

        if (group.getJvms() != null) {
            final List<JpaJvm> jvms = group.getJvms();
            jvms.remove(jvm);
            if (jvm.getGroups() != null) {
                jvm.getGroups().remove(group);
            }
        }

        entityManager.flush();
    }

    @Override
    public void removeRelationshipsForGroup(final Identifier<Group> aGroupId) {

        final JpaGroup group = groupCrudService.getGroup(aGroupId);

        if (group.getJvms() != null) {
            final Iterator<JpaJvm> jvms = group.getJvms().iterator();
            while (jvms.hasNext()) {
                final JpaJvm jvm = jvms.next();
                if (jvm.getGroups() != null) {
                    jvm.getGroups().remove(group);
                }
                jvms.remove();
            }
        }

        entityManager.flush();
    }

    @Override
    public void removeRelationshipsForJvm(final Identifier<Jvm> aJvmId) {

        final JpaJvm jvm = jvmCrudService.getJvm(aJvmId);

        if (jvm.getGroups() != null) {
            final Iterator<JpaGroup> groups = jvm.getGroups().iterator();
            while (groups.hasNext()) {
                final JpaGroup group = groups.next();
                if (group.getJvms() != null) {
                    group.getJvms().remove(jvm);
                }
                groups.remove();
            }
        }

        entityManager.flush();
    }

    @Override
    public void populateJvmConfig(List<UploadJvmTemplateRequest> uploadJvmTemplateCommands, User user, boolean overwriteExisting) {
        for (UploadJvmTemplateRequest command: uploadJvmTemplateCommands) {
            if (overwriteExisting || jvmCrudService.getJvmTemplate(command.getConfFileName(), command.getJvm().getId()).isEmpty()){
                jvmCrudService.uploadJvmTemplateXml(new Event<>(command, AuditEvent.now(user)));
            }
        }
    }
}
