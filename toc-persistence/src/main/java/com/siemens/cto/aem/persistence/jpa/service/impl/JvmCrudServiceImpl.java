package com.siemens.cto.aem.persistence.jpa.service.impl;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.request.jvm.CreateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UpdateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.JpaJvmConfigTemplate;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JvmBuilder;
import com.siemens.cto.aem.persistence.jpa.service.JvmCrudService;
import com.siemens.cto.aem.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.siemens.cto.aem.persistence.jpa.service.exception.ResourceTemplateUpdateException;

import javax.persistence.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class JvmCrudServiceImpl extends AbstractCrudServiceImpl<JpaJvm> implements JvmCrudService {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;

    @Override
    public JpaJvm createJvm(CreateJvmRequest createJvmRequest) {

        try {
            final JpaJvm jpaJvm = new JpaJvm();

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
                    "JVM with name already exists: " + createJvmRequest.getJvmName(),
                    eee);
        }
    }

    @Override
    public JpaJvm updateJvm(UpdateJvmRequest updateJvmRequest) {

        try {
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
                    "JVM with name already exists: " + updateJvmRequest.getNewJvmName(),
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
        query.setParameter("jvmName", "%" + aName + "%");
        return query.getResultList();
    }

    @Override
    public void removeJvm(final Identifier<Jvm> aJvmId) {
        remove(getJvm(aJvmId));
    }

    @Override
    public JpaJvmConfigTemplate uploadJvmTemplateXml(UploadJvmTemplateRequest uploadJvmTemplateRequest) {

        final Jvm jvm = uploadJvmTemplateRequest.getJvm();
        Identifier<Jvm> id = jvm.getId();
        final JpaJvm jpaJvm = getJvm(id);

        InputStream inStream = uploadJvmTemplateRequest.getData();
        Scanner scanner = new Scanner(inStream).useDelimiter("\\A");
        String templateContent = scanner.hasNext() ? scanner.next() : "";

        // get an instance and then do a create or update
        Query query = entityManager.createQuery("SELECT t FROM JpaJvmConfigTemplate t where t.templateName = :tempName and t.jvm = :jpaJvm");
        query.setParameter("jpaJvm", jpaJvm);
        query.setParameter("tempName", uploadJvmTemplateRequest.getConfFileName());
        List<JpaJvmConfigTemplate> templates = query.getResultList();
        JpaJvmConfigTemplate jpaConfigTemplate;
        final String metaData = uploadJvmTemplateRequest.getMetaData();
        if (templates.size() == 1) {
            //update
            jpaConfigTemplate = templates.get(0);
            jpaConfigTemplate.setTemplateContent(templateContent);
            jpaConfigTemplate.setMetaData(metaData);
            entityManager.flush();
        } else if (templates.isEmpty()) {
            //create
            jpaConfigTemplate = new JpaJvmConfigTemplate();
            jpaConfigTemplate.setJvm(jpaJvm);
            jpaConfigTemplate.setTemplateName(uploadJvmTemplateRequest.getConfFileName());
            jpaConfigTemplate.setTemplateContent(templateContent);
            jpaConfigTemplate.setMetaData(metaData);
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
    public String getResourceTemplateMetaData(String jvmName, String fileName) {
        final Query q = entityManager.createNamedQuery(JpaJvmConfigTemplate.GET_JVM_TEMPLATE_META_DATA);
        q.setParameter("jvmName", jvmName);
        q.setParameter("templateName", fileName);
        try {
            return (String) q.getSingleResult();
        } catch (RuntimeException re) {
            throw new NonRetrievableResourceTemplateContentException(jvmName, fileName, re);
        }
    }

    @Override
    public void updateResourceTemplate(final String jvmName, final String resourceTemplateName, final String template) {
        final Query q = entityManager.createNamedQuery(JpaJvmConfigTemplate.UPDATE_JVM_TEMPLATE_CONTENT);
        q.setParameter("jvmName", jvmName);
        q.setParameter("templateName", resourceTemplateName);
        q.setParameter("templateContent", template);

        int numEntities;

        try {
            numEntities = q.executeUpdate();
        } catch (RuntimeException re) {
            throw new ResourceTemplateUpdateException(jvmName, resourceTemplateName, re);
        }

        if (numEntities == 0) {
            throw new ResourceTemplateUpdateException(jvmName, resourceTemplateName);
        }
    }

    /**
     *
     * @param jvmName
     * @param groupName
     * @return
     */
    @Override
    public Jvm findJvm(final String jvmName, final String groupName) {
        final Query q = entityManager.createNamedQuery(JpaJvm.QUERY_FIND_JVM_BY_GROUP_AND_JVM_NAME);
        q.setParameter("jvmName", jvmName);
        q.setParameter("groupName", groupName);
        JpaJvm jpaJvm = (JpaJvm) q.getSingleResult();
        return (new JvmBuilder(jpaJvm)).build();
    }

    @Override
    public Jvm findJvmByExactName(String jvmName) {
        final Query query = entityManager.createQuery("SELECT j FROM JpaJvm j WHERE j.name=:jvmName ORDER BY j.name");
        query.setParameter("jvmName", jvmName);
        return (new JvmBuilder((JpaJvm) query.getSingleResult())).build();
    }

    @Override
    public Long getJvmStartedCount(final String groupName) {
        final Query query = entityManager.createNamedQuery(JpaJvm.QUERY_GET_JVM_COUNT_BY_STATE_AND_GROUP_NAME);
        query.setParameter(JpaJvm.QUERY_PARAM_STATE, JvmState.JVM_STARTED);
        query.setParameter(JpaJvm.QUERY_PARAM_GROUP_NAME, groupName);
        return (Long) query.getSingleResult();
    }

    @Override
    public Long getJvmStoppedCount(final String groupName) {
        final Query query = entityManager.createNamedQuery(JpaJvm.QUERY_GET_JVM_COUNT_BY_STATE_AND_GROUP_NAME);
        query.setParameter(JpaJvm.QUERY_PARAM_STATE, JvmState.JVM_STOPPED);
        query.setParameter(JpaJvm.QUERY_PARAM_GROUP_NAME, groupName);
        return (Long) query.getSingleResult();
    }

    @Override
    public Long getJvmCount(final String groupName) {
        final Query query = entityManager.createNamedQuery(JpaJvm.QUERY_GET_JVM_COUNT_BY_GROUP_NAME);
        query.setParameter(JpaJvm.QUERY_PARAM_GROUP_NAME, groupName);
        return (Long) query.getSingleResult();
    }

    @Override
    public int updateState(final Identifier<Jvm> id, final JvmState state) {
        // Normally we would load the JpaJvm then set the states but I reckon running an UPDATE query would be faster since
        // it's only one transaction vs 2 (find and update).
        final Query query = entityManager.createNamedQuery(JpaJvm.QUERY_UPDATE_STATE_BY_ID);
        query.setParameter(JpaJvm.QUERY_PARAM_STATE, state);
        query.setParameter(JpaJvm.QUERY_PARAM_ID, id.getId());
        return query.executeUpdate();
    }

    @Override
    public int updateErrorStatus(final Identifier<Jvm> id, final String errorStatus) {
        final Query query = entityManager.createNamedQuery(JpaJvm.QUERY_UPDATE_ERROR_STATUS_BY_ID);
        query.setParameter(JpaJvm.QUERY_PARAM_ERROR_STATUS, errorStatus);
        query.setParameter(JpaJvm.QUERY_PARAM_ID, id.getId());
        return query.executeUpdate();
    }

    @Override
    public int updateState(final Identifier<Jvm> id, final JvmState state, final String errorStatus) {
        final Query query = entityManager.createNamedQuery(JpaJvm.QUERY_UPDATE_STATE_AND_ERR_STS_BY_ID);
        query.setParameter(JpaJvm.QUERY_PARAM_STATE, state);
        query.setParameter(JpaJvm.QUERY_PARAM_ERROR_STATUS, errorStatus);
        query.setParameter(JpaJvm.QUERY_PARAM_ID, id.getId());
        return query.executeUpdate();
    }

    @Override
    public Long getJvmForciblyStoppedCount(final String groupName) {
        final Query query = entityManager.createNamedQuery(JpaJvm.QUERY_GET_JVM_COUNT_BY_STATE_AND_GROUP_NAME);
        query.setParameter(JpaJvm.QUERY_PARAM_STATE, JvmState.FORCED_STOPPED);
        query.setParameter(JpaJvm.QUERY_PARAM_GROUP_NAME, groupName);
        return (Long) query.getSingleResult();
    }

    @Override
    public int removeTemplate(final String name) {
        final Query q = entityManager.createNamedQuery(JpaJvmConfigTemplate.QUERY_DELETE_JVM_TEMPLATE);
        q.setParameter(JpaJvmConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, name);
        return q.executeUpdate();
    }

    @Override
    public int removeTemplate(final String jvmName, final String templateName) {
        final Query q = entityManager.createNamedQuery(JpaJvmConfigTemplate.QUERY_DELETE_JVM_TEMPLATE_BY_JVM_NAME);
        q.setParameter(JpaJvmConfigTemplate.QUERY_PARAM_JVM_NAME, jvmName);
        q.setParameter(JpaJvmConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, templateName);
        return q.executeUpdate();
    }

    @Override
    public List<JpaJvmConfigTemplate> getConfigTemplates(final String jvmName) {
        final Query q = entityManager.createNamedQuery(JpaJvmConfigTemplate.QUERY_GET_JVM_RESOURCE_TEMPLATES);
        q.setParameter(JpaJvmConfigTemplate.QUERY_PARAM_JVM_NAME, jvmName);
        return q.getResultList();
    }

    @Override
    public List<JpaJvm> getJvmsByGroupId(final String groupName) {
        final Query q = entityManager.createNamedQuery(JpaJvm.QUERY_GET_JVMS_BY_GROUP_NAME);
        q.setParameter(JpaJvm.QUERY_PARAM_GROUP_NAME, groupName);
        return q.getResultList();
    }

    @Override
    public List<Jvm> getJvmsByGroupName(String groupName) {
        final Query q = entityManager.createNamedQuery(JpaJvm.QUERY_GET_JVMS_BY_GROUP_NAME);
        q.setParameter(JpaJvm.QUERY_PARAM_GROUP_NAME, groupName);
        return buildJvms(q.getResultList());
    }

    /**
     * Build the JVM list.
     * @param jpaJvms {@link JpaJvm}
     * @return The JVM list. Returns an empty list if there are no JVMs.
     */
    private List<Jvm> buildJvms(List<JpaJvm> jpaJvms) {
        final List<Jvm> jvms = new ArrayList<>(jpaJvms.size());
        for(final JpaJvm jpaJvm: jpaJvms) {
            jvms.add(new JvmBuilder(jpaJvm).build());
        }
        return jvms;
    }
}
