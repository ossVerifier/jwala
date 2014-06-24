package com.siemens.cto.aem.persistence.dao.webserver;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.CreateGroupCommand;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.UpdateGroupCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.webserver.CreateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.UpdateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.persistence.dao.group.GroupDao;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplication;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServer;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

import static com.siemens.cto.aem.persistence.dao.group.GroupEventsTestHelper.createCreateGroupEvent;
import static com.siemens.cto.aem.persistence.dao.webserver.WebServerEventsTestHelper.createCreateWebServerEvent;
import static com.siemens.cto.aem.persistence.dao.webserver.WebServerEventsTestHelper.createUpdateWebServerEvent;
import static org.junit.Assert.*;

@Transactional
public abstract class AbstractWebServerDaoIntegrationTest {

	@Autowired
	private WebServerDao webServerDao;

	@Autowired
	private GroupDao groupDao;

	private WebServer preCreatedWebServer;
	private Group preCreatedGroup;

	private Collection<Identifier<Group>> preCreatedGroupIds;

	private String userName;
	private static final String TEST_WS_NAME = "Tomcat Operations Center TEST";
	private static final Integer TEST_WS_PORT = Integer.valueOf(8080);
    private static final Integer TEST_WS_HTTPS_PORT = Integer.valueOf(8009);
	private static final String TEST_WS_HOST = "localhost";
	private static final String TEST_WS_GROUP_NAME = "test group";
	private static final String TEST_USER_NAME = "Auto-constructed User ";
	private static final String UNCHECKED_WS_NAME = "noname";
	private static final Integer UNCHECKED_WS_PORT = Integer.valueOf(1023);
    private static final Integer UNCHECKED_WS_HTTPS_PORT = Integer.valueOf(1024);
	private static final String UNCHECKED_WS_HOST = "nohost";
	private static final Long NONEXISTANT_WS_ID = Long.valueOf(-123456L);
	private static final Long NONEXISTANT_GROUP_ID = Long.valueOf(-123456L);
	private static final Identifier<Group> NONEXISTANT_GROUP = new Identifier<>(
            NONEXISTANT_GROUP_ID);
	private static final Collection<Identifier<Group>> NONEXISTANT_GROUP_IDS = new ArrayList<>(1);
	private static final String UNIQUE_NEW_WS_NAME = "Web Server Name to turn into a duplicate";
	private static final String SECOND_WS_GROUP_NAME = "test group 2";
	private static final String SECOND_TEST_WS_NAME = "TOC Test 2";

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;

	@Before
	public void setUp() throws Exception {

		preCreatedGroup = groupDao.createGroup(createCreateGroupEvent(
				TEST_WS_GROUP_NAME, TEST_USER_NAME));

		preCreatedGroupIds = new ArrayList<>(1);
		preCreatedGroupIds.add(preCreatedGroup.getId());

		preCreatedWebServer = webServerDao
				.createWebServer(createCreateWebServerEvent(
						preCreatedGroupIds, TEST_WS_NAME, TEST_WS_HOST,
						TEST_WS_PORT, TEST_WS_HTTPS_PORT, userName));
	}

	static {
	    NONEXISTANT_GROUP_IDS.add(NONEXISTANT_GROUP);
	}

	@Test
	public void testCreateWebServer() {
		assertEquals(TEST_WS_NAME, preCreatedWebServer.getName());
		assertNotNull(preCreatedWebServer.getId());
	}

	@Test(expected = BadRequestException.class)
	public void testCreateDuplicateWebServer() {

		final Event<CreateWebServerCommand> createWebServer = createCreateWebServerEvent(
				preCreatedGroupIds, TEST_WS_NAME, UNCHECKED_WS_HOST,
				UNCHECKED_WS_PORT, TEST_WS_HTTPS_PORT, userName);

		webServerDao.createWebServer(createWebServer);
	}

	@Test
	public void testUpdateWebServer() {

		final Event<UpdateWebServerCommand> updateWebServer = createUpdateWebServerEvent(
				preCreatedWebServer.getId(), preCreatedGroupIds,
				"My New Name", "My New Host", Integer.valueOf(1), Integer.valueOf(2), userName);

		final WebServer actualWebServer = webServerDao
				.updateWebServer(updateWebServer);

		assertEquals(updateWebServer.getCommand().getNewName(),
				actualWebServer.getName());
		assertEquals(updateWebServer.getCommand().getNewHost(),
				actualWebServer.getHost());
		assertEquals(updateWebServer.getCommand().getNewPort(),
				actualWebServer.getPort());
        assertEquals(updateWebServer.getCommand().getNewHttpsPort(),
                actualWebServer.getHttpsPort());
		assertEquals(updateWebServer.getCommand().getId(),
				actualWebServer.getId());

	}

	@Test(expected = NotFoundException.class)
	public void testUpdateNonExistent() {

		final Identifier<WebServer> nonExistentWebServerId = new Identifier<>(
				NONEXISTANT_WS_ID);

		webServerDao.updateWebServer(createUpdateWebServerEvent(
				nonExistentWebServerId, preCreatedGroupIds,
				UNCHECKED_WS_NAME, UNCHECKED_WS_HOST, UNCHECKED_WS_PORT, UNCHECKED_WS_HTTPS_PORT,
				userName));
	}

	@Test(expected = BadRequestException.class)
	public void testUpdateDuplicateWebServer() {

		final WebServer newWebServer = webServerDao
				.createWebServer(createCreateWebServerEvent(
				        preCreatedGroupIds, UNIQUE_NEW_WS_NAME,
						UNCHECKED_WS_HOST, UNCHECKED_WS_PORT, UNCHECKED_WS_HTTPS_PORT, userName));

		webServerDao.updateWebServer(createUpdateWebServerEvent(
				newWebServer.getId(), preCreatedGroupIds,
				preCreatedWebServer.getName(), UNCHECKED_WS_HOST, UNCHECKED_WS_HTTPS_PORT,
				UNCHECKED_WS_PORT, userName));
	}

	@Test
	public void testGetWebServer() {

		final Identifier<WebServer> expectedWebServerIdentifier = preCreatedWebServer
				.getId();

		final WebServer webServer = webServerDao
				.getWebServer(expectedWebServerIdentifier);

        assertTrue(webServer.getGroupIds().containsAll(preCreatedGroupIds));
        assertEquals(preCreatedGroupIds.size(), webServer.getGroups().size());

		assertEquals(preCreatedWebServer.getName(), webServer.getName());
		assertEquals(preCreatedWebServer.getHost(), webServer.getHost());
		assertEquals(preCreatedWebServer.getPort(), webServer.getPort());
		assertEquals(expectedWebServerIdentifier, webServer.getId());
		assertEquals(preCreatedWebServer, webServer);
		assertEquals(preCreatedWebServer.hashCode(), webServer.hashCode());
	}

	@Test(expected = NotFoundException.class)
	public void testGetNonExistentWebServer() {

		webServerDao.getWebServer(new Identifier<WebServer>(NONEXISTANT_WS_ID));
	}

	@Test
	public void testGetWebServers() {

		final PaginationParameter pagination = new PaginationParameter(0, 2);

		for (int i = 0; i <= pagination.getLimit(); i++) {
			webServerDao.createWebServer(createCreateWebServerEvent(
					preCreatedGroupIds, TEST_WS_NAME + (i + 1),
					UNCHECKED_WS_HOST, UNCHECKED_WS_PORT, UNCHECKED_WS_HTTPS_PORT, TEST_USER_NAME
							+ (i + 1)));
		}

		final List<WebServer> actualWebServers = webServerDao
				.getWebServers(pagination);

		assertEquals(pagination.getLimit().intValue(), actualWebServers.size());
	}

	@Test
	public void testFindWebServers() {

		final String expectedContains = preCreatedWebServer.getName()
				.substring(3, 5);

		final List<WebServer> actualWebServers = webServerDao.findWebServers(
				expectedContains, new PaginationParameter());

		for (final WebServer WebServer : actualWebServers) {
			assertTrue(WebServer.getName().contains(expectedContains));
		}
	}

	@Test
	public void testRemoveWebServer() {

		final Identifier<WebServer> webServerId = preCreatedWebServer.getId();

		webServerDao.removeWebServer(webServerId);

		try {
			webServerDao.getWebServer(webServerId);
		} catch (final NotFoundException nfe) {
			// Success
			return;
		}
	}

	@Test(expected = NotFoundException.class)
	public void testRemoveNonExistent() {

		final Identifier<WebServer> nonExistentWebServerId = new Identifier<>(
				NONEXISTANT_WS_ID);

		webServerDao.removeWebServer(nonExistentWebServerId);
	}

	@Test(expected = NotFoundException.class)
	public void testWebServerWithNotFoundGroup() {
		WebServer webServer = webServerDao
				.createWebServer(createCreateWebServerEvent(NONEXISTANT_GROUP_IDS,
						TEST_WS_NAME, TEST_WS_HOST, TEST_WS_PORT, TEST_WS_HTTPS_PORT,
						TEST_USER_NAME));

		assertNotNull(webServer.getId());
		assertNotNull(webServer.getId().getId());
	}

    @Test
    public void testWebServerWithNullGroup() {
        WebServer webServer = webServerDao
                .createWebServer(createCreateWebServerEvent(null, SECOND_TEST_WS_NAME,
                        TEST_WS_HOST, TEST_WS_PORT, TEST_WS_HTTPS_PORT, TEST_USER_NAME));

        assertNotNull(webServer.getId());
        assertNotNull(webServer.getId().getId());
    }

    @Test
    public void testWebServerWithEmptyGroup() {
        WebServer webServer = webServerDao
                .createWebServer(createCreateWebServerEvent(new ArrayList<Identifier<Group>>(0), SECOND_TEST_WS_NAME,
                        TEST_WS_HOST, TEST_WS_PORT, TEST_WS_HTTPS_PORT, TEST_USER_NAME));

        assertNotNull(webServer.getId());
        assertNotNull(webServer.getId().getId());
    }

	@Test
	public void testUpdateWebServerGroup() {
		Group newGroup = groupDao.createGroup(createCreateGroupEvent(
				SECOND_WS_GROUP_NAME, TEST_USER_NAME));

		ArrayList<Identifier<Group>> newGroupIds = new ArrayList<>(1);
		newGroupIds.add(newGroup.getId());

		WebServer webServer = webServerDao
				.updateWebServer(createUpdateWebServerEvent(
						preCreatedWebServer.getId(), newGroupIds,
						TEST_WS_NAME, TEST_WS_HOST, TEST_WS_PORT, TEST_WS_HTTPS_PORT,
						TEST_USER_NAME));

		assertTrue(webServer.getGroupIds().containsAll(newGroupIds));

		assertEquals(newGroupIds.size(), webServer.getGroups().size());

        assertEquals(newGroup.getName(), webServer.getGroups().iterator().next().getName());
	}

	class GeneralizedDao {
		<R> R update(Event<?> updateCommand) {
			return this.<R> updateInternal(updateCommand.getCommand(),
					updateCommand);
		}

		@SuppressWarnings("unchecked")
		private <R> R updateInternal(Object updateCommand,
				@SuppressWarnings("rawtypes") Event eventObj) {
			if (updateCommand instanceof UpdateGroupCommand) {
				return (R) groupDao.updateGroup(eventObj);
			} else if (updateCommand instanceof UpdateWebServerCommand) {
				return (R) webServerDao.updateWebServer(eventObj);
			}
			return null;
		}

		<R> R create(Event<?> createCommand) {
			return this.<R> createInternal(createCommand.getCommand(),
					createCommand);
		}

		@SuppressWarnings({ "unchecked" })
		private <R> R createInternal(Object createCommand,
				@SuppressWarnings("rawtypes") Event eventObj) {
			if (createCommand instanceof CreateGroupCommand) {
				return (R) groupDao.createGroup(eventObj);
			} else if (createCommand instanceof CreateWebServerCommand) {
				return (R) webServerDao.createWebServer(eventObj);
			}
			return null;
		}
	}

	@Test
	public void testGeneralDao() {
		GeneralizedDao generalizedDao = new GeneralizedDao();
		Group newGroup = generalizedDao.create(createCreateGroupEvent(
				SECOND_WS_GROUP_NAME, TEST_USER_NAME));

		ArrayList<Identifier<Group>> newGroupIds = new ArrayList<>(1);
        newGroupIds.add(newGroup.getId());

        WebServer webServer = generalizedDao.update(createUpdateWebServerEvent(
				preCreatedWebServer.getId(), newGroupIds, TEST_WS_NAME,
				TEST_WS_HOST, TEST_WS_PORT, TEST_WS_HTTPS_PORT, TEST_USER_NAME));

        assertTrue(webServer.getGroupIds().containsAll(newGroupIds));
        assertEquals(newGroupIds.size(), webServer.getGroups().size());

        assertEquals(newGroup.getName(), webServer.getGroups().iterator().next().getName());
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
                webServerDao.findApplications(jpaWebServer.getName(), PaginationParameter.all());

        assertEquals(4, applications.size());

        final List<String> contextList = Arrays.asList("/app1", "/app2", "/app3", "/app5");
        final List<String> generatedContextList = new ArrayList<>();

        for (final Application app : applications) {
            generatedContextList.add(app.getWebAppContext());
        }

        Collections.sort(generatedContextList);
        assertEquals(contextList.toString(), generatedContextList.toString());
    }

}
