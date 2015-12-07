package com.siemens.cto.aem.persistence.dao.app.impl.jpa;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.persistence.dao.app.ApplicationDao;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplication;
import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServer;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaAppBuilder;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class JpaApplicationDaoImpl implements ApplicationDao {
    
    @PersistenceContext(unitName = "aem-unit")
    EntityManager em;
    
    /*
     * When creating create/update methods, we must call the methods to update the 
     * audit columns:
     * 
     * jpaGroup.setCreateBy(userId);
     * jpaGroup.setCreateDate(updateDate);
     * jpaGroup.setUpdateBy(userId);
     * jpaGroup.setLastUpdateDate(updateDate);
     *  
     */

    @Override
    public Application getApplication(Identifier<Application> aApplicationId) throws NotFoundException {
        JpaApplication jpaApp = em.find(JpaApplication.class, aApplicationId.getId());
        if(jpaApp == null) {
            throw new NotFoundException(AemFaultType.APPLICATION_NOT_FOUND,
                    "Application not found: " + aApplicationId);
        }
        return JpaAppBuilder.appFrom(jpaApp);
    }

    @Override
    public List<Application> getApplications() {
        Query q = em.createQuery("select a from JpaApplication a");
        return buildApplications(q.getResultList());
    }

    @SuppressWarnings("unchecked")
    private List<Application> buildApplications(List<?> resultList) {
        ArrayList<Application> apps = new ArrayList<>(resultList.size());
        for(JpaApplication jpa : (List<JpaApplication>)resultList) {
            apps.add(JpaAppBuilder.appFrom(jpa));
        }
        return apps;
    }
    @Override
    public List<Application> findApplications(String aGroupName) {
        Query q = em.createNamedQuery(JpaApplication.QUERY_BY_GROUP_NAME);
        q.setParameter(JpaApplication.GROUP_NAME_PARAM, aGroupName);
        return buildApplications(q.getResultList());
    }

    @Override
    public List<Application> findApplicationsBelongingTo(Identifier<Group> aGroupId) {
        Query q = em.createNamedQuery(JpaApplication.QUERY_BY_GROUP_ID);
        q.setParameter(JpaApplication.GROUP_ID_PARAM, aGroupId.getId());
        return buildApplications(q.getResultList());
    }

    @Override
    public List<Application> findApplicationsBelongingToJvm(Identifier<Jvm> aJvmId) {
        Query q = em.createNamedQuery(JpaApplication.QUERY_BY_JVM_ID);
        q.setParameter(JpaApplication.JVM_ID_PARAM, aJvmId.getId());
        return buildApplications(q.getResultList());
    }

    @Override
    public List<Application> findApplicationsBelongingToWebServer(String aWebServerName) {
        // TODO: Use named query
        Query q = em.createQuery("SELECT ws FROM JpaWebServer ws WHERE ws.name = :wsName");
        q.setParameter(JpaApplication.WEB_SERVER_NAME_PARAM, aWebServerName);
        final JpaWebServer webServer = (JpaWebServer) q.getSingleResult();
        webServer.getGroups().size(); // Since it's lazy loaded we do this. TODO: Try to use JOIN FETCH.

        q = em.createNamedQuery(JpaApplication.QUERY_BY_WEB_SERVER_NAME);
        q.setParameter("groups", webServer.getGroups());
        return buildApplications(q.getResultList());
    }

    @Override
    public Application findApplicationByName(final String name) {
        final Query q = em.createNamedQuery(JpaApplication.QUERY_BY_NAME);
        q.setParameter("appName", name);
        return JpaAppBuilder.appFrom((JpaApplication) q.getSingleResult());
    }

    @Override
    public Application findApplication(final String appName, final String groupName, final String jvmName) {
        final Query q = em.createNamedQuery(JpaApplication.QUERY_BY_GROUP_JVM_AND_APP_NAME);
        q.setParameter("appName", appName);
        q.setParameter("groupName", groupName);
        q.setParameter("jvmName", jvmName);
        return JpaAppBuilder.appFrom((JpaApplication) q.getSingleResult());
    }

}
