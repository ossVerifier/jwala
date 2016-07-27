package com.siemens.cto.aem.persistence.service.impl;

import com.siemens.cto.aem.common.domain.model.resource.EntityType;
import com.siemens.cto.aem.common.domain.model.resource.ResourceIdentifier;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.JpaGroupAppConfigTemplate;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.JpaResourceConfigTemplate;
import com.siemens.cto.aem.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.siemens.cto.aem.persistence.service.ResourcePersistenceService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

public class JpaResourcePersistenceServiceImpl implements ResourcePersistenceService {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;

    @Override
    // NOTE: We're going to use the entity manager here since we are phasing out the CRUD layer soon.
    public List<String> getApplicationResourceNames(final String groupName, final String appName) {
        final Query q = entityManager.createNamedQuery(JpaGroupAppConfigTemplate.QUERY_APP_RESOURCE_NAMES);
        q.setParameter("grpName", groupName);
        q.setParameter("appName", appName);
        return q.getResultList();
    }


    @Override
    public String getAppTemplate(final String groupName, final String appName, final String templateName) {
        final Query q = entityManager.createNamedQuery(JpaGroupAppConfigTemplate.GET_GROUP_APP_TEMPLATE_CONTENT);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_GRP_NAME, groupName);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_APP_NAME, appName);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, templateName);
        return (String) q.getSingleResult();
    }

    @Override
    // TODO map return type to non-JPA POJO?
    public JpaResourceConfigTemplate createResource(Long entityId, Long groupId, Long appId, EntityType extProperties, String fileName, InputStream fileInputStream) {
        Scanner scanner = new Scanner(fileInputStream).useDelimiter("\\A");
        String templateContent = scanner.hasNext() ? scanner.next() : "";

        JpaResourceConfigTemplate resourceTemplate = new JpaResourceConfigTemplate();
        resourceTemplate.setEntityId(entityId);
        resourceTemplate.setGrpId(groupId);
        resourceTemplate.setAppId(appId);
        resourceTemplate.setTemplateContent(templateContent);
        resourceTemplate.setTemplateName(fileName);
        resourceTemplate.setEntityType(extProperties);
        // TODO add meta data
        resourceTemplate.setMetaData("{}");

        entityManager.persist(resourceTemplate);
        entityManager.flush();

        return resourceTemplate;
    }

    @Override
    public void updateResource(ResourceIdentifier resourceIdentifier, EntityType entityType, String templateContent) {
        final Query q = entityManager.createNamedQuery(JpaResourceConfigTemplate.UPDATE_RESOURCE_TEMPLATE_CONTENT);
        q.setParameter(JpaResourceConfigTemplate.QUERY_PARAM_ENTITY_TYPE, entityType);
        // TODO make this more generic and actually use the resource identifier
        q.setParameter(JpaResourceConfigTemplate.QUERY_PARAM_GRP_ID, null);
        q.setParameter(JpaResourceConfigTemplate.QUERY_PARAM_APP_ID, null);
        q.setParameter(JpaResourceConfigTemplate.QUERY_PARAM_ENTITY_ID, null);
        q.setParameter(JpaResourceConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, resourceIdentifier.resourceName);
        q.setParameter(JpaResourceConfigTemplate.QUERY_PARAM_TEMPLATE_CONTENT, templateContent);

        int numEntities;

        try {
            numEntities = q.executeUpdate();
        } catch (RuntimeException re) {
            throw new ResourceTemplateUpdateException(resourceIdentifier.toString(), resourceIdentifier.resourceName, re);
        }

        if (numEntities == 0) {
            throw new ResourceTemplateUpdateException(resourceIdentifier.toString(), resourceIdentifier.resourceName);
        }
    }
}
