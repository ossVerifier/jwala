package com.siemens.cto.aem.persistence.jpa.service.impl;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.group.GroupState;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.request.group.CreateGroupRequest;
import com.siemens.cto.aem.common.request.group.UpdateGroupRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroupJvmConfigTemplate;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroupWebServerConfigTemplate;
import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServer;
import com.siemens.cto.aem.persistence.jpa.service.GroupCrudService;
import com.siemens.cto.aem.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.siemens.cto.aem.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import org.joda.time.DateTime;

import javax.persistence.EntityExistsException;
import javax.persistence.Query;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GroupCrudServiceImpl extends AbstractCrudServiceImpl<JpaGroup> implements GroupCrudService {

    public GroupCrudServiceImpl() {
    }

    @Override
    public JpaGroup createGroup(CreateGroupRequest createGroupRequest) {
        final JpaGroup jpaGroup = new JpaGroup();
        jpaGroup.setName(createGroupRequest.getGroupName());

        try {
            return create(jpaGroup);
        } catch (final EntityExistsException eee) {
            throw new BadRequestException(AemFaultType.INVALID_GROUP_NAME,
                    "Group Name already exists: " + createGroupRequest.getGroupName(),
                    eee);
        }
    }

    @Override
    public void updateGroup(UpdateGroupRequest updateGroupRequest) {

        final JpaGroup jpaGroup = getGroup(updateGroupRequest.getId());

        jpaGroup.setName(updateGroupRequest.getNewName());

        try {
            update(jpaGroup);
        } catch (final EntityExistsException eee) {
            throw new BadRequestException(AemFaultType.INVALID_GROUP_NAME,
                    "Group Name already exists: " + updateGroupRequest.getNewName(),
                    eee);
        }
    }

    @Override
    public JpaGroup getGroup(final Identifier<Group> aGroupId) throws NotFoundException {
        return findById(aGroupId.getId());
    }

    @SuppressWarnings("unchecked")
    @Override
    public JpaGroup getGroup(final String name) throws NotFoundException {
        final Query query = entityManager.createQuery("SELECT g FROM JpaGroup g WHERE g.name = :groupName");
        query.setParameter("groupName", name);
        List<JpaGroup> jpaGroups = query.getResultList();
        if (jpaGroups == null || jpaGroups.size() < 1) {
            throw new NotFoundException(AemFaultType.GROUP_NOT_FOUND, "Group not found: " + name);
        } else if (jpaGroups.size() > 1) {
            throw new NotFoundException(AemFaultType.DATA_CONTROL_ERROR, "Too many groups found for " + name + " code is set to only use one");
        }
        return jpaGroups.get(0);
    }

    @Override
    public List<JpaGroup> getGroups() {
        return findAll();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<JpaGroup> findGroups(final String aName) {

        final Query query = entityManager.createQuery("SELECT g FROM JpaGroup g WHERE g.name LIKE :groupName");
        query.setParameter("groupName", "%" + aName + "%");

        return query.getResultList();
    }

    @Override
    public void removeGroup(final Identifier<Group> aGroupId) {
        final JpaGroup group = getGroup(aGroupId);
        remove(group);
    }

    @Override
    public JpaGroup updateGroupStatus(SetStateRequest<Group, GroupState> setStateRequest) {
        final JpaGroup jpaGroup = getGroup(setStateRequest.getNewState().getId());

        jpaGroup.setState(setStateRequest.getNewState().getState());
        jpaGroup.setStateUpdated(DateTime.now().toCalendar(null));

        return update(jpaGroup);
    }

    @Override
    public Long getGroupId(final String name) {
        final Query q = entityManager.createNamedQuery(JpaGroup.QUERY_GET_GROUP_ID);
        q.setParameter("name", name);
        return (Long) q.getSingleResult();
    }

    @Override
    public void linkWebServer(final WebServer webServer) {
        linkWebServer(webServer.getId(), webServer);
    }

    @Override
    public void linkWebServer(final Identifier<WebServer> id, final WebServer webServer) {
        final JpaWebServer jpaWebServer = entityManager.find(JpaWebServer.class, id.getId());
        final List<JpaGroup> jpaGroups = getGroupsWithWebServer(jpaWebServer);

        // Unlink web server from all the groups.
        for (JpaGroup jpaGroup : jpaGroups) {
            jpaGroup.getWebServers().remove(jpaWebServer);
        }

        // Link web server's newly defined groups.
        List<JpaGroup> linkedGroups = new ArrayList<>();
        for (Group group : webServer.getGroups()) {
            final JpaGroup jpaGroup = getGroup(group.getId());
            jpaGroup.getWebServers().add(jpaWebServer);
            linkedGroups.add(jpaGroup);
        }
        jpaWebServer.setGroups(linkedGroups);
        entityManager.persist(jpaWebServer);

        entityManager.flush();
    }

    @SuppressWarnings("unchecked")
    private List<JpaGroup> getGroupsWithWebServer(final JpaWebServer jpaWebServer) {
        final Query q = entityManager.createNamedQuery(JpaGroup.QUERY_GET_GROUPS_WITH_WEBSERVER);
        q.setParameter("webServer", jpaWebServer);
        return q.getResultList();
    }

    @Override
    public void uploadGroupJvmTemplate(UploadJvmTemplateRequest uploadJvmTemplateRequest, JpaGroup group) {
        InputStream inStream = uploadJvmTemplateRequest.getData();
        Scanner scanner = new Scanner(inStream).useDelimiter("\\A");
        String templateContent = scanner.hasNext() ? scanner.next() : "";

        Query query = entityManager.createQuery("SELECT t FROM JpaGroupJvmConfigTemplate t where t.templateName = :tempName and t.grp.name = :grpName");
        query.setParameter("grpName", group.getName());
        query.setParameter("tempName", uploadJvmTemplateRequest.getConfFileName());
        List<JpaGroupJvmConfigTemplate> templates = query.getResultList();
        JpaGroupJvmConfigTemplate jpaConfigTemplate;
        if (templates.size() == 1) {
            //update
            jpaConfigTemplate = templates.get(0);
            jpaConfigTemplate.setTemplateContent(templateContent);
            entityManager.flush();
        } else if (templates.isEmpty()) {
            jpaConfigTemplate = new JpaGroupJvmConfigTemplate();
            jpaConfigTemplate.setJpaGroup(group);
            jpaConfigTemplate.setTemplateName(uploadJvmTemplateRequest.getConfFileName());
            jpaConfigTemplate.setTemplateContent(templateContent);
            entityManager.persist(jpaConfigTemplate);
            entityManager.flush();
        } else {
            throw new BadRequestException(AemFaultType.JVM_TEMPLATE_NOT_FOUND,
                    "Only expecting one template to be returned for GROUP JVM Template [" + group.getName() + "] but returned " + templates.size() + " templates");
        }
    }

    @Override
    public void uploadGroupWebServerTemplate(UploadWebServerTemplateRequest uploadWSTemplateRequest, JpaGroup group) {
        InputStream inStream = uploadWSTemplateRequest.getData();
        Scanner scanner = new Scanner(inStream).useDelimiter("\\A");
        String templateContent = scanner.hasNext() ? scanner.next() : "";

        Query query = entityManager.createQuery("SELECT t FROM JpaGroupWebServerConfigTemplate t where t.templateName = :tempName and t.grp.name = :grpName");
        query.setParameter("grpName", group.getName());
        query.setParameter("tempName", uploadWSTemplateRequest.getConfFileName());
        List<JpaGroupWebServerConfigTemplate> templates = query.getResultList();
        JpaGroupWebServerConfigTemplate jpaConfigTemplate;
        if (templates.size() == 1) {
            //update
            jpaConfigTemplate = templates.get(0);
            jpaConfigTemplate.setTemplateContent(templateContent);
            entityManager.flush();
        } else if (templates.isEmpty()) {
            jpaConfigTemplate = new JpaGroupWebServerConfigTemplate();
            jpaConfigTemplate.setJpaGroup(group);
            jpaConfigTemplate.setTemplateName(uploadWSTemplateRequest.getConfFileName());
            jpaConfigTemplate.setTemplateContent(templateContent);
            entityManager.persist(jpaConfigTemplate);
            entityManager.flush();
        } else {
            throw new BadRequestException(AemFaultType.WEB_SERVER_HTTPD_CONF_TEMPLATE_NOT_FOUND,
                    "Only expecting one template to be returned for GROUP Web Server Template [" + group.getName() + "] but returned " + templates.size() + " templates");
        }
    }

    @Override
    public List<String> getGroupJvmsResourceTemplateNames(final String groupName) {
        final Query query = entityManager.createNamedQuery(JpaGroupJvmConfigTemplate.GET_GROUP_JVM_TEMPLATE_RESOURCE_NAMES);
        query.setParameter("grpName", groupName);
        return query.getResultList();
    }

    @Override
    public List<String> getGroupWebServersResourceTemplateNames(final String groupName) {
        final Query query = entityManager.createNamedQuery(JpaGroupWebServerConfigTemplate.GET_GROUP_WEBSERVER_TEMPLATE_RESOURCE_NAMES);
        query.setParameter("grpName", groupName);
        return query.getResultList();
    }

    @Override
    public void updateGroupJvmResourceTemplate(String groupName, String resourceTemplateName, String content) {
        final Query q = entityManager.createNamedQuery(JpaGroupJvmConfigTemplate.UPDATE_GROUP_JVM_TEMPLATE_CONTENT);
        q.setParameter("grpName", groupName);
        q.setParameter("templateName", resourceTemplateName);
        q.setParameter("templateContent", content);

        int numEntities = 0;

        try {
            numEntities = q.executeUpdate();
        } catch (RuntimeException re) {
            throw new ResourceTemplateUpdateException(groupName, resourceTemplateName, re);
        }

        if (numEntities == 0) {
            throw new ResourceTemplateUpdateException(groupName, resourceTemplateName);
        }
    }

    @Override
    public String getGroupJvmResourceTemplate(String groupName, String resourceTemplateName) {
        final Query q = entityManager.createNamedQuery(JpaGroupJvmConfigTemplate.GET_GROUP_JVM_TEMPLATE_CONTENT);
        q.setParameter("grpName", groupName);
        q.setParameter("templateName", resourceTemplateName);
        try {
            return (String) q.getSingleResult();
        } catch (RuntimeException re) {
            throw new NonRetrievableResourceTemplateContentException(groupName, resourceTemplateName, re);
        }
    }

    @Override
    public void updateGroupWebServerResourceTemplate(String groupName, String resourceTemplateName, String content) {
        final Query q = entityManager.createNamedQuery(JpaGroupWebServerConfigTemplate.UPDATE_GROUP_WEBSERVER_TEMPLATE_CONTENT);
        q.setParameter("grpName", groupName);
        q.setParameter("templateName", resourceTemplateName);
        q.setParameter("templateContent", content);

        int numEntities = 0;

        try {
            numEntities = q.executeUpdate();
        } catch (RuntimeException re) {
            throw new ResourceTemplateUpdateException(groupName, resourceTemplateName, re);
        }

        if (numEntities == 0) {
            throw new ResourceTemplateUpdateException(groupName, resourceTemplateName);
        }
    }

    @Override
    public String getGroupWebServerResourceTemplate(String groupName, String resourceTemplateName) {
        final Query q = entityManager.createNamedQuery(JpaGroupWebServerConfigTemplate.GET_GROUP_WEBSERVER_TEMPLATE_CONTENT);
        q.setParameter("grpName", groupName);
        q.setParameter("templateName", resourceTemplateName);
        try {
            return (String) q.getSingleResult();
        } catch (RuntimeException re) {
            throw new NonRetrievableResourceTemplateContentException(groupName, resourceTemplateName, re);
        }
    }
}

