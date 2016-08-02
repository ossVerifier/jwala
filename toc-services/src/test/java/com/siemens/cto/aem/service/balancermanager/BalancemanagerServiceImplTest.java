package com.siemens.cto.aem.service.balancermanager;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.resource.ResourceGroup;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerState;
import com.siemens.cto.aem.common.domain.model.webserver.message.WebServerHistoryEvent;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.ExecReturnCode;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.service.MessagingService;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.service.balancermanager.impl.BalancemanagerHttpClient;
import com.siemens.cto.aem.service.balancermanager.impl.BalancermanagerServiceImpl;
import com.siemens.cto.aem.service.balancermanager.impl.xml.data.Manager;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.ssl.hc.HttpClientRequestFactory;
import com.siemens.cto.aem.service.webserver.WebServerCommandService;

import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.service.webserver.component.ClientFactoryHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.*;

import static org.junit.Assert.assertEquals;
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

    private ClientFactoryHelper clientFactoryHelper = new ClientFactoryHelper();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.balancermanagerServiceImpl = new BalancermanagerServiceImpl(mockGroupService, mockApplicationService, mockWebServerService, mockMessagingService,
                clientFactoryHelper) {
            @Override
            public String getHttpdConffromResource(final String webServerName) {
                return getTestHttpConf();
            }

            @Override
            public void sendMessage(final Identifier<WebServer> id, final String message) {

            }
        };
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");

    }

    @After
    public void tearDown() {
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testGetBalanceMember() {
        File httpdconfFile = new File("./src/test/resources/balancermanager/httpd.conf");
        String contents = "";
        try {
            byte[] bytes = Files.readAllBytes(httpdconfFile.toPath());
            contents = new String(bytes, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(6, balancermanagerServiceImpl.getBalanceMembers(contents, "HEALTH-CHECK-4.0").size());
    }

    @Test
    public void testGetBalanceMemberMultiProxy() {
        File httpdconfFile = new File("./src/test/resources/balancermanager/multiProxy_httpd.conf");
        String contents = "";
        try {
            byte[] bytes = Files.readAllBytes(httpdconfFile.toPath());
            contents = new String(bytes, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(2, balancermanagerServiceImpl.getBalanceMembers(contents, "SLPA-WS-4.0.0800.01").size());
    }

    @Test
    public void testGetMatchJvm() {
        Set<String> set1 = new HashSet<>();
        set1.add("https://USMLVV1CDS0049:9101/hct");
        set1.add("https://USMLVV1CDS0049:9111/hct");
        Set<String> set2 = new HashSet<>();
        set2.add("https://USMLVV1CDS0049:9101/hct");
        set2.add("https://USMLVV1CDS0049:9111/hct");
        set2.add("https://USMLVV1CDS0049:9121/hct");
        Set<String> returnSet = balancermanagerServiceImpl.getMatchJvm(set1, set2);
        for (String set : returnSet) {
            System.out.println(set);
        }
        assertEquals(2, returnSet.size());
        Set<String> set3 = new HashSet<>();
        set3.add("https://USMLVV1CDS0049:9101/hct");
        set3.add("https://USMLVV1CDS0049:9111/hct");
        set3.add("https://USMLVV1CDS0049:9121/hct");
        Set<String> set4 = new HashSet<>();
        set4.add("https://USMLVV1CDS0049:9101/hct");
        set4.add("https://USMLVV1CDS0049:9111/hct");
        returnSet = balancermanagerServiceImpl.getMatchJvm(set3, set4);
        for (String set : returnSet) {
            System.out.println(set);
        }
        assertEquals(2, returnSet.size());
    }

    @Test
    public void testGetPostMap() {
        Map<String, String> map = balancermanagerServiceImpl.getPostMap("health-check-4.0", "https://usmlvv1cds0049:9101/hct");
        assertEquals(3, map.size());
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println(pair.getKey() + " " + pair.getValue());
            it.remove();
        }
    }

    @Test
    public void testDrainUserGroup() {
        final MockGroup mockGroup = new MockGroup();
        when(mockGroupService.getGroup("mygroupName")).thenReturn(mockGroup.getGroup());
        when(mockApplicationService.findApplications(new Identifier<Group>(1L))).thenReturn(mockGroup.findApplications());
        when(mockWebServerService.findWebServers(new Identifier<Group>(1L))).thenReturn(mockGroup.findWebServers());
        Map map = new HashMap<>();
        map.put("a", "1");
        BalancemanagerHttpClient mockBalancemanagerHttpClient = org.mockito.Mockito.mock(BalancemanagerHttpClient.class);
        when(mockBalancemanagerHttpClient.doHttpClientPost("http://localhost", map)).thenReturn(200);
        balancermanagerServiceImpl.drainUserGroup("mygroupName");
    }

    @Test
    public void testDrainUserWebServer() {
        final MockGroup mockGroup = new MockGroup();
        when(mockGroupService.getGroup("mygroupName")).thenReturn(mockGroup.getGroup());
        when(mockApplicationService.findApplications(new Identifier<Group>(1L))).thenReturn(mockGroup.findApplications());
        when(mockWebServerService.findWebServers(new Identifier<Group>(1L))).thenReturn(mockGroup.findWebServers());
        when(mockWebServerService.getWebServer("myWebServerName")).thenReturn(mockGroup.getWebServer("myWebServerName"));
        Map map = new HashMap<>();
        map.put("a", "1");
        BalancemanagerHttpClient mockBalancemanagerHttpClient = org.mockito.Mockito.mock(BalancemanagerHttpClient.class);
        when(mockBalancemanagerHttpClient.doHttpClientPost("http://localhost", map)).thenReturn(200);
        balancermanagerServiceImpl.drainUserWebServer("mygroupName", "myWebServerName");
    }

    private String getTestHttpConf() {
        File httpdconfFile = new File("./src/test/resources/balancermanager/httpd.conf");
        String contents = "";
        try {
            byte[] bytes = Files.readAllBytes(httpdconfFile.toPath());
            contents = new String(bytes, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }

    /*@Test
    public void testGetBalancerManager() {
        try {
            ClientFactoryHelper clientFactoryHelper = new ClientFactoryHelper();
            System.out.println(clientFactoryHelper.requestGet(new URI("https://usmlvv1cds0057/balancer-manager")).toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }*/

    @Test
    public void testGetWorkerStatus(){
        final File httpdconfFile = new File("./src/test/resources/balancermanager/balancer-Manager-response.html");
        String contents = "";
        try {
            byte[] bytes = Files.readAllBytes(httpdconfFile.toPath());
            contents = new String(bytes, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        final String appName = "health-check-4.0";
        Set<String> workers = new HashSet<>();
        workers.add("https://usmlvv1cds0057:9101/hct");
        workers.add("https://usmlvv1cds0057:9111/hct");
        workers.add("https://usmlvv1cds0057:9121/hct");
        workers.add("https://usmlvv1cds0058:9101/hct");
        workers.add("https://usmlvv1cds0058:9111/hct");
        workers.add("https://usmlvv1cds0058:9121/hct");
        balancermanagerServiceImpl.getWorkerStatus(contents, appName, workers);
    }

    @Test
    public void testGetWorkerXml(){
        final File httpdconfFile = new File("./src/test/resources/balancermanager/balancer-Manager-response.xml");
        String contents = "";
        try {
            byte[] bytes = Files.readAllBytes(httpdconfFile.toPath());
            contents = new String(bytes, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        final String appName = "health-check-4.0";
        Manager manager = balancermanagerServiceImpl.getWorkerXml(contents, null, null);
        assertEquals(2, manager.getBalancers().size());
    }
}
