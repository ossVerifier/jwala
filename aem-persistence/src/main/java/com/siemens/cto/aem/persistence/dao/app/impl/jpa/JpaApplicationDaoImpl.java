package com.siemens.cto.aem.persistence.dao.app.impl.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.persistence.dao.app.ApplicationDao;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplication;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaGroupBuilder;

public class JpaApplicationDaoImpl implements ApplicationDao {
    
    @PersistenceContext(unitName = "aem-unit")
    EntityManager em;

    @Override
    public Application getApplication(Identifier<Application> aApplicationId) throws NotFoundException {
        JpaApplication jpaApp = em.find(JpaApplication.class, aApplicationId.getId());
        if(jpaApp == null) {
            throw new NotFoundException(AemFaultType.APPLICATION_NOT_FOUND,
                    "Application not found: " + aApplicationId);
        }
        return buildApplication(jpaApp);
    }

    @Override
    public List<Application> getApplications(PaginationParameter somePagination) {        
        Query q = em.createQuery("select a from JpaApplication a");
        if(somePagination.isLimited()) {
            q.setFirstResult(somePagination.getOffset());
            q.setMaxResults(somePagination.getLimit());
        } 
        return buildApplications(q.getResultList());              
    }

    @SuppressWarnings("unchecked")
    private List<Application> buildApplications(List<?> resultList) {
        ArrayList<Application> apps = new ArrayList<>(resultList.size());
        for(JpaApplication jpa : (List<JpaApplication>)resultList) {
            apps.add(buildApplication(jpa));
        }
        return apps;
    }

    private Application buildApplication(JpaApplication jpa) {
        Application a = new Application();
        a.name = jpa.name;
        a.warPath = jpa.warPath;
        a.version = jpa.version;
        a.webAppContext = jpa.webAppContext;        
        a.group = jpa.group == null ? null : new JpaGroupBuilder(jpa.group).build(); 
        return a;
    }

    @Override
    public List<Application> findApplications(String aGroupName, PaginationParameter somePagination) {
        Query q = em.createNamedQuery(JpaApplication.QUERY_BY_GROUP_NAME);
        q.setParameter(JpaApplication.GROUP_NAME_PARAM, aGroupName);
        if(somePagination.isLimited()) {
            q.setFirstResult(somePagination.getOffset());
            q.setMaxResults(somePagination.getLimit());
        } 
        return buildApplications(q.getResultList());
    }

    @Override
    public List<Application> findApplicationsBelongingTo(Identifier<Group> aGroupId,
            PaginationParameter somePagination) {
        Query q = em.createNamedQuery(JpaApplication.QUERY_BY_GROUP_ID);
        q.setParameter(JpaApplication.GROUP_ID_PARAM, aGroupId.getId());
        if(somePagination.isLimited()) {
            q.setFirstResult(somePagination.getOffset());
            q.setMaxResults(somePagination.getLimit());
        } 
        return buildApplications(q.getResultList());
    }

}
