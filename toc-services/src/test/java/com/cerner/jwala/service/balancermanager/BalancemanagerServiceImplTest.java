package com.cerner.jwala.service.balancermanager;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.domain.model.balancermanager.BalancerManagerState;
import com.cerner.jwala.common.domain.model.balancermanager.WorkerStatusType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.service.HistoryService;
import com.cerner.jwala.service.MessagingService;
import com.cerner.jwala.service.app.ApplicationService;
import com.cerner.jwala.service.balancermanager.impl.BalancerManagerHtmlParser;
import com.cerner.jwala.service.balancermanager.impl.BalancerManagerHttpClient;
import com.cerner.jwala.service.balancermanager.impl.BalancerManagerServiceImpl;
import com.cerner.jwala.service.balancermanager.impl.BalancerManagerXmlParser;
import com.cerner.jwala.service.balancermanager.impl.xml.data.Manager;
import com.cerner.jwala.service.group.GroupService;
import com.cerner.jwala.service.jvm.JvmService;
import com.cerner.jwala.service.webserver.WebServerService;
import com.cerner.jwala.service.webserver.component.ClientFactoryHelper;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.cerner.jwala.common.domain.model.id.Identifier.id;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class BalancemanagerServiceImplTest {

    private BalancerManagerServiceImpl balancerManagerServiceImpl;

    @Mock
    private GroupService mockGroupService;

    @Mock
    private ApplicationService mockApplicationService;

    @Mock
    private WebServerService mockWebServerService;

    @Mock
    private JvmService mockJvmService;

    @Mock
    private MessagingService mockMessagingService;

    @Mock
    private ClientFactoryHelper mockClientFactoryHelper;

    @Mock
    private HistoryService mockHistoryService;

    @Mock
    private BalancerManagerHttpClient mockBalancerManagerHttpClient;

    private BalancerManagerHtmlParser balancerManagerHtmlParser = new BalancerManagerHtmlParser();
    private BalancerManagerXmlParser balancerManagerXmlParser = new BalancerManagerXmlParser();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.balancerManagerServiceImpl = new BalancerManagerServiceImpl(mockGroupService, mockApplicationService, mockWebServerService, mockJvmService,
                mockClientFactoryHelper, mockMessagingService, mockHistoryService, balancerManagerHtmlParser, balancerManagerXmlParser, mockBalancerManagerHttpClient) {
            public void sendMessage(final WebServer webServer, final String message) {

            }
        };
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");
    }

    @After
    public void tearDown() {
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testGetNVP() {
        assertEquals(4, balancerManagerServiceImpl.getNvp("myWorkerUrl", "", "").size());
    }

    @Test
    public void testDrainUserGroup() throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
        final MockGroup mockGroup = new MockGroup();
        when(mockGroupService.getGroup("mygroupName")).thenReturn(mockGroup.getGroup());
        when(mockApplicationService.findApplications(new Identifier<Group>(1L))).thenReturn(mockGroup.findApplications());
        when(mockWebServerService.findWebServers(new Identifier<Group>(1L))).thenReturn(mockGroup.findWebServers());
        WebServer webServer = mockGroup.getWebServer("myWebServerName");
        when(mockWebServerService.isStarted(webServer)).thenReturn(true);

        ClientHttpResponse mockResponseHtml = mock(ClientHttpResponse.class);
        when(mockResponseHtml.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost/balancer-manager"))).thenReturn(mockResponseHtml);
        when(mockResponseHtml.getBody()).thenReturn(new ByteArrayInputStream(getBalancerManagerResponseHtml().getBytes()));

        ClientHttpResponse mockResponseHtml2 = mock(ClientHttpResponse.class);
        when(mockResponseHtml2.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost2/balancer-manager"))).thenReturn(mockResponseHtml2);
        when(mockResponseHtml2.getBody()).thenReturn(new ByteArrayInputStream(getBalancerManagerResponseHtml().getBytes()));

        ClientHttpResponse mockResponseXml = mock(ClientHttpResponse.class);
        when(mockResponseXml.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost/balancer-manager?b=lb-health-check-4.0&xml=1&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357"))).thenReturn(mockResponseXml);
        when(mockResponseXml.getBody()).thenReturn(new ByteArrayInputStream(getBalancerManagerResponseXml().getBytes()));

        ClientHttpResponse mockResponseXml2 = mock(ClientHttpResponse.class);
        when(mockResponseXml2.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost2/balancer-manager?b=lb-health-check-4.0&xml=1&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357"))).thenReturn(mockResponseXml2);
        when(mockResponseXml2.getBody()).thenReturn(new ByteArrayInputStream(getBalancerManagerResponseXml().getBytes()));

        CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);
        StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.OK.value());
        when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockBalancerManagerHttpClient.doHttpClientPost(any(String.class), anyListOf(NameValuePair.class))).thenReturn(mockResponse);

        ClientHttpResponse mockWorkerResponse = mock(ClientHttpResponse.class);
        when(mockWorkerResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0057:9101/hct"))).thenReturn(mockWorkerResponse);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0058:9101/hct"))).thenReturn(mockWorkerResponse);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0057:9111/hct"))).thenReturn(mockWorkerResponse);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0058:9111/hct"))).thenReturn(mockWorkerResponse);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0057:9121/hct"))).thenReturn(mockWorkerResponse);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0058:9121/hct"))).thenReturn(mockWorkerResponse);

        when(mockClientFactoryHelper.requestGet(new URI("https://localhost2/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0057:9101/hct"))).thenReturn(mockWorkerResponse);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost2/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0058:9101/hct"))).thenReturn(mockWorkerResponse);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost2/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0057:9111/hct"))).thenReturn(mockWorkerResponse);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost2/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0058:9111/hct"))).thenReturn(mockWorkerResponse);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost2/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0057:9121/hct"))).thenReturn(mockWorkerResponse);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost2/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0058:9121/hct"))).thenReturn(mockWorkerResponse);
        when(mockWorkerResponse.getBody()).thenReturn(new ByteArrayInputStream(getWorkerHtml().getBytes()));

        BalancerManagerState balancerManagerState = balancerManagerServiceImpl.drainUserGroup("mygroupName", "", getUser());
        assertEquals(2, balancerManagerState.getwebServers().size());
    }

    @Test
    public void testDrainUserWebServer() throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyManagementException {
        final MockGroup mockGroup = new MockGroup();

        when(mockGroupService.getGroup("mygroupName")).thenReturn(mockGroup.getGroup());
        when(mockApplicationService.findApplications(new Identifier<Group>(1L))).thenReturn(mockGroup.findApplications());
        when(mockWebServerService.findWebServers(new Identifier<Group>(1L))).thenReturn(mockGroup.findWebServers());
        when(mockWebServerService.getWebServer("myWebServerName")).thenReturn(mockGroup.getWebServer("myWebServerName"));
        WebServer webServer = mockGroup.getWebServer("myWebServerName");
        when(mockWebServerService.isStarted(webServer)).thenReturn(true);

        ClientHttpResponse mockResponseHtml = mock(ClientHttpResponse.class);
        when(mockResponseHtml.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockClientFactoryHelper.requestGet(any(URI.class))).thenReturn(mockResponseHtml);

        when(mockResponseHtml.getBody()).thenReturn(new ByteArrayInputStream(getBalancerManagerResponseHtml().getBytes()));
        ClientHttpResponse mockResponseXml = mock(ClientHttpResponse.class);
        when(mockResponseXml.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost/balancer-manager?b=lb-health-check-4.0&xml=1&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357"))).thenReturn(mockResponseXml);
        when(mockResponseXml.getBody()).thenReturn(new ByteArrayInputStream(getBalancerManagerResponseXml().getBytes()));

        CloseableHttpResponse mockResponse = mock(CloseableHttpResponse.class);
        StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockStatusLine.getStatusCode()).thenReturn(HttpStatus.OK.value());
        when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);

        ClientHttpResponse mockWorkerResponse = mock(ClientHttpResponse.class);
        when(mockWorkerResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0057:9101/hct"))).thenReturn(mockWorkerResponse);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0058:9101/hct"))).thenReturn(mockWorkerResponse);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0057:9111/hct"))).thenReturn(mockWorkerResponse);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0058:9111/hct"))).thenReturn(mockWorkerResponse);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0057:9121/hct"))).thenReturn(mockWorkerResponse);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0058:9121/hct"))).thenReturn(mockWorkerResponse);
        when(mockWorkerResponse.getBody()).thenReturn(new ByteArrayInputStream(getWorkerHtml().getBytes()));

        when(mockBalancerManagerHttpClient.doHttpClientPost(any(String.class), anyListOf(NameValuePair.class))).thenReturn(mockResponse);
        BalancerManagerState balancerManagerState = balancerManagerServiceImpl.drainUserWebServer("mygroupName", "myWebServerName", getUser());
        assertEquals(1, balancerManagerState.getwebServers().size());
    }

    @Test
    public void testGetWorkerXml() {
        Manager manager = balancerManagerXmlParser.getWorkerXml(getBalancerManagerResponseXml());
        assertEquals(2, manager.getBalancers().size());
    }

    @Test
    public void testGetWorkers() {
        Manager manager = balancerManagerXmlParser.getWorkerXml(getBalancerManagerResponseXml());
        //balancerManagerServiceImpl.setBalancerName("lb-health-check-4.0");
        Map<String, String> workers = balancerManagerXmlParser.getWorkers(manager, "lb-health-check-4.0");
        assertEquals(6, workers.size());
    }

    @Test
    public void testGetWorkersMulti() {
        Manager manager = balancerManagerXmlParser.getWorkerXml(getBalancerManagerResponseXmlMulti());
        //balancerManagerServiceImpl.setBalancerName("lb-slpa-4.0.0800.02");
        Map<String, String> workers = balancerManagerXmlParser.getWorkers(manager, "lb-slpa-4.0.0800.02");
        assertEquals(2, workers.size());
    }

    @Test
    public void testGetGroupDrainStatus() throws IOException, URISyntaxException {
        final MockGroup mockGroup = new MockGroup();
        when(mockGroupService.getGroup("mygroupName")).thenReturn(mockGroup.getGroup());
        when(mockApplicationService.findApplications(new Identifier<Group>(1L))).thenReturn(mockGroup.findApplications());
        when(mockWebServerService.findWebServers(new Identifier<Group>(1L))).thenReturn(mockGroup.findWebServers());
        WebServer webServer = mockGroup.getWebServer("myWebServerName");
        when(mockWebServerService.isStarted(webServer)).thenReturn(true);
        ClientHttpResponse mockResponseHtml = mock(ClientHttpResponse.class);
        when(mockResponseHtml.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost/balancer-manager"))).thenReturn(mockResponseHtml);
        when(mockResponseHtml.getBody()).thenReturn(new ByteArrayInputStream(getBalancerManagerResponseHtml().getBytes()));
        ClientHttpResponse mockResponseHtml2 = mock(ClientHttpResponse.class);
        when(mockResponseHtml2.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost2/balancer-manager"))).thenReturn(mockResponseHtml2);
        when(mockResponseHtml2.getBody()).thenReturn(new ByteArrayInputStream(getBalancerManagerResponseHtml().getBytes()));
        ClientHttpResponse mockResponseXml = mock(ClientHttpResponse.class);
        when(mockResponseXml.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost/balancer-manager?b=lb-health-check-4.0&xml=1&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357"))).thenReturn(mockResponseXml);
        when(mockResponseXml.getBody()).thenReturn(new ByteArrayInputStream(getBalancerManagerResponseXml().getBytes()));
        ClientHttpResponse mockResponseXml2 = mock(ClientHttpResponse.class);
        when(mockResponseXml2.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost2/balancer-manager?b=lb-health-check-4.0&xml=1&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357"))).thenReturn(mockResponseXml2);
        when(mockResponseXml2.getBody()).thenReturn(new ByteArrayInputStream(getBalancerManagerResponseXml().getBytes()));

        ClientHttpResponse mockWorkerResponse = mock(ClientHttpResponse.class);
        when(mockWorkerResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0057:9101/hct"))).thenReturn(mockWorkerResponse);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0058:9101/hct"))).thenReturn(mockWorkerResponse);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0057:9111/hct"))).thenReturn(mockWorkerResponse);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0058:9111/hct"))).thenReturn(mockWorkerResponse);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0057:9121/hct"))).thenReturn(mockWorkerResponse);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0058:9121/hct"))).thenReturn(mockWorkerResponse);

        when(mockClientFactoryHelper.requestGet(new URI("https://localhost2/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0057:9101/hct"))).thenReturn(mockWorkerResponse);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost2/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0058:9101/hct"))).thenReturn(mockWorkerResponse);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost2/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0057:9111/hct"))).thenReturn(mockWorkerResponse);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost2/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0058:9111/hct"))).thenReturn(mockWorkerResponse);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost2/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0057:9121/hct"))).thenReturn(mockWorkerResponse);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost2/balancer-manager?b=lb-health-check-4.0&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357&w=https://usmlvv1cds0058:9121/hct"))).thenReturn(mockWorkerResponse);
        when(mockWorkerResponse.getBody()).thenReturn(new ByteArrayInputStream(getWorkerHtml().getBytes()));

        BalancerManagerState balancerManagerState = balancerManagerServiceImpl.getGroupDrainStatus("mygroupName", getUser());
        System.out.println(balancerManagerState);
        assertEquals(2, balancerManagerState.getwebServers().size());
        for (BalancerManagerState.WebServerDrainStatus webServerDrainStatus : balancerManagerState.getwebServers()) {
            System.out.println(webServerDrainStatus.getWebServerName());
            assertEquals(6, webServerDrainStatus.getjvms().size());
            for (BalancerManagerState.WebServerDrainStatus.JvmDrainStatus jvmDrainStatus : webServerDrainStatus.getjvms()) {
                System.out.println(jvmDrainStatus.getJvmName() + " " + jvmDrainStatus.getDrainingMode());
            }
        }
    }

    @Test
    public void testManager() {
        Manager manager = balancerManagerXmlParser.getWorkerXml(getBalancerManagerResponseXml());
        System.out.println(manager.getBalancers().size());
        assertEquals(2, manager.getBalancers().size());
        List<Manager.Balancer> balancerList = manager.getBalancers();
        for (Manager.Balancer balancer : balancerList) {
            System.out.println(balancer.toString());
            for (Manager.Balancer.Worker worker : balancer.getWorkers()) {
                System.out.println(worker.toString());
            }
        }
    }

    @Test
    public void testGetRequireWebServers() {
        final String emptyString = "";
        assertEquals(0, balancerManagerServiceImpl.getRequireWebServers(emptyString).length);
        final String goodString1 = "webSever1";
        assertEquals(1, balancerManagerServiceImpl.getRequireWebServers(goodString1).length);
        final String goodString2 = "webServer1, webServer2";
        assertEquals(2, balancerManagerServiceImpl.getRequireWebServers(goodString2).length);
        final String goodString3 = "asdfafafa afafadfafaf;adfafafdaf";
        assertEquals(1, balancerManagerServiceImpl.getRequireWebServers(goodString3).length);
    }

    @Test
    public void testFindMatchWebServers() {
        final MockGroup mockGroup = new MockGroup();
        Set<Group> groups = new HashSet<>();
        groups.add(mockGroup.getGroup());
        WebServer webServer1 = new WebServer(id(1L, WebServer.class), groups, "webServer1");
        WebServer webServer2 = new WebServer(id(2L, WebServer.class), groups, "webServer2");
        List<WebServer> webServers = new ArrayList<>();
        webServers.add(webServer1);
        webServers.add(webServer2);
        String[] webServerArray = "webServer1, webServer2".split(",");
        assertEquals(2, balancerManagerServiceImpl.findMatchWebServers(webServers, webServerArray).size());
    }

    @Test
    public void testNotFindMatchWebServers() {
        final MockGroup mockGroup = new MockGroup();
        Set<Group> groups = new HashSet<>();
        groups.add(mockGroup.getGroup());
        WebServer webServer1 = new WebServer(id(1L, WebServer.class), groups, "webServer1");
        WebServer webServer2 = new WebServer(id(2L, WebServer.class), groups, "webServer2");
        List<WebServer> webServers = new ArrayList<>();
        webServers.add(webServer1);
        webServers.add(webServer2);
        String[] webServerArray = "webServer3, webServer4".split(",");
        try {
            balancerManagerServiceImpl.findMatchWebServers(webServers, webServerArray);
            fail();
        } catch (Exception e) {
            assertEquals("com.cerner.jwala.common.exception.InternalErrorException: webServer3, webServer4 cannot be found in the group", e.toString());
        }
    }

    @Test
    public void testPartialFindMatchWebServers() {
        final MockGroup mockGroup = new MockGroup();
        Set<Group> groups = new HashSet<>();
        groups.add(mockGroup.getGroup());
        WebServer webServer1 = new WebServer(id(1L, WebServer.class), groups, "webServer1");
        WebServer webServer2 = new WebServer(id(2L, WebServer.class), groups, "webServer2");
        List<WebServer> webServers = new ArrayList<>();
        webServers.add(webServer1);
        webServers.add(webServer2);
        String[] webServerArray = "webServer2".split(",");
        assertEquals(1, balancerManagerServiceImpl.findMatchWebServers(webServers, webServerArray).size());
    }

    @Test
    public void testPartialFindMatchWebServers_PostMore() {
        final MockGroup mockGroup = new MockGroup();
        Set<Group> groups = new HashSet<>();
        groups.add(mockGroup.getGroup());
        WebServer webServer1 = new WebServer(id(1L, WebServer.class), groups, "webServer1");
        List<WebServer> webServers = new ArrayList<>();
        webServers.add(webServer1);
        String[] webServerArray = "webServer1, webServer2".split(",");
        try {
            balancerManagerServiceImpl.findMatchWebServers(webServers, webServerArray);
            fail();
        } catch (Exception e) {
            assertEquals("com.cerner.jwala.common.exception.InternalErrorException: webServer2 cannot be found in the group", e.toString());
        }
    }

    @Test
    public void testCheckStatus() {
        final MockGroup mockGroup = new MockGroup();
        Set<Group> groups = new HashSet<>();
        groups.add(mockGroup.getGroup());
        WebServer webServer = mockGroup.getWebServer("myWebServerName");
        when(mockWebServerService.isStarted(webServer)).thenReturn(true);
        balancerManagerServiceImpl.checkStatus(webServer);
    }

    @Test
    public void testCheckStatusFail() {
        final MockGroup mockGroup = new MockGroup();
        Set<Group> groups = new HashSet<>();
        groups.add(mockGroup.getGroup());
        WebServer webServer = mockGroup.getWebServer("myWebServerName");
        when(mockWebServerService.isStarted(webServer)).thenReturn(false);
        try {
            balancerManagerServiceImpl.checkStatus(webServer);
        } catch (Exception e) {
            System.out.println(e.toString());
            assertEquals("com.cerner.jwala.common.exception.InternalErrorException: The target Web Server myWebSererName must be STARTED before attempting to drain users", e.toString());
        }
    }

    @Test
    public void testCheckGroupStatus() {
        final MockGroup mockGroup = new MockGroup();
        when(mockGroupService.getGroup("mygroupName")).thenReturn(mockGroup.getGroup());
        when(mockWebServerService.findWebServers(new Identifier<Group>(1L))).thenReturn(mockGroup.findWebServers());
        when(mockWebServerService.isStarted(any(WebServer.class))).thenReturn(true);
        balancerManagerServiceImpl.checkGroupStatus(mockGroup.getGroup().getName());
    }

    @Test
    public void testCheckGroupStatusFail() {
        final MockGroup mockGroup = new MockGroup();
        when(mockGroupService.getGroup("mygroupName")).thenReturn(mockGroup.getGroup());
        when(mockWebServerService.findWebServers(new Identifier<Group>(1L))).thenReturn(mockGroup.findWebServers());
        when(mockWebServerService.isStarted(any(WebServer.class))).thenReturn(false);
        try {
            balancerManagerServiceImpl.checkGroupStatus(mockGroup.getGroup().getName());
        } catch (Exception e) {
            assertEquals("com.cerner.jwala.common.exception.InternalErrorException: The target Web Server myWebServerName in group mygroupName must be STARTED before attempting to drain users", e.toString());
        }
    }

    @Test
    public void testDoHttpClientPostFail() {
        BalancerManagerHttpClient balancerManagerHttpClient = new BalancerManagerHttpClient();
        try {
            balancerManagerHttpClient.doHttpClientPost("https://localhost", getNvp());
            fail();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            assertEquals("org.apache.http.conn.HttpHostConnectException: Connect to localhost:443 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", e.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFindBalancers() {
        final String content = getBalancerManagerResponseHtml();
        Map<String, String> map = balancerManagerHtmlParser.findBalancers(content);
        assertEquals(1, map.size());
        assertEquals("7bbf520f-8454-7b47-8edc-d5ade6c31357", map.get("lb-health-check-4.0"));
    }

    @Test
    public void testFindBalancersMulti() {
        final String content = getBalancerManagerResponseHtmlMulti();
        Map<String, String> map = balancerManagerHtmlParser.findBalancers(content);
        assertEquals(3, map.size());
        assertEquals("6af7e4d6-531d-b343-95d4-99f2127609ad", map.get("lb-slpa-4.0.0800.02"));
        assertEquals("c7a709c3-8bc5-af49-b894-a57c7ac55519", map.get("lb-slpa-ws-4.0.0800.02"));
        assertEquals("a3f2dd40-8d20-e34e-9dec-6cfe264d87e2", map.get("ping"));
    }

    @Test
    public void testFindWorkerStatus() {
        assertEquals("Off", balancerManagerHtmlParser.getWorkerStatus(getWorkerHtml(), WorkerStatusType.IGNORE_ERRORS));
        assertEquals("On", balancerManagerHtmlParser.getWorkerStatus(getWorkerHtml(), WorkerStatusType.DRAINING_MODE));
        assertEquals("Off", balancerManagerHtmlParser.getWorkerStatus(getWorkerHtml(), WorkerStatusType.DISABLED));
        assertEquals("Off", balancerManagerHtmlParser.getWorkerStatus(getWorkerHtml(), WorkerStatusType.HOT_STANDBY));
    }

    @Test
    public void testFindApplicationNameByWorker() {
        final MockGroup mockGroup = new MockGroup();
        mockGroup.getGroup();
        when(mockApplicationService.getApplications()).thenReturn(mockGroup.getApplications());
        final String worker = "https://hostname:port/hct";
        assertEquals("HEALTH-CHECK-4.0", balancerManagerServiceImpl.findApplicationNameByWorker(worker));
    }

    @Test
    public void testFindApplicationNameByWorkerMulti(){
        final MockGroup mockGroup = new MockGroup();
        mockGroup.getGroup();
        when(mockApplicationService.getApplications()).thenReturn(mockGroup.getApplicationsMulti());
        final String worker = "https://usmlvv1cds0052:9111/slpa-test/slum/ws";
        assertEquals("SLPA-WS-4.0.0800.02", balancerManagerServiceImpl.findApplicationNameByWorker(worker));
    }

    @Test
    public void testFindJvmNameByWorker() {
        final MockGroup mockGroup = new MockGroup();
        mockGroup.getGroup();
        when(mockJvmService.getJvms()).thenReturn(mockGroup.getJvms());
        final String worker = "https://localhost:9101/mywebAppContext";
        assertEquals("jvmname", balancerManagerServiceImpl.findJvmNameByWorker(worker));
    }

    @Test
    public void testFindJvmNameByWorkerAJP(){
        final MockGroup mockGroup = new MockGroup();
        mockGroup.getGroup();
        when(mockJvmService.getJvms()).thenReturn(mockGroup.getJvms());
        final String worker = "ajp://localhost:9103/mywebAppContext";
        assertEquals("jvmname", balancerManagerServiceImpl.findJvmNameByWorker(worker));
    }

    @Test
    public void testFindJvmNameByWorkerNotFound(){
        final MockGroup mockGroup = new MockGroup();
        mockGroup.getGroup();
        when(mockJvmService.getJvms()).thenReturn(mockGroup.getJvms());
        final String worker = "xxxxx://localhost:9103/mywebAppContext";
        assertEquals("", balancerManagerServiceImpl.findJvmNameByWorker(worker));
    }

    private String getBalancerManagerResponseXml() {
        final File httpdconfFile = new File("./src/test/resources/balancermanager/balancer-manager-response.xml");
        String contents = "";
        try {
            byte[] bytes = Files.readAllBytes(httpdconfFile.toPath());
            contents = new String(bytes, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }

    private String getBalancerManagerResponseHtml() {
        final File file = new File("./src/test/resources/balancermanager/balancer-manager-response.html");
        String contents = "";
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            contents = new String(bytes, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }

    private String getBalancerManagerResponseXmlMulti() {
        final File file = new File("./src/test/resources/balancermanager/balancer-manager-response-multi.xml");
        String contents = "";
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            contents = new String(bytes, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }

    private String getBalancerManagerResponseHtmlMulti() {
        final File file = new File("./src/test/resources/balancermanager/balancer-manager-response-multi.html");
        String contents = "";
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            contents = new String(bytes, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }

    private List<NameValuePair> getNvp() {
        List<NameValuePair> nvp = new ArrayList<>();
        nvp.add(new BasicNameValuePair("a", "1"));
        return nvp;
    }

    private String getUser() {
        return "1";
    }

    private String getWorkerHtml() {
        final File file = new File("./src/test/resources/balancermanager/balancer-manager-response-worker.html");
        String contents = "";
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            contents = new String(bytes, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }
}
