package com.siemens.cto.aem.persistence.dao.impl;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.persistence.dao.ApplicationDao;
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
    public Application findApplication(final String appName, final String groupName, final String jvmName) {
        final Query q = em.createNamedQuery(JpaApplication.QUERY_BY_GROUP_JVM_AND_APP_NAME);
        q.setParameter("appName", appName);
        q.setParameter("groupName", groupName);
        q.setParameter("jvmName", jvmName);
        return JpaAppBuilder.appFrom((JpaApplication) q.getSingleResult());
    }

}
