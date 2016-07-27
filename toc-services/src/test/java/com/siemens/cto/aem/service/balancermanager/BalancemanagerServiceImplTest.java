package com.siemens.cto.aem.service.balancermanager;

import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.ExecReturnCode;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.service.balancermanager.impl.BalancemanagerHttpClient;
import com.siemens.cto.aem.service.balancermanager.impl.BalancermanagerServiceImpl;
import com.siemens.cto.aem.service.group.GroupService;
import com.siemens.cto.aem.service.webserver.WebServerCommandService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class BalancemanagerServiceImplTest {

    private BalancermanagerServiceImpl balancermanagerServiceImpl;

    @Mock
    private GroupService mockGroupService;

    @Mock
    private WebServerCommandService mockWebServerCommandService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.balancermanagerServiceImpl = new BalancermanagerServiceImpl(mockGroupService, mockWebServerCommandService);
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
        Identifier<WebServer> testWebServerId = new Identifier<>((long) 1);
        try {
            CommandOutput commandOutput = new CommandOutput(new ExecReturnCode(0), getTestHttpConf(), "");
            when(mockWebServerCommandService.getHttpdConf(testWebServerId)).thenReturn(commandOutput);
        } catch (CommandFailureException e) {
            e.printStackTrace();
        }
        Map map = new HashMap<String, String>();
        map.put("a","1");
        BalancemanagerHttpClient mockBalancemanagerHttpClient = org.mockito.Mockito.mock(BalancemanagerHttpClient.class);
        when(mockBalancemanagerHttpClient.doHttpClientPost("http://localhost", map)).thenReturn(200);
        assertEquals(HttpStatus.OK, balancermanagerServiceImpl.drainUserGroup("mygroupName"));
    }

    private String getTestHttpConf(){
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

}
