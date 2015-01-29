package com.siemens.cto.aem.persistence.dao.app;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.path.Path;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplication;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.siemens.cto.aem.domain.model.id.Identifier.id;
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang.RandomStringUtils.randomAscii;
import static org.junit.Assert.*;


@Transactional
public abstract class AbstractApplicationDaoIntegrationTest {

    @Autowired
    private ApplicationDao applicationDao;

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;

    /** Expected data */
    private JpaApplication jpaApplication;
    private JpaApplication jpaApplicationWithGroup;
    private JpaJvm jpaJvm;
    private static final Path SVR_ROOT = new Path("./");
    private static final Path DOC_ROOT = new Path("htdocs");

    private PaginationParameter limitNone = new PaginationParameter(1000, 1);
    private PaginationParameter limit10 = new PaginationParameter(0,10);
    private PaginationParameter limitAll = PaginationParameter.all();

    private JpaGroup        jpaGroup;

    @Before public void setUp() {

        jpaJvm = new JpaJvm();
        jpaJvm.setHostName("usmlvv1junit00");
        jpaJvm.setName("jvm_name");
        jpaJvm.setHttpPort(5);
        jpaJvm.setRedirectPort(4);
        jpaJvm.setShutdownPort(3);
        jpaJvm.setAjpPort(2);
        jpaJvm.setStatusPath("/abc");
        entityManager.persist(jpaJvm);

        jpaApplication = new JpaApplication();
        jpaApplication.setName(randomAlphanumeric(5));
        jpaApplication.setWarPath(randomAscii(10));
        jpaApplication.setWebAppContext("/" + randomAscii(5));
        entityManager.persist(jpaApplication);

        jpaGroup = new JpaGroup();
        jpaGroup.setName("testJpaApp" + randomAscii(5));
        jpaGroup.setJvms(Arrays.asList(jpaJvm));
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

    @Test public void testFindApplicationsBelongingToJvm() {
        JpaGroup jpaGroup2 = new JpaGroup();
        jpaGroup2.setName("testJpaApp" + randomAscii(5));
        jpaGroup2.setJvms(Arrays.asList(jpaJvm));
        entityManager.persist(jpaGroup2);

        JpaApplication jpaApplicationWithGroup2 = new JpaApplication();
        jpaApplicationWithGroup2.setName(randomAlphanumeric(5));
        jpaApplicationWithGroup2.setWarPath(randomAscii(10));
        jpaApplicationWithGroup2.setWebAppContext("/" + randomAscii(5));
        jpaApplicationWithGroup2.setGroup(jpaGroup2);
        entityManager.persist(jpaApplicationWithGroup2);
        entityManager.flush();

        List<Application> applications = applicationDao.findApplicationsBelongingToJvm(id(jpaJvm.getId(), Jvm.class), limit10);

        assertTrue(applications.size() == 2);
        assertNotEquals(applications.get(0), applications.get(1));

        for (Application application : applications) {
            if (application.getId().equals(id(jpaApplicationWithGroup.id, Application.class))) {
                assertJpaApplicationMatches(application, jpaApplicationWithGroup);
            } else if (application.getId().equals(id(jpaApplicationWithGroup2.id, Application.class))) {
                assertJpaApplicationMatches(application, jpaApplicationWithGroup2);
            } else {
                fail();
            }
        }
    }

    @Test
    public void testFindApplicationsBelongingToWebServer() {
        final Integer testMethodHash = "testFindApplicationsBelongingToWebServer".hashCode();
        final String GROUP_NAME_PREFIX = "group" + testMethodHash;
        final String GROUP_A_NAME = GROUP_NAME_PREFIX + "A";
        final String GROUP_B_NAME = GROUP_NAME_PREFIX + "B";
        final String GROUP_C_NAME = GROUP_NAME_PREFIX + "C";

        final String WEB_SERVER_NAME = "webServer" + testMethodHash;

        final String APP_PREFIX = "app" + testMethodHash;
        final String APP_1_NAME = APP_PREFIX + "1";
        final String APP_2_NAME = APP_PREFIX + "2";
        final String APP_3_NAME = APP_PREFIX + "3";
        final String APP_4_NAME = APP_PREFIX + "4";
        final String APP_5_NAME = APP_PREFIX + "5";

        // Create groups A, B and C
        final JpaGroup jpaGroupA = new JpaGroup();
        jpaGroupA.setName(GROUP_A_NAME);
        entityManager.persist(jpaGroupA);

        final JpaGroup jpaGroupB = new JpaGroup();
        jpaGroupB.setName(GROUP_B_NAME);
        entityManager.persist(jpaGroupB);

        final JpaGroup jpaGroupC = new JpaGroup();
        jpaGroupC.setName(GROUP_C_NAME);
        entityManager.persist(jpaGroupC);

        final List<JpaGroup> groups = new ArrayList<>();
        groups.add(jpaGroupA);
        groups.add(jpaGroupB);

        // Create the web server
        final JpaWebServer jpaWebServer = new JpaWebServer();
        jpaWebServer.setName(WEB_SERVER_NAME);
        jpaWebServer.setGroups(groups);
        jpaWebServer.setHost("the-host-name");
        jpaWebServer.setPort(80);
        jpaWebServer.setStatusPath("/jk/status");
        jpaWebServer.setHttpConfigFile("d:/some-dir/httpd.conf");
        jpaWebServer.setDocRoot(DOC_ROOT.getPath());
        jpaWebServer.setSvrRoot(SVR_ROOT.getPath());
        entityManager.persist(jpaWebServer);

        // Create the applications 1, 2, 3, 4 and 5
        final JpaApplication jpaApp1 = new JpaApplication();
        jpaApp1.setName(APP_1_NAME);
        jpaApp1.setGroup(jpaGroupA);
        jpaApp1.setWebAppContext("/app1");
        entityManager.persist(jpaApp1);

        final JpaApplication jpaApp2 = new JpaApplication();
        jpaApp2.setName(APP_2_NAME);
        jpaApp2.setGroup(jpaGroupA);
        jpaApp2.setWebAppContext("/app2");
        entityManager.persist(jpaApp2);

        final JpaApplication jpaApp3 = new JpaApplication();
        jpaApp3.setName(APP_3_NAME);
        jpaApp3.setGroup(jpaGroupB);
        jpaApp3.setWebAppContext("/app3");
        entityManager.persist(jpaApp3);

        final JpaApplication jpaApp4 = new JpaApplication();
        jpaApp4.setName(APP_4_NAME);
        jpaApp4.setGroup(jpaGroupC);
        jpaApp4.setWebAppContext("/app4");
        entityManager.persist(jpaApp4);

        final JpaApplication jpaApp5 = new JpaApplication();
        jpaApp5.setName(APP_5_NAME);
        jpaApp5.setGroup(jpaGroupB);
        jpaApp5.setWebAppContext("/app5");
        entityManager.persist(jpaApp5);

        entityManager.flush();

        final List<Application> applications =
                applicationDao.findApplicationsBelongingToWebServer(jpaWebServer.getName(), limit10);

        assertEquals(4, applications.size());

        final List<String> contextList = Arrays.asList("/app1", "/app2", "/app3", "/app5");
        final List<String> generatedContextList = new ArrayList<>();

        for (final Application app : applications) {
            generatedContextList.add(app.getWebAppContext());
        }

        Collections.sort(generatedContextList);
        assertEquals(contextList.toString(), generatedContextList.toString());
    }

    private void assertJpaApplicationMatches(Application a, JpaApplication jpaApplication) {
        if(a.getId() != null || jpaApplication.id != 0) {
            assertEquals(jpaApplication.id, a.getId().getId());
        }
        assertEquals(jpaApplication.getWarPath(), a.getWarPath());
        assertEquals(jpaApplication.name, a.getName());
        assertEquals(jpaApplication.getWebAppContext(), a.getWebAppContext());
        if(a.getGroup() != null || jpaApplication.group != null) {
            assertGroupMatches(jpaApplication.group, a.getGroup());
        }
    }

    private void assertGroupMatches(JpaGroup jpaGroup, Group group) {
        assertEquals(jpaGroup.getId(), group.getId().getId());
        assertEquals(jpaGroup.getName(), group.getName());
    }

}
