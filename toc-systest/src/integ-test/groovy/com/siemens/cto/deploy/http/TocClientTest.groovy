package com.siemens.cto.deploy.http

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

/**
 * Test code for TocClient
 * Please modify the server name to a server that you have deployed TOC to in order to
 * run these tests.
 *
 * '*MayFail' tests may fail because they need pre-existing data on the TOC server
 * most likely, as deployed by a toc-x.y.z package.
 *
 * @author -the-tomcat-operations-center-team
 *
 */
class TocClientTest {

    TocClient tocHttpClient = new TocClient("https","localhost", "9101", "N9SFTomcatAdmin", "Healthcare@14");

    def wsId = -1;
    def wsId2 = -1;
    def jvmId = -1;
    def jvmId2 = -1;
    def appId = -1;
    def groupId = -1;
    def groupId1 = -1;
    def randGroupName;
    def randGroupName2;
    def randJvmName;
    
    @Before
    public void setUp() throws Exception {
        randGroupName = "TestGroup" + java.util.UUID.randomUUID().toString();
        randGroupName2 = "TestGroup" + java.util.UUID.randomUUID().toString();
        randJvmName = "TestJvm" + java.util.UUID.randomUUID().toString();
        groupId1 = tocHttpClient.getOrCreateGroup(randGroupName)
    }

    @After
    public void tearDown() throws Exception {     
        if(jvmId != -1) tocHttpClient.deleteJvm(jvmId);
        if(wsId != -1) tocHttpClient.deleteWebServer(wsId);
        if(wsId2 != -1) tocHttpClient.deleteWebServer(wsId2);
        if(appId != -1) tocHttpClient.deleteApp(appId);        
        if(groupId != -1) tocHttpClient.deleteGroup(groupId);

        //if(groupId1 != -1) tocHttpClient.deleteGroup(groupId1);
        //if(jvmId2 != -1) tocHttpClient.deleteJvm(jvmId2);        
    }

    @Test
    public void testCreateGroupIfExists() {
        def id = tocHttpClient.getOrCreateGroup(randGroupName)
        assertNotNull(id)
        def id2 = tocHttpClient.getOrCreateGroup(randGroupName)
        assertEquals(id,id2)
    }

    @Test
    @Ignore
    public void testGetJvmsForExistingGroupDashesMayFail() {
        tocHttpClient.getJvmIdsForGroupAndServer("HEALTH-CHECK-4.0","USMLVV1CTO3175");
    }

    @Test 
    @Ignore
    public void testGetJvmsForExistingGroupSpacesMayFail() {
        tocHttpClient.getJvmIdsForGroupAndServer("HEALTH%20CHECK%204.0","USMLVV1CTO3175");
    }

    @Test
    public void testCreateOneWS() {
        tocHttpClient.getOrCreateGroup(randGroupName)
        wsId = tocHttpClient.addWebServer("TEST-WS", "TESTHOST", 100, 101, "/stp.png", "/cygdrive/d/stp/apache-httpd-2.4.10/conf/httpd.conf", "testsvrroot", "testdocroot");
    }

    @Test
    public void testCreateTwoWS() {
        tocHttpClient.getOrCreateGroup(randGroupName)
        wsId = tocHttpClient.addWebServer("TEST-WS", "TESTHOST", 100, 101, "/stp.png", "/cygdrive/d/stp/apache-httpd-2.4.10/conf/httpd.conf", "testsvrroot", "testdocroot");
        wsId2 = tocHttpClient.addWebServer("TEST-WS2", "TESTHOST", 100, 101, "/stp.png", "/cygdrive/d/stp/apache-httpd-2.4.10/conf/httpd.conf", "testsvrroot", "testdocroot");
    }

    @Test
    public void testGetAndEditWS() {
        tocHttpClient.getOrCreateGroup(randGroupName)
        wsId = tocHttpClient.addWebServer("TEST-WS", "TESTHOST", 100, 101, "/stp.png", "/cygdrive/d/stp/apache-httpd-2.4.10/conf/httpd.conf", "testsvrroot", "testdocroot");
        tocHttpClient.getOrCreateGroup(randGroupName2)
        wsId2 = tocHttpClient.getOrCreateWebServer("TEST-WS", "TESTHOST", 100, 101, "/stp.png", "/cygdrive/d/stp/apache-httpd-2.4.10/conf/httpd.conf", "testsvrroot", "testdocroot");
        wsId2 = tocHttpClient.getOrCreateWebServer("TEST-WS", "TESTHOST", 100, 101, "/stp.png", "/cygdrive/d/stp/apache-httpd-2.4.10/conf/httpd.conf", "testsvrroot", "testdocroot");
    }

    @Test
    public void testGetOrCreateWS() {
        tocHttpClient.getOrCreateGroup(randGroupName)
        wsId = -1
        def id = tocHttpClient.getOrCreateWebServer("TEST-WS", "TESTHOST", 100, 101, "/stp.png", "/cygdrive/d/stp/apache-httpd-2.4.10/conf/httpd.conf", "testsvrroot", "testdocroot");
        wsId = id
        assertNotNull(id)
        def id2 = tocHttpClient.getOrCreateWebServer("TEST-WS", "TESTHOST", 100, 101, "/stp.png", "/cygdrive/d/stp/apache-httpd-2.4.10/conf/httpd.conf", "testsvrroot", "testdocroot");
        assertEquals(id,id2)
    }
    
    @Test public void testGetAndCreateJVM() {        
        groupId = tocHttpClient.getOrCreateGroup("TestGroup2")
        jvmId = tocHttpClient.addJvm("SF-SFET-TEST-MPI-DEVSRF2228-1", "TESTHOST", 100, 101, 102,-1,104, "/stp.png", "testsysprops");        
        jvmId2 = tocHttpClient.addJvm(randJvmName, "TESTHOST", 100, 101, 102,-1,104, "/stp.png", "testsysprops");                
        def id = tocHttpClient.getOrCreateJvm("SF-SFET-TEST-MPI-DEVSRF2228-1", "TESTHOST", 100, 101, 102,-1,104, "/stp.png", "testsysprops");
        def id2 = tocHttpClient.getOrCreateJvm(randJvmName, "TESTHOST", 100, 101, 102,-1,104, "/stp.png", "testsysprops");
        assertEquals(jvmId, id)
        assertEquals(jvmId2, id2)
    }

    @Test
    public void testCreateOneJVM() {
        tocHttpClient.getOrCreateGroup(randGroupName)
        jvmId = tocHttpClient.addJvm("TEST-JVM", "TESTHOST", 100, 101, 102,-1,104, "/stp.png", "testsysprops");        
    }

    @Test
    public void testGetOrCreateJVM() {
        tocHttpClient.getOrCreateGroup(randGroupName)
        jvmId = -1
        def id = tocHttpClient.getOrCreateJvm("TEST-JVM", "TESTHOST", 100, 101, 102,-1,104, "/stp.png", "testsysprops");
        assertNotNull(id)
        jvmId = id;
        tocHttpClient.getOrCreateJvm("TEST-JVM", "TESTHOST", 100, 101, 102,-1,104, "/stp.png", "testsysprops");
        def id2 = jvmId;
        assertEquals(id,id2)        
    }

    
    @Test(expected=RestException.class)
    public void testFailDuplicateWebServer() {
        testCreateOneWS()
        testCreateOneWS()
    }

    @Test
    public void testCreateOneApp() {
        tocHttpClient.getOrCreateGroup(randGroupName)
        appId = tocHttpClient.addWebApp("/test","testapp");
    }
    
    @Test(expected=RestException.class)
    public void testFailDuplicateApp() {
        testCreateOneApp()
        testCreateOneApp()
    }
    
    @Test
    public void testGetOrCreateAppTNTIN() {
        tocHttpClient.getOrCreateGroup(randGroupName)
        tocHttpClient.getOrCreateWebApp("/test","TNTIN-4.1");
        tocHttpClient.getOrCreateWebApp("/test","TNTIN-4.1");
    }

    @Test
    public void testJsonManipulation() {
        def ws = new JsonSlurper().parseText('[{"id":4}]');
        def json = new JsonBuilder()
        ws.add([id:5])        
        json test2:'c',groupIds:ws,test:'b';
        System.out.println(json.toString());
    }
    @Test
    public void testGetOrCreateApp() {
        tocHttpClient.getOrCreateGroup(randGroupName)
        appId = -1
        def id = tocHttpClient.getOrCreateWebApp("/test","testapp");
        appId = id
        assertNotNull(id)
        def id2 = tocHttpClient.getOrCreateWebApp("/test","testapp");
        assertEquals(id,id2)
    }

    @Test(expected=RestException.class)
    public void testFailDuplicateJvm() {
        try {
            testCreateOneJVM()
            testCreateOneJVM()
        } catch (RestException e) {
            println (e)
            throw e;
        }
    }

}
