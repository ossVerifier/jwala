package com.siemens.cto.aem.persistence.jpa.service.impl;

import com.siemens.cto.aem.common.domain.model.group.History;
import com.siemens.cto.aem.common.request.jvm.CreateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.request.jvm.UpdateJvmRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaHistory;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvmConfigTemplate;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaJvmBuilder;
import com.siemens.cto.aem.persistence.jpa.service.HistoryCrudService;
import com.siemens.cto.aem.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.siemens.cto.aem.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.siemens.cto.aem.persistence.jpa.service.JvmCrudService;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

public class JvmCrudServiceImpl extends AbstractCrudServiceImpl<JpaJvm, Jvm> implements JvmCrudService {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;

    @Override
    public JpaJvm createJvm(final Event<CreateJvmRequest> aJvmToCreate) {

        try {
            final JpaJvm jpaJvm = new JpaJvm();
            final CreateJvmRequest createJvmRequest = aJvmToCreate.getRequest();

            jpaJvm.setName(createJvmRequest.getJvmName());
            jpaJvm.setHostName(createJvmRequest.getHostName());
            jpaJvm.setHttpPort(createJvmRequest.getHttpPort());
            jpaJvm.setHttpsPort(createJvmRequest.getHttpsPort());
            jpaJvm.setRedirectPort(createJvmRequest.getRedirectPort());
            jpaJvm.setShutdownPort(createJvmRequest.getShutdownPort());
            jpaJvm.setAjpPort(createJvmRequest.getAjpPort());
            jpaJvm.setStatusPath(createJvmRequest.getStatusPath().getPath());
            jpaJvm.setSystemProperties(createJvmRequest.getSystemProperties());

            return create(jpaJvm);
        } catch (final EntityExistsException eee) {
            throw new BadRequestException(AemFaultType.INVALID_JVM_NAME,
                    "JVM with name already exists: " + aJvmToCreate.getRequest().getJvmName(),
                    eee);
        }
    }

    @Override
    public JpaJvm updateJvm(final Event<UpdateJvmRequest> aJvmToUpdate) {

        try {
            final UpdateJvmRequest updateJvmRequest = aJvmToUpdate.getRequest();
            final Identifier<Jvm> jvmId = updateJvmRequest.getId();
            final JpaJvm jpaJvm = getJvm(jvmId);

            jpaJvm.setName(updateJvmRequest.getNewJvmName());
            jpaJvm.setHostName(updateJvmRequest.getNewHostName());
            jpaJvm.setHttpPort(updateJvmRequest.getNewHttpPort());
            jpaJvm.setHttpsPort(updateJvmRequest.getNewHttpsPort());
            jpaJvm.setRedirectPort(updateJvmRequest.getNewRedirectPort());
            jpaJvm.setShutdownPort(updateJvmRequest.getNewShutdownPort());
            jpaJvm.setAjpPort(updateJvmRequest.getNewAjpPort());
            jpaJvm.setStatusPath(updateJvmRequest.getNewStatusPath().getPath());
            jpaJvm.setSystemProperties(updateJvmRequest.getNewSystemProperties());

            return update(jpaJvm);
        } catch (final EntityExistsException eee) {
            throw new BadRequestException(AemFaultType.INVALID_JVM_NAME,
                    "JVM with name already exists: " + aJvmToUpdate.getRequest().getNewJvmName(),
                    eee);
        }
    }

    @Override
    public JpaJvm getJvm(final Identifier<Jvm> aJvmId) throws NotFoundException {
        final JpaJvm jvm = findById(aJvmId.getId());

        if (jvm == null) {
            throw new NotFoundException(AemFaultType.JVM_NOT_FOUND,
                    "Jvm not found: " + aJvmId);
        }

        return jvm;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<JpaJvm> getJvms() {
        return findAll();
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
        remove(getJvm(aJvmId));
    }

    @Override
    public JpaJvmConfigTemplate uploadJvmTemplateXml(Event<UploadJvmTemplateRequest> event) {

        final UploadJvmTemplateRequest command = event.getRequest();
        final Jvm jvm = command.getJvm();
        Identifier<Jvm> id = jvm.getId();
        final JpaJvm jpaJvm = getJvm(id);

        InputStream inStream = command.getData();
        Scanner scanner = new Scanner(inStream).useDelimiter("\\A");
        String templateContent = scanner.hasNext() ? scanner.next() : "";

        // get an instance and then do a create or update
        Query query = entityManager.createQuery("SELECT t FROM JpaJvmConfigTemplate t where t.templateName = :tempName and t.jvm = :jpaJvm");
        query.setParameter("jpaJvm", jpaJvm);
        query.setParameter("tempName", command.getConfFileName());
        List<JpaJvmConfigTemplate> templates = query.getResultList();
        JpaJvmConfigTemplate jpaConfigTemplate;
        if (templates.size() == 1) {
            //update
            jpaConfigTemplate = templates.get(0);
            jpaConfigTemplate.setTemplateContent(templateContent);
            entityManager.flush();
        } else if (templates.isEmpty()) {
            //create
            jpaConfigTemplate = new JpaJvmConfigTemplate();
            jpaConfigTemplate.setJvm(jpaJvm);
            jpaConfigTemplate.setTemplateName(command.getConfFileName());
            jpaConfigTemplate.setTemplateContent(templateContent);
            entityManager.persist(jpaConfigTemplate);
            entityManager.flush();
        } else {
            throw new BadRequestException(AemFaultType.JVM_TEMPLATE_NOT_FOUND,
                    "Only expecting one template to be returned for JVM [" + jvm.getJvmName() + "] but returned " + templates.size() + " templates");
        }

        return jpaConfigTemplate;

    }

    @Override
    public String getJvmTemplate(String templateName, Identifier<Jvm> jvmId) {
        JpaJvm jpaJvm = getJvm(jvmId);
        Query query = entityManager.createQuery("SELECT t FROM JpaJvmConfigTemplate t where t.templateName = :tempName and t.jvm = :jpaJvm");
        query.setParameter("jpaJvm", jpaJvm);
        query.setParameter("tempName", templateName);
        List<JpaJvmConfigTemplate> templates = query.getResultList();
        if (templates.size() == 1) {
            return templates.get(0).getTemplateContent();
        } else if (templates.isEmpty()) {
            return "";
        } else {
            throw new BadRequestException(AemFaultType.JVM_TEMPLATE_NOT_FOUND,
                    "Only expecting one " + templateName + " template to be returned for JVM [" + jpaJvm.getName() + "] but returned " + templates.size() + " templates");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<JpaJvm> findJvmsBelongingTo(final Identifier<Group> aGroup) {
        final Query query = entityManager.createQuery("SELECT j FROM JpaGroup g join g.jvms j WHERE g.id = :groupId ORDER BY j.name");

        query.setParameter("groupId", aGroup.getId());

        return query.getResultList();
    }

    @Override
    public List<String> getResourceTemplateNames(String jvmName) {
        final Query q = entityManager.createNamedQuery(JpaJvmConfigTemplate.GET_JVM_RESOURCE_TEMPLATE_NAMES);
        q.setParameter("jvmName", jvmName);
        return q.getResultList();
    }

    @Override
    public String getResourceTemplate(final String jvmName, final String resourceTemplateName) {
        final Query q = entityManager.createNamedQuery(JpaJvmConfigTemplate.GET_JVM_TEMPLATE_CONTENT);
        q.setParameter("jvmName", jvmName);
        q.setParameter("templateName", resourceTemplateName);
        try {
            return (String) q.getSingleResult();
        } catch (RuntimeException re) {
            throw new NonRetrievableResourceTemplateContentException(jvmName, resourceTemplateName, re);
        }
    }

    @Override
    public void updateResourceTemplate(final String jvmName, final String resourceTemplateName, final String template) {
        final Query q = entityManager.createNamedQuery(JpaJvmConfigTemplate.UPDATE_JVM_TEMPLATE_CONTENT);
        q.setParameter("jvmName", jvmName);
        q.setParameter("templateName", resourceTemplateName);
        q.setParameter("templateContent", template);

        int numEntities = 0;

        try {
            numEntities = q.executeUpdate();
        } catch (RuntimeException re) {
            throw new ResourceTemplateUpdateException(jvmName, resourceTemplateName, re);
        }

        if (numEntities == 0) {
            throw new ResourceTemplateUpdateException(jvmName, resourceTemplateName);
        }
    }

    @Override
    public Jvm findJvm(final String jvmName, final String groupName) {
        final Query q = entityManager.createNamedQuery(JpaJvm.QUERY_FIND_JVM_BY_GROUP_AND_JVM_NAME);
        q.setParameter("jvmName", jvmName);
        q.setParameter("groupName", groupName);
        JpaJvm jpaJvm = (JpaJvm) q.getSingleResult();
        return (new JpaJvmBuilder(jpaJvm)).build();
    }

}
