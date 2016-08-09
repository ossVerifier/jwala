package com.siemens.cto.aem.service.balancermanager;

import com.siemens.cto.aem.common.domain.model.balancermanager.DrainStatus;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.service.HistoryService;
import com.siemens.cto.aem.service.MessagingService;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.service.balancermanager.impl.BalancemanagerHttpClient;
import com.siemens.cto.aem.service.balancermanager.impl.BalancermanagerServiceImpl;
import com.siemens.cto.aem.service.balancermanager.impl.xml.data.Manager;
import com.siemens.cto.aem.service.group.GroupService;

import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.service.webserver.component.ClientFactoryHelper;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.siemens.cto.aem.common.domain.model.id.Identifier.id;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class BalancemanagerServiceImplTest {

    private BalancermanagerServiceImpl balancermanagerServiceImpl;

    @Mock
    private GroupService mockGroupService;

    @Mock
    private ApplicationService mockApplicationService;

    @Mock
    private WebServerService mockWebServerService;

    @Mock
    private MessagingService mockMessagingService;

    @Mock
    private ClientFactoryHelper mockClientFactoryHelper;

    @Mock
    private HistoryService mockHistoryService;

    @Mock
    private CloseableHttpResponse mockResponse;

    @Mock
    private BalancemanagerHttpClient mockBalancemanagerHttpClient;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.balancermanagerServiceImpl = new BalancermanagerServiceImpl(mockGroupService, mockApplicationService, mockWebServerService, mockClientFactoryHelper,
                mockMessagingService, mockHistoryService, mockBalancemanagerHttpClient) {
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
        assertEquals(4, balancermanagerServiceImpl.getNVP("myWorkerUrl").size());
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
        when(mockClientFactoryHelper.requestGet(any(URI.class))).thenReturn(mockResponseHtml);
        when(mockResponseHtml.getBody()).thenReturn(new ByteArrayInputStream(getBalancerManagerResponseHtml().getBytes()));
        ClientHttpResponse mockResponseXml = mock(ClientHttpResponse.class);
        when(mockResponseXml.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost/balancer-manager?b=lb-health-check-4.0&xml=1&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357"))).thenReturn(mockResponseXml);
        when(mockResponseXml.getBody()).thenReturn(new ByteArrayInputStream(getBalancerManagerResponseXml().getBytes()));
        ClientHttpResponse mockResponseXml2 = mock(ClientHttpResponse.class);
        when(mockResponseXml2.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockClientFactoryHelper.requestGet(new URI("https://localhost2/balancer-manager?b=lb-health-check-4.0&xml=1&nonce=7bbf520f-8454-7b47-8edc-d5ade6c31357"))).thenReturn(mockResponseXml2);
        when(mockResponseXml2.getBody()).thenReturn(new ByteArrayInputStream(getBalancerManagerResponseXml().getBytes()));
        when(mockBalancemanagerHttpClient.doHttpClientPost(any(String.class), anyListOf(NameValuePair.class))).thenReturn(mockResponse);
        DrainStatus drainStatus = balancermanagerServiceImpl.drainUserGroup("mygroupName", "");
        assertEquals(2, drainStatus.getWebServerDrainStatusList().size());

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
        when(mockBalancemanagerHttpClient.doHttpClientPost(any(String.class), anyListOf(NameValuePair.class))).thenReturn(mockResponse);
        DrainStatus drainStatus = balancermanagerServiceImpl.drainUserWebServer("mygroupName", "myWebServerName");
        assertEquals(1, drainStatus.getWebServerDrainStatusList().size());
    }

    @Test
    public void testGetWorkerStatus() {
        final String appName = "health-check-4.0";
        Map<String, String> workers = new HashMap<>();
        workers.put("https://usmlvv1cds0057:9101/hct", "CTO-N9SF-LTST-HEALTH-CHECK-4.0-USMLVV1CDS0057-1");
        workers.put("https://usmlvv1cds0057:9111/hct", "CTO-N9SF-LTST-HEALTH-CHECK-4.0-USMLVV1CDS0057-2");
        workers.put("https://usmlvv1cds0057:9121/hct", "CTO-N9SF-LTST-HEALTH-CHECK-4.0-USMLVV1CDS0057-3");
        workers.put("https://usmlvv1cds0058:9101/hct", "CTO-N9SF-LTST-HEALTH-CHECK-4.0-USMLVV1CDS0058-1");
        workers.put("https://usmlvv1cds0058:9111/hct", "CTO-N9SF-LTST-HEALTH-CHECK-4.0-USMLVV1CDS0058-2");
        workers.put("https://usmlvv1cds0058:9121/hct", "CTO-N9SF-LTST-HEALTH-CHECK-4.0-USMLVV1CDS0058-3");
        List<DrainStatus.WebServerDrainStatus.JvmDrainStatus> jvmDrainStatusList = balancermanagerServiceImpl.getJvmsWorkerStatus(getBalancerManagerResponseHtml(), appName, workers);
        assertEquals(6, jvmDrainStatusList.size());
    }

    @Test
    public void testGetWorkerXml() {
        Manager manager = balancermanagerServiceImpl.getWorkerXml(getBalancerManagerResponseXml());
        assertEquals(2, manager.getBalancers().size());
    }

    @Test
    public void testGetWorkers() {
        Manager manager = balancermanagerServiceImpl.getWorkerXml(getBalancerManagerResponseXml());
        balancermanagerServiceImpl.setBalancerName("lb-health-check-4.0");
        Map<String, String> workers = balancermanagerServiceImpl.getWorkers(manager);
        assertEquals(6, workers.size());
    }

    @Test
    public void testGetWorkersMulti() {
        Manager manager = balancermanagerServiceImpl.getWorkerXml(getBalancerManagerResponseXmlMulti());
        balancermanagerServiceImpl.setBalancerName("lb-slpa-4.0.0800.02");
        Map<String, String> workers = balancermanagerServiceImpl.getWorkers(manager);
        assertEquals(2, workers.size());
    }

    @Test
    public void testGetNonce() {
        final String content = getBalancerManagerResponseHtml();
        balancermanagerServiceImpl.setBalancerName("lb-health-check-4.0");
        balancermanagerServiceImpl.findNonce(content);
        assertEquals("7bbf520f-8454-7b47-8edc-d5ade6c31357", balancermanagerServiceImpl.getNonce());
    }

    @Test
    public void testGetNonceMulti() {
        final String content = getBalancerManagerResponseHtmlMulti();
        balancermanagerServiceImpl.setBalancerName("lb-slpa-4.0.0800.02");
        balancermanagerServiceImpl.findNonce(content);
        assertEquals("6af7e4d6-531d-b343-95d4-99f2127609ad", balancermanagerServiceImpl.getNonce());
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
        DrainStatus drainStatus = balancermanagerServiceImpl.getGroupDrainStatus("mygroupName");
        System.out.println(drainStatus);
        assertEquals(2, drainStatus.getWebServerDrainStatusList().size());
        for (DrainStatus.WebServerDrainStatus webServerDrainStatus : drainStatus.getWebServerDrainStatusList()) {
            System.out.println(webServerDrainStatus.getWebServerName());
            assertEquals(6, webServerDrainStatus.getJvmDrainStatusList().size());
            for (DrainStatus.WebServerDrainStatus.JvmDrainStatus jvmDrainStatus : webServerDrainStatus.getJvmDrainStatusList()) {
                System.out.println(jvmDrainStatus.getJvmName() + " " + jvmDrainStatus.getDrainStatus());
            }
        }
    }

    @Test
    public void testManager() {
        Manager manager = balancermanagerServiceImpl.getWorkerXml(getBalancerManagerResponseXml());
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
        assertEquals(0, balancermanagerServiceImpl.getRequireWebServers(emptyString).length);
        final String goodString1 = "webSever1";
        assertEquals(1, balancermanagerServiceImpl.getRequireWebServers(goodString1).length);
        final String goodString2 = "webServer1, webServer2";
        assertEquals(2, balancermanagerServiceImpl.getRequireWebServers(goodString2).length);
        final String goodString3 = "asdfafafa afafadfafaf;adfafafdaf";
        assertEquals(1, balancermanagerServiceImpl.getRequireWebServers(goodString3).length);
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
        assertEquals(2, balancermanagerServiceImpl.findMatchWebServers(webServers, webServerArray).size());
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
        assertEquals(0, balancermanagerServiceImpl.findMatchWebServers(webServers, webServerArray).size());
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
        assertEquals(1, balancermanagerServiceImpl.findMatchWebServers(webServers, webServerArray).size());
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
        assertEquals(1, balancermanagerServiceImpl.findMatchWebServers(webServers, webServerArray).size());
    }

    @Test
    public void testCheckStatus() {
        final MockGroup mockGroup = new MockGroup();
        Set<Group> groups = new HashSet<>();
        groups.add(mockGroup.getGroup());
        WebServer webServer = mockGroup.getWebServer("myWebServerName");
        when(mockWebServerService.isStarted(webServer)).thenReturn(true);
        balancermanagerServiceImpl.checkStatus(webServer);
    }

    @Test
    public void testCheckStatusFail() {
        final MockGroup mockGroup = new MockGroup();
        Set<Group> groups = new HashSet<>();
        groups.add(mockGroup.getGroup());
        WebServer webServer = mockGroup.getWebServer("myWebServerName");
        when(mockWebServerService.isStarted(webServer)).thenReturn(false);
        try {
            balancermanagerServiceImpl.checkStatus(webServer);
        } catch (Exception e) {
            System.out.println(e.toString());
            assertEquals("com.siemens.cto.aem.common.exception.InternalErrorException: The target Web Server myWebSererName must be start before attempting to drain user", e.toString());
        }
    }

    @Test
    public void testCheckGroupStatus() {
        final MockGroup mockGroup = new MockGroup();
        when(mockGroupService.getGroup("mygroupName")).thenReturn(mockGroup.getGroup());
        when(mockWebServerService.findWebServers(new Identifier<Group>(1L))).thenReturn(mockGroup.findWebServers());
        when(mockWebServerService.isStarted(any(WebServer.class))).thenReturn(true);
        balancermanagerServiceImpl.checkGroupStatus(mockGroup.getGroup().getName());
    }

    @Test
    public void testCheckGroupStatusFail() {
        final MockGroup mockGroup = new MockGroup();
        when(mockGroupService.getGroup("mygroupName")).thenReturn(mockGroup.getGroup());
        when(mockWebServerService.findWebServers(new Identifier<Group>(1L))).thenReturn(mockGroup.findWebServers());
        when(mockWebServerService.isStarted(any(WebServer.class))).thenReturn(false);
        try {
            balancermanagerServiceImpl.checkGroupStatus(mockGroup.getGroup().getName());
        } catch (Exception e) {
            assertEquals("com.siemens.cto.aem.common.exception.InternalErrorException: The target Web Server myWebServerName in group mygroupName must be start before attempting to drain user", e.toString());
        }
    }

    @Test
    public void testGetBalancerName() {
        final String content = getBalancerManagerResponseHtml();
        final String appName = "health-check-4.0";
        balancermanagerServiceImpl.findBalancerName(content, appName);
        assertEquals("lb-health-check-4.0", balancermanagerServiceImpl.getBalancerName());
    }

    @Test
    public void testGetBalancerNameMulti() {
        final String content = getBalancerManagerResponseHtmlMulti();
        final String appName = "slpa-4.0.0800.02";
        balancermanagerServiceImpl.findBalancerName(content, appName);
        assertEquals("lb-slpa-4.0.0800.02", balancermanagerServiceImpl.getBalancerName());
    }

    @Test
    public void testDoHttpClientPost()  {
        BalancemanagerHttpClient balancemanagerHttpClient = new BalancemanagerHttpClient();
        try {
            balancemanagerHttpClient.doHttpClientPost("https://localhost", getNvp());
            fail();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            assertEquals("org.apache.http.conn.HttpHostConnectException: Connect to localhost:443 [localhost/127.0.0.1, localhost/0:0:0:0:0:0:0:1] failed: Connection refused: connect", e.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
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
        final File httpdconfFile = new File("./src/test/resources/balancermanager/balancer-manager-response.html");
        String contents = "";
        try {
            byte[] bytes = Files.readAllBytes(httpdconfFile.toPath());
            contents = new String(bytes, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }

    private String getBalancerManagerResponseXmlMulti() {
        final File httpdconfFile = new File("./src/test/resources/balancermanager/balancer-manager-response-multi.xml");
        String contents = "";
        try {
            byte[] bytes = Files.readAllBytes(httpdconfFile.toPath());
            contents = new String(bytes, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }

    private String getBalancerManagerResponseHtmlMulti() {
        final File httpdconfFile = new File("./src/test/resources/balancermanager/balancer-manager-response-multi.html");
        String contents = "";
        try {
            byte[] bytes = Files.readAllBytes(httpdconfFile.toPath());
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
}
