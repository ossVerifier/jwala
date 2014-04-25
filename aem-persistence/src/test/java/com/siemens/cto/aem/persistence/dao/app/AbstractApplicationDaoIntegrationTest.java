package com.siemens.cto.aem.persistence.dao.app;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.group.Group;

import static com.siemens.cto.aem.domain.model.id.Identifier.*;

import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplication;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;

import static org.apache.commons.lang.RandomStringUtils.*;
import static org.junit.Assert.*;


@Transactional
public abstract class AbstractApplicationDaoIntegrationTest {

    @Autowired
    private ApplicationDao applicationDao;
    
    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;
    
    /** Expected data */
    private JpaApplication jpaApplication;

    /** Expected data */
    private JpaApplication jpaApplicationWithGroup;
    
    private PaginationParameter limitNone = new PaginationParameter(1000, 1);
    private PaginationParameter limit10 = new PaginationParameter(0,10);
    private PaginationParameter limitAll = PaginationParameter.all();
       
    private JpaGroup        jpaGroup;
    
    @Before public void setUp() {
        jpaApplication = new JpaApplication();
        jpaApplication.setName(randomAlphanumeric(5));
        jpaApplication.setWarPath(randomAscii(10));
        jpaApplication.setWebAppContext("/" + randomAscii(5));
        entityManager.persist(jpaApplication);
        
        jpaGroup = new JpaGroup();
        jpaGroup.setName("testJpaApp" + randomAscii(5));
        entityManager.persist(jpaGroup);
        
        jpaApplicationWithGroup = new JpaApplication();
        jpaApplicationWithGroup.setName(randomAlphanumeric(5));
        jpaApplicationWithGroup.setWarPath(randomAscii(10));
        jpaApplicationWithGroup.setWebAppContext("/" + randomAscii(5));
        jpaApplicationWithGroup.setGroup(jpaGroup);
        entityManager.persist(jpaApplicationWithGroup);
        
        entityManager.flush(); // need ids for testing.
        
    }

    @After public void tearDown() {
        entityManager.createQuery("delete from JpaApplication").executeUpdate();
    }
    
    @Test public void testGetApplication() {
        Application app = applicationDao.getApplication(id(jpaApplication.id, Application.class));
        assertNotNull(app);
        assertJpaApplicationMatches(app, jpaApplication);        
        Application app2 = applicationDao.getApplication(id(jpaApplicationWithGroup.id, Application.class));
        assertNotNull(app2);
        assertJpaApplicationMatches(app2, jpaApplicationWithGroup);        
    }
    
    @Test(expected = NotFoundException.class) public void testGetApplicationNotFound() {
        applicationDao.getApplication(id(0L, Application.class));
    }

    @Test public void testGetApplications() {
        List<Application> allApplications = applicationDao.getApplications(PaginationParameter.all());
        assertTrue(allApplications.size() == 2);
        assertJpaApplicationMatches(allApplications.get(0), jpaApplicationWithGroup);
        assertJpaApplicationMatches(allApplications.get(1), jpaApplication);
    }

    @Test public void testGetLimitedApplications() {
        List<Application> allApplications = applicationDao.getApplications(limitNone);
        assertTrue(allApplications.size() == 0);
    }

    @Test public void testFindApplicationsNoGroup() {
        List<Application> applications = applicationDao.findApplications("groupMissing", limitAll);
        assertTrue(applications.size() == 0);
    }

    @Test public void testFindApplicationsBelongingTo() {
        
        List<Application> applications = applicationDao.findApplications(jpaGroup.getName(), limit10);
        assertTrue(applications.size() == 1);
        assertJpaApplicationMatches(applications.get(0), jpaApplicationWithGroup);
    }

    @Test public void testFindApplicationsBelongingToGroupId() {        
        List<Application> applications = applicationDao.findApplicationsBelongingTo(id(jpaGroup.getId(), Group.class), limit10);
        assertTrue(applications.size() == 1);
        assertJpaApplicationMatches(applications.get(0), jpaApplicationWithGroup);
    }
    
    private void assertJpaApplicationMatches(Application a, JpaApplication jpaApplication) {
        if(a.getId() != null || jpaApplication.id != 0) {
            assertEquals(jpaApplication.id, a.getId().getId());
        }
        assertEquals(jpaApplication.version, a.getVersion());
        assertEquals(jpaApplication.warPath, a.getWarPath());
        assertEquals(jpaApplication.name, a.getName());
        assertEquals(jpaApplication.webAppContext, a.getWebAppContext());    
        if(a.getGroup() != null || jpaApplication.group != null) {
            assertGroupMatches(jpaApplication.group, a.getGroup());
        }
    }
    
    private void assertGroupMatches(JpaGroup jpaGroup, Group group) {
        assertEquals(jpaGroup.getId(), group.getId().getId());
        assertEquals(jpaGroup.getName(), group.getName());
    }

}
