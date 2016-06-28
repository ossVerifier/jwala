package com.siemens.cto.aem.persistence.service.impl;

import com.siemens.cto.aem.persistence.jpa.domain.JpaApplicationConfigTemplate;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.*;
import com.siemens.cto.aem.persistence.service.ResourceDao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

/**
 * Implements {@link ResourceDao}
 *
 * Created by JC043760 on 6/3/2016.
 */
public class ResourceDaoImpl implements ResourceDao {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager em;

    @Override
    public int deleteWebServerResource(final String templateName, final String webServerName) {
        final Query q = em.createNamedQuery(JpaWebServerConfigTemplate.QUERY_DELETE_WEBSERVER_RESOURCE_BY_TEMPLATE_WEBSERVER_NAME);
        q.setParameter(JpaWebServerConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, templateName);
        q.setParameter(JpaWebServerConfigTemplate.QUERY_PARAM_WEBSERVER_NAME, webServerName);
        return q.executeUpdate();
    }

    @Override
    public int deleteGroupLevelWebServerResource(final String templateName, final String groupName) {
        final Query q = em.createNamedQuery(JpaGroupWebServerConfigTemplate.QUERY_DELETE_GROUP_LEVEL_WEBSERVER_RESOURCE_BY_TEMPLATE_GROUP_NAME);
        q.setParameter(JpaGroupWebServerConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, templateName);
        q.setParameter(JpaGroupWebServerConfigTemplate.QUERY_PARAM_GROUP_NAME, groupName);
        return q.executeUpdate();
    }

    @Override
    public int deleteJvmResource(final String templateName, final String jvmName) {
        final Query q = em.createNamedQuery(JpaJvmConfigTemplate.QUERY_DELETE_JVM_RESOURCE_BY_TEMPLATE_JVM_NAME);
        q.setParameter(JpaJvmConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, templateName);
        q.setParameter(JpaJvmConfigTemplate.QUERY_PARAM_JVM_NAME, jvmName);
        return q.executeUpdate();
    }

    @Override
    public int deleteGroupLevelJvmResource(final String templateName, final String groupName) {
        final Query q = em.createNamedQuery(JpaGroupJvmConfigTemplate.QUERY_DELETE_GROUP_LEVEL_JVM_RESOURCE_BY_TEMPLATE_GROUP_NAME);
        q.setParameter(JpaGroupJvmConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, templateName);
        q.setParameter(JpaGroupJvmConfigTemplate.QUERY_PARAM_GROUP_NAME, groupName);
        return q.executeUpdate();
    }

    @Override
    public int deleteAppResource(final String templateName, final String appName, final String jvmName) {
        final Query q = em.createNamedQuery(JpaApplicationConfigTemplate.QUERY_DELETE_APP_RESOURCE_BY_TEMPLATE_APP_JVM_NAME);
        q.setParameter(JpaApplicationConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, templateName);
        q.setParameter(JpaApplicationConfigTemplate.QUERY_PARAM_JVM_NAME, jvmName);
        q.setParameter(JpaApplicationConfigTemplate.QUERY_PARAM_APP_NAME, appName);
        return q.executeUpdate();
    }

    @Override
    public int deleteGroupLevelAppResource(String appName, final String groupName, final String templateName) {
        final Query q = em.createNamedQuery(JpaGroupAppConfigTemplate.QUERY_DELETE_GROUP_LEVEL_APP_RESOURCE_BY_APP_GROUP_TEMPLATE_NAME);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_APP_NAME, appName);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_GRP_NAME, groupName);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_TEMPLATE_NAME, templateName);
        return q.executeUpdate();
    }

    @Override
    public int deleteWebServerResources(final List<String> templateNameList, final String webServerName) {
        final Query q = em.createNamedQuery(JpaWebServerConfigTemplate
                .QUERY_DELETE_WEBSERVER_RESOURCES_BY_TEMPLATE_NAME_LIST_WEBSERVER_NAME);
        q.setParameter(JpaWebServerConfigTemplate.QUERY_PARAM_TEMPLATE_NAME_LIST, templateNameList);
        q.setParameter(JpaWebServerConfigTemplate.QUERY_PARAM_WEBSERVER_NAME, webServerName);
        return q.executeUpdate();
    }

    @Override
    public int deleteGroupLevelWebServerResources(final List<String> templateNameList, final String groupName) {
        final Query q = em.createNamedQuery(JpaGroupWebServerConfigTemplate
                .QUERY_DELETE_GROUP_LEVEL_WEBSERVER_RESOURCES_BY_TEMPLATE_NAME_LIST_GROUP_NAME);
        q.setParameter(JpaGroupWebServerConfigTemplate.QUERY_PARAM_TEMPLATE_NAME_LIST, templateNameList);
        q.setParameter(JpaGroupWebServerConfigTemplate.QUERY_PARAM_GROUP_NAME, groupName);
        return q.executeUpdate();
    }

    @Override
    public int deleteJvmResources(final List<String> templateNameList, final String jvmName) {
        final Query q = em.createNamedQuery(JpaJvmConfigTemplate.QUERY_DELETE_JVM_RESOURCES_BY_TEMPLATE_NAME_LIST_JVM_NAME);
        q.setParameter(JpaJvmConfigTemplate.QUERY_PARAM_TEMPLATE_NAME_LIST, templateNameList);
        q.setParameter(JpaJvmConfigTemplate.QUERY_PARAM_JVM_NAME, jvmName);
        return q.executeUpdate();
    }

    @Override
    public int deleteGroupLevelJvmResources(final List<String> templateNameList, final String groupName) {
        final Query q = em.createNamedQuery(JpaGroupJvmConfigTemplate.QUERY_DELETE_GROUP_LEVEL_JVM_RESOURCES_BY_TEMPLATE_NAME_LIST_GROUP_NAME);
        q.setParameter(JpaGroupJvmConfigTemplate.QUERY_PARAM_TEMPLATE_NAME_LIST, templateNameList);
        q.setParameter(JpaGroupJvmConfigTemplate.QUERY_PARAM_GROUP_NAME, groupName);
        return q.executeUpdate();
    }

    @Override
    public int deleteAppResources(final List<String> templateNameList, final String appName, final String jvmName) {
        final Query q = em.createNamedQuery(JpaApplicationConfigTemplate.QUERY_DELETE_APP_RESOURCES_BY_TEMPLATE_NAME_LIST_APP_JVM_NAME);
        q.setParameter(JpaApplicationConfigTemplate.QUERY_PARAM_TEMPLATE_NAME_LIST, templateNameList);
        q.setParameter(JpaApplicationConfigTemplate.QUERY_PARAM_JVM_NAME, jvmName);
        q.setParameter(JpaApplicationConfigTemplate.QUERY_PARAM_APP_NAME, appName);
        return q.executeUpdate();
    }

    @Override
    public int deleteGroupLevelAppResources(final String appName, final String groupName, final List<String> templateNameList) {
        final Query q = em.createNamedQuery(JpaGroupAppConfigTemplate.QUERY_DELETE_GROUP_LEVEL_APP_RESOURCES_BY_APP_GROUP_NAME_TEMPLATE_NAME_LIST);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_APP_NAME, appName);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_GRP_NAME, groupName);
        q.setParameter(JpaGroupAppConfigTemplate.QUERY_PARAM_TEMPLATE_NAME_LIST, templateNameList);
        return q.executeUpdate();
    }
}
