package com.siemens.cto.deploy.http

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue 

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

    TocClient tocHttpClient = new TocClient("https","localhost", "9101", "N9SFGlabTomcatAdmin", "Passw0rd1");
    
    static def webappFolder = System.getProperty("STP_HOME")+"\\app\\webapps"
    static def jvmBase= 10300 + 10*(10.0 * Math.random()).toInteger() ;

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
    def randAppName;
    def randAppContext;
    def jvmHttp;
    def jvmHttps;
    def jvmAjp;
    def jvmShutdown = -1;

    
    @Before
    public void setUp() throws Exception {
        randGroupName = "TestGroup" + java.util.UUID.randomUUID().toString();
        randGroupName2 = "TestGroup" + java.util.UUID.randomUUID().toString();
        randJvmName = "TestJvm" + java.util.UUID.randomUUID().toString();
        randAppName = "TestApp" + java.util.UUID.randomUUID().toString();
        randAppContext = "/testApp";

                jvmHttp = jvmBase;
        jvmHttps = jvmBase+1
        jvmAjp = jvmBase+2
        jvmBase += 3;
        
    }

    @After
    public void tearDown() throws Exception {     
        if(jvmId != -1) try { tocHttpClient.deleteJvm(jvmId); } catch(Exception e) { println "Warning on cleanup: "+ e; /* ignore failure to clean up */ }
        if(wsId != -1) try { tocHttpClient.deleteWebServer(wsId); } catch(Exception e) { println "Warning on cleanup: "+ e; /* ignore failure to clean up */ }
        if(wsId2 != -1) try { tocHttpClient.deleteWebServer(wsId2); } catch(Exception e) { println "Warning on cleanup: "+ e; /* ignore failure to clean up */ }
        if(appId != -1) try { tocHttpClient.deleteApp(appId);         } catch(Exception e) { println "Warning on cleanup: "+ e; /* ignore failure to clean up */ }
        if(groupId != -1) try { tocHttpClient.deleteGroup(groupId); } catch(Exception e) { println "Warning on cleanup: "+ e; /* ignore failure to clean up */ }
        if(jvmId2 != -1) try { tocHttpClient.deleteJvm(jvmId2);         } catch(Exception e) { println "Warning on cleanup: "+ e; /* ignore failure to clean up */ }
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
        wsId = tocHttpClient.addWebServer("TEST-WS", "TESTHOST", jvmHttp, jvmHttps, "/stp.png", "/cygdrive/d/stp/apache-httpd-2.4.10/conf/httpd.conf", "testsvrroot", "testdocroot");
    }

    @Test
    public void testCreateTwoWS() {
        tocHttpClient.getOrCreateGroup(randGroupName)
        wsId = tocHttpClient.addWebServer("TEST-WS", "TESTHOST", jvmHttp, jvmHttps, "/stp.png", "/cygdrive/d/stp/apache-httpd-2.4.10/conf/httpd.conf", "testsvrroot", "testdocroot");
        wsId2 = tocHttpClient.addWebServer("TEST-WS2", "TESTHOST", jvmHttp, jvmHttps, "/stp.png", "/cygdrive/d/stp/apache-httpd-2.4.10/conf/httpd.conf", "testsvrroot", "testdocroot");
    }

    @Test
    public void testGetAndEditWS() {
        tocHttpClient.getOrCreateGroup(randGroupName)
        wsId = tocHttpClient.addWebServer("TEST-WS", "TESTHOST", jvmHttp, jvmHttps, "/stp.png", "/cygdrive/d/stp/apache-httpd-2.4.10/conf/httpd.conf", "testsvrroot", "testdocroot");
        tocHttpClient.getOrCreateGroup(randGroupName2)
        wsId2 = tocHttpClient.getOrCreateWebServer("TEST-WS", "TESTHOST", jvmHttp, jvmHttps, "/stp.png", "/cygdrive/d/stp/apache-httpd-2.4.10/conf/httpd.conf", "testsvrroot", "testdocroot");
        wsId2 = tocHttpClient.getOrCreateWebServer("TEST-WS", "TESTHOST", jvmHttp, jvmHttps, "/stp.png", "/cygdrive/d/stp/apache-httpd-2.4.10/conf/httpd.conf", "testsvrroot", "testdocroot");
    }

    @Test
    public void testGetOrCreateWS() {
        tocHttpClient.getOrCreateGroup(randGroupName)
        wsId = -1
        def id = tocHttpClient.getOrCreateWebServer("TEST-WS", "TESTHOST", jvmHttp, jvmHttps, "/stp.png", "/cygdrive/d/stp/apache-httpd-2.4.10/conf/httpd.conf", "testsvrroot", "testdocroot");
        wsId = id
        assertNotNull(id)
        def id2 = tocHttpClient.getOrCreateWebServer("TEST-WS", "TESTHOST", jvmHttp, jvmHttps, "/stp.png", "/cygdrive/d/stp/apache-httpd-2.4.10/conf/httpd.conf", "testsvrroot", "testdocroot");
        assertEquals(id,id2)
    }
    
    @Test public void testGetAndCreateJVM() {        
        groupId = tocHttpClient.getOrCreateGroup("TestGroup2")
        jvmId = tocHttpClient.addJvm("SF-SFET-TEST-MPI-DEVSRF2228-1", "TESTHOST", jvmHttp, jvmHttps, jvmHttps,jvmShutdown,jvmAjp, "/stp.png", "testsysprops");        
        jvmId2 = tocHttpClient.addJvm(randJvmName, "TESTHOST", jvmHttp, jvmHttps, jvmHttps,jvmShutdown,jvmAjp, "/stp.png", "testsysprops");                
        def id = tocHttpClient.getOrCreateJvm("SF-SFET-TEST-MPI-DEVSRF2228-1", "TESTHOST", jvmHttp, jvmHttps, jvmHttps,jvmShutdown,jvmAjp, "/stp.png", "testsysprops");
        def id2 = tocHttpClient.getOrCreateJvm(randJvmName, "TESTHOST", jvmHttp, jvmHttps, jvmHttps,jvmShutdown,jvmAjp, "/stp.png", "testsysprops");
        assertEquals(jvmId, id)
        assertEquals(jvmId2, id2)
    }

    @Test
    public void testCreateOneJVM() {
        tocHttpClient.getOrCreateGroup(randGroupName)
        jvmId = tocHttpClient.addJvm("TEST-JVM", "TESTHOST", jvmHttp, jvmHttps, jvmHttps,jvmShutdown,jvmAjp, "/stp.png", "testsysprops");        
    }
   
    @Test(expected=RestException.class)
    public void testStartMissingJVM() {
        tocHttpClient.getOrCreateGroup(randGroupName)
        jvmId = tocHttpClient.addJvm(randJvmName, "TESTHOST", jvmHttp, jvmHttps, jvmHttps,jvmShutdown,jvmAjp, "/stp.png", "testsysprops");
        println ('Jvm attempt to start')
        tocHttpClient.getV1JvmClient().start(randJvmName);
    }

    @Test(expected=RestException.class)
    public void testStopMissingJVM() {
        tocHttpClient.getOrCreateGroup(randGroupName)
        jvmId = tocHttpClient.addJvm(randJvmName, "TESTHOST", jvmHttp, jvmHttps, jvmHttps,jvmShutdown,jvmAjp, "/stp.png", "testsysprops");
        println ('Jvm attempt to stop')
        tocHttpClient.getV1JvmClient().stop(randJvmName);
    }

    @Test
    public void testGetOrCreateJVM() {
        tocHttpClient.getOrCreateGroup(randGroupName)
        jvmId = -1
        def id = tocHttpClient.getOrCreateJvm("TEST-JVM", "TESTHOST", jvmHttp, jvmHttps, jvmHttps,jvmShutdown,jvmAjp, "/stp.png", "testsysprops");
        assertNotNull(id)
        jvmId = id;
        tocHttpClient.getOrCreateJvm("TEST-JVM", "TESTHOST", jvmHttp, jvmHttps, jvmHttps,jvmShutdown,jvmAjp, "/stp.png", "testsysprops");
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

    
    @Test
    public void testDeployOneJVM() {
        try {
            def groupId = tocHttpClient.getV1GroupClient().getOrCreateGroup(randGroupName)
            jvmId = tocHttpClient.getV1JvmClient().addJvm(randJvmName, "localhost", jvmHttp, jvmHttps, jvmHttps, jvmShutdown,jvmAjp, "/stp.png;scheme=https", "testsysprops", groupId);
            tocHttpClient.getV1JvmClient().deployJvmInstance(randJvmName)
        } catch (RestException e) {
            println (e)
            throw e;
        }
    }

    @Test
    public void testDeployOneJVMandApplication() {
        try {
            def groupId = tocHttpClient.getOrCreateGroup(randGroupName)
            jvmId = tocHttpClient.addJvm(randJvmName, "localhost", jvmHttp, jvmHttps, jvmHttps,jvmShutdown,jvmAjp, "/stp.png;scheme=https", "testsysprops");
            tocHttpClient.deployJvmInstance(randJvmName);
            def appId = tocHttpClient.getV1WebAppClient().addWebApp(randAppContext, randAppName, groupId, true, false);            
            tocHttpClient.getV1WebAppClient().deployApplicationToInstance(randAppName, randAppContext, randGroupName, randJvmName)
        } catch (RestException e) {
            println (e)
            throw e;
        }
    }
    
    @Test
    public void testDeployOneJVMWithCustomServerXml() {
        try {
            def serverXmlTemplateFile = new File('resources/server-CTO-N9SF-LTST-1.tpl')
            assertTrue(serverXmlTemplateFile.exists());           
            def serverXmlTemplate = serverXmlTemplateFile.getText();
            def groupId = tocHttpClient.getOrCreateGroup(randGroupName)
            jvmId = tocHttpClient.addJvm(randJvmName, "localhost", jvmHttp, jvmHttps, jvmHttps,jvmShutdown,jvmAjp, "/stp.png;scheme=https", "testsysprops");
            tocHttpClient.getV1JvmClient().updateJvmTemplate(randJvmName, "server.xml", serverXmlTemplateFile.text)
            tocHttpClient.deployJvmInstance(randJvmName);
            def appId = tocHttpClient.getV1WebAppClient().addWebApp(randAppContext, randAppName, groupId, true, false);
            tocHttpClient.getV1WebAppClient().deployApplicationToInstance(randAppName, randAppContext, randGroupName, randJvmName)
            println ('Successfully deployed')
        } catch (RestException e) {
            println (e)
            throw e;
        }
    }
    
    @Test
    public void testDeployHealthcheck() {
        try {           
            def serverXmlTemplateFile = new File('resources/server-CTO-N9SF-LTST-1.tpl')
            assertTrue(serverXmlTemplateFile.exists());
            def serverXmlTemplate = serverXmlTemplateFile.getText();
            def groupId = tocHttpClient.getOrCreateGroup(randGroupName)
            jvmId = tocHttpClient.addJvm(randJvmName, "localhost", jvmHttp, jvmHttps, jvmHttps,jvmShutdown,jvmAjp, "/stp.png;scheme=https", "testsysprops");
            tocHttpClient.getV1JvmClient().updateJvmTemplate(randJvmName, "server.xml", serverXmlTemplateFile.text)
            tocHttpClient.deployJvmInstance(randJvmName);
            def appId = tocHttpClient.getV1WebAppClient().addWebApp("/cluster", randAppName, groupId, true, false);
            tocHttpClient.getV1WebAppClient().updateApplicationXml(randAppName, "cluster", randGroupName, randJvmName, new File('resources/cluster.tpl').text)
            def result = tocHttpClient.getV1WebAppClient().uploadWebArchive(randAppName,new File('resources/cluster.war'));
            def warPath = result.warPath;

            // copy war file for test purposes.
            def ant = new AntBuilder()

            ant.sequential {
                copy(file: warPath, toDir: webappFolder, overwrite: 'true', force: 'true')
            }
            tocHttpClient.getV1WebAppClient().deployApplicationToInstance(randAppName, "/cluster", randGroupName, randJvmName)
            println ('Cluster test application and JVM deployed')
        } catch (RestException e) {
            println (e)
            throw e;
        }
    }

    @Test
    public void testUploadArchive() {
        try {                   
            def groupId = tocHttpClient.getV1GroupClient().getOrCreateGroup(randGroupName)
            def appId = tocHttpClient.getV1WebAppClient().addWebApp(randAppContext, randAppName, groupId, true, false);
            def result = tocHttpClient.getV1WebAppClient().uploadWebArchive(randAppName,new File('resources/cluster.war'));
            assertNotNull(result.warPath)
            assertTrue(result.warPath.indexOf("cluster") != -1)
            println ('Successfully uploaded')
        } catch (RestException e) {
            println (e)
            throw e;        
        }
    }    
    
    @Test
    public void testDeployHealthcheckAndStartStop() {
        try {
            testDeployHealthcheck();
            tocHttpClient.getV1JvmClient().start(randJvmName);
            println ('Cluster test application and JVM deployed and started')
            try {
                tocHttpClient.getV1JvmClient().checkJvm(randJvmName);
                println ('JVM check passed.')
                tocHttpClient.getV1WebAppClient().checkApp(randJvmName, randAppName);
                println ('App check passed.')
            } finally {
                println ('Jvm stopping')
                tocHttpClient.getV1JvmClient().stop(randJvmName);
                println ('Jvm stopped')
            }
        } catch (RestException e) {
            println (e)
            throw e;
        }
    }
}
