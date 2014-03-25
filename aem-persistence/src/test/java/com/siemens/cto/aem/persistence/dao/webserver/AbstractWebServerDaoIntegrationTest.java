package com.siemens.cto.aem.persistence.dao.webserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.CreateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.UpdateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.WebServer;

@Transactional
public abstract class AbstractWebServerDaoIntegrationTest {

	@Autowired
	private WebServerDao webServerDao;

	private WebServer preCreatedWebServer;
	private String userName;
	private static final String TEST_WS_NAME = "Tomcat Operations Center TEST";
	private static final Integer TEST_WS_PORT = Integer.valueOf(8080);
	private static final String TEST_WS_HOST = "localhost";
	private static final String TEST_USER_NAME = "Auto-constructed User ";
	private static final String UNCHECKED_WS_NAME = "noname";
	private static final Integer UNCHECKED_WS_PORT = Integer.valueOf(1023);
	private static final String UNCHECKED_WS_HOST = "nohost";
	private static final Long NONEXISTANT_WS_ID = Long.valueOf(-123456L);
	private static final String UNIQUE_NEW_WS_NAME = "Web Server Name to turn into a duplicate";

	@Before
	public void setUp() throws Exception {

		userName = "Test User Name";

		preCreatedWebServer = webServerDao
				.createWebServer(createCreateWebServerEvent(TEST_WS_NAME,
						TEST_WS_HOST, TEST_WS_PORT, userName));
	}

	@Test
	public void testCreateWebServer() {
		assertEquals(TEST_WS_NAME, preCreatedWebServer.getName());
		assertNotNull(preCreatedWebServer.getId());
	}

	@Test(expected = BadRequestException.class)
	public void testCreateDuplicateWebServer() {

		final Event<CreateWebServerCommand> createWebServer = createCreateWebServerEvent(
				TEST_WS_NAME, UNCHECKED_WS_HOST, UNCHECKED_WS_PORT, userName);

		webServerDao.createWebServer(createWebServer);
	}

	@Test
	public void testUpdateWebServer() {

		final Event<UpdateWebServerCommand> updateWebServer = createUpdateWebServerEvent(
				preCreatedWebServer.getId(), "My New Name", "My New Host",
				Integer.valueOf(1), userName);

		final WebServer actualWebServer = webServerDao
				.updateWebServer(updateWebServer);

		assertEquals(updateWebServer.getCommand().getNewName(),
				actualWebServer.getName());
		assertEquals(updateWebServer.getCommand().getNewHost(),
				actualWebServer.getHost());
		assertEquals(updateWebServer.getCommand().getNewPort(),
				actualWebServer.getPort());
		assertEquals(updateWebServer.getCommand().getId(),
				actualWebServer.getId());

	}

	@Test(expected = NotFoundException.class)
	public void testUpdateNonExistent() {

		final Identifier<WebServer> nonExistentWebServerId = new Identifier<>(
				NONEXISTANT_WS_ID);

		webServerDao.updateWebServer(createUpdateWebServerEvent(
				nonExistentWebServerId, UNCHECKED_WS_NAME, UNCHECKED_WS_HOST,
				UNCHECKED_WS_PORT, userName));
	}

	@Test(expected = BadRequestException.class)
	public void testUpdateDuplicateWebServer() {

		final WebServer newWebServer = webServerDao
				.createWebServer(createCreateWebServerEvent(UNIQUE_NEW_WS_NAME,
						UNCHECKED_WS_HOST, UNCHECKED_WS_PORT, userName));

		webServerDao.updateWebServer(createUpdateWebServerEvent(
				newWebServer.getId(), preCreatedWebServer.getName(),
				UNCHECKED_WS_HOST, UNCHECKED_WS_PORT, userName));
	}

	@Test
	public void testGetWebServer() {

		final Identifier<WebServer> expectedWebServerIdentifier = preCreatedWebServer
				.getId();

		final WebServer webServer = webServerDao
				.getWebServer(expectedWebServerIdentifier);

		assertEquals(preCreatedWebServer.getName(), webServer.getName());
		assertEquals(preCreatedWebServer.getHost(), webServer.getHost());
		assertEquals(preCreatedWebServer.getPort(), webServer.getPort());
		assertEquals(expectedWebServerIdentifier, webServer.getId());
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
					TEST_WS_NAME + (i + 1), UNCHECKED_WS_HOST,
					UNCHECKED_WS_PORT, TEST_USER_NAME + (i + 1)));
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

	protected Event<CreateWebServerCommand> createCreateWebServerEvent(
			final String aNewWebServerName, final String aNewWebServerHost,
			final Integer aNewWebServerPort, final String aUserId) {

		final Event<CreateWebServerCommand> createWebServer = new Event<>(
				new CreateWebServerCommand(aNewWebServerName,
						aNewWebServerHost, aNewWebServerPort),
				createAuditEvent(aUserId));

		return createWebServer;
	}

	protected Event<UpdateWebServerCommand> createUpdateWebServerEvent(
			final Identifier<WebServer> id, final String aNewWebServerName,
			final String aNewWebServerHost, final Integer aNewWebServerPort,
			final String aUserId) {

		final Event<UpdateWebServerCommand> updateWebServer = new Event<>(
				new UpdateWebServerCommand(id, aNewWebServerName,
						aNewWebServerHost, aNewWebServerPort),
				createAuditEvent(aUserId));

		return updateWebServer;
	}

	protected AuditEvent createAuditEvent(final String aUserId) {
		return AuditEvent.now(new User(aUserId));
	}

}
