package com.siemens.cto.deploy.http

import com.sun.xml.internal.ws.org.objectweb.asm.Item;

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

// see http://usmlvv1srn464/jenkins/userContent/tocapi/index.html 

public class TocClient {

    protected TocContext httpContext;
    protected HttpClient httpClient;

    TocClient(String host, String port, String username, String password) {
        this("http",host,port,username,password);
    }

    TocClient(String protocol, String host, String port, String username, String password) {
        println "Created new TocClient : protocol=$protocol, host=$host, port=$port, username=$username, password=$password"
        httpContext = new TocContext(protocol, host, port, username, password)
        httpClient = new HttpClient(httpContext)
    }

    public void login() {
        httpClient.login();
    }

    public int addWebApp(String contextPath, String appName) {
        println("Creating application ${appName}")
        def json = new JsonBuilder();
        json name: appName, webappContext: contextPath, groupId: httpContext.groupId

        def response = httpClient.execute(httpContext.urls.applicationsUrl, json.toString())
        def slurper = new JsonSlurper()
        def result = slurper.parseText(response);

        if(result.msgCode == "0" ) {
            return result.applicationResponseContent.id.id;
        }
        
        return -1; // this will never be reached, an error response will be an exception.      
    }

    public int addWebServer(String webServerName, String hostName, int httpPort, int httpsPort, String statusPath, String configPath, String svrRoot, String docRoot) {
        println("Creating web server ${webServerName}")

        def json = new JsonBuilder();
        json webserverName: webServerName, groupId: httpContext.groupId, hostName: hostName, portNumber: httpPort, httpsPort: httpsPort, statusPath: statusPath, httpConfigFile: configPath, svrRoot: svrRoot, docRoot: docRoot;

        def response = httpClient.execute(httpContext.urls.webServersUrl, "[${json.toString()}]");
        def slurper = new JsonSlurper()
        def result = slurper.parseText(response);

        if(result.msgCode == "0" ) {
            return result.applicationResponseContent.id.id;
        }
        
        return -1; // this will never be reached, an error response will be an exception.      
    }
    
    public void deleteWebServer(int webServerId) {
        httpClient.delete(httpContext.urls.webServersUrl+"/" + webServerId)
    }
    public void deleteJvm(int jvmId) {
        httpClient.delete(httpContext.urls.jvmsUrl+"/" + jvmId)
    }
    public void deleteApp(int appId) {
        httpClient.delete(httpContext.urls.applicationsUrl+"/" + appId)
    }
    public void deleteGroup(int groupId) {
        httpClient.delete(httpContext.urls.groupsUrl+"/" + groupId)
    }

    public int getOrCreateWebServer(String webServerName, String hostName, int httpPort, int httpsPort, String statusPath, String configPath, String svrRoot, String docRoot) {
        // there is no API for get by name, so will find it ourselves
        def ws = new JsonSlurper().parseText(httpClient.get(httpContext.urls.webServersUrl+"?all")).applicationResponseContent.findAll { it.name == webServerName; }

        if(ws.size() == 0)
        {
            def id = addWebServer(webServerName, hostName, httpPort, httpsPort, statusPath, configPath, svrRoot, docRoot)
            println("Created web server ${webServerName} exists, id ${id}")
            return id
        } else {
        
            def groupIds = ws[0].groupIds.collect({ [groupId:it.id] })
            if(groupIds.find({it.groupId == Integer.parseInt(httpContext.groupId)}) == null) {
                println("Editing web server ${webServerName}, id ${ws[0].id.id}, to add group ${httpContext.groupId}")            
                            
                def json = new JsonBuilder()
        
                groupIds.add([groupId:Integer.parseInt(httpContext.groupId)])        
                                        
                json webserverId: ws[0].id.id,
                     webserverName: ws[0].name, 
                     groupIds: groupIds,
                     hostName: ws[0].host, 
                     portNumber: ws[0].port, 
                     httpsPort: ws[0].httpsPort, 
                     statusPath: ws[0].statusPath.path, 
                     httpConfigFile: ws[0].httpConfigFile.path, 
                     svrRoot: ws[0].svrRoot.path, 
                     docRoot: ws[0].docRoot.path;
                     
                 httpClient.put(httpContext.urls.webServersUrl, "["+json.toString()+"]")
            }
            else {
                println("Utilizing web server ${webServerName}, id ${ws[0].id.id}")            
            }             
             return ws[0].id.id
        }
    }

    public int addJvm(String jvmName, String hostName, int httpPort, int httpsPort, int redirectPort, int shutdownPort, int ajpPort, String statusPath, String systemProperties) {
        println("Creating jvm ${jvmName}")

        def json = new JsonBuilder();
        json jvmName: jvmName, groupId: httpContext.groupId, hostName: hostName, httpPort: httpPort, httpsPort: httpsPort, redirectPort: redirectPort, shutdownPort: shutdownPort, ajpPort: ajpPort, statusPath: statusPath, systemProperties: systemProperties;

        def response = httpClient.execute(httpContext.urls.jvmsUrl, json.toString());
        def jvms = new JsonSlurper().parseText(response).applicationResponseContent
        def id = jvms.id.id;
        return id;
    }

    public int getOrCreateJvm(String jvmName, String hostName, int httpPort, int httpsPort, int redirectPort, int shutdownPort, int ajpPort, String statusPath, String systemProperties) {
        // there is no API for get by name, so will find it ourselves
        def jvms = new JsonSlurper().parseText(httpClient.get(httpContext.urls.jvmsUrl+"?all")).applicationResponseContent.findAll { it.jvmName == jvmName; }

        if(jvms.size() == 0)
        {
            return addJvm(jvmName, hostName, httpPort, httpsPort, redirectPort, shutdownPort, ajpPort, statusPath, systemProperties);
        } else {
            println("Jvm ${jvmName} exists, id ${jvms[0].id.id}")
            return jvms[0].id.id
        }
    }

    public int getOrCreateWebApp(String contextPath, String appName) {
        // there is no API for get by name, so will find it ourselves
        def apps = new JsonSlurper().parseText(httpClient.get(httpContext.urls.applicationsUrl+"?all")).applicationResponseContent.findAll { it.name == appName; }

        if(apps.size() == 0)
        {
            return addWebApp(contextPath, appName)
        } else {
            println("App ${appName} exists, id ${apps[0].id.id}")
            return apps[0].id.id
        }
    }

    public int getOrCreateGroup(String groupName) { 
        def groupsUrl = "${httpClient.httpContext.urls.groupsUrl}?name=${groupName}"
        println "url = ${groupsUrl}";
        def response = httpClient.get(groupsUrl);

        def slurper = new JsonSlurper()
        def result = slurper.parseText(response);

        if(result.msgCode == "0" && result.applicationResponseContent.size() > 0) {
            httpContext.groupId = result.applicationResponseContent[0].id.id;
            return result.applicationResponseContent[0].id.id;
        }
        
        // otherwise create
        response = httpClient.execute(httpClient.httpContext.urls.groupsUrl, groupName);
                
        result = slurper.parseText(response);
        httpContext.groupId = result.applicationResponseContent.id.id;
        println "groupId = ${httpContext.groupId}"
        return result.applicationResponseContent.id.id;
    }
    
    public void createGroup(String groupName) {
        String response = httpClient.execute(httpClient.httpContext.urls.groupsUrl, groupName);

        def slurper = new JsonSlurper()
        def result = slurper.parseText(response);
        httpContext.groupId = result.applicationResponseContent.id.id;
        println "groupId = ${httpContext.groupId}"
    }

    public String getGroups(String id = 'all') {
        def groupsUrl = httpClient.httpContext.urls.groupsUrl
        if (id && id != 'all') {
            groupsUrl += "/" + id;
        }
        println "url = ${groupsUrl}";
        httpClient.get(groupsUrl);
    }

    public Map<String,Integer> getJvmIdsForGroupAndServer(String groupName, String hostName) {
        def groupsUrl = "${httpClient.httpContext.urls.groupsUrl}?name=${groupName}"
        println "url = ${groupsUrl}";
        String response = httpClient.get(groupsUrl);

        def slurper = new JsonSlurper()
        def result = slurper.parseText(response);

        if(result.msgCode != "0" ) {
            return null;
        }
        
        def resultMap = new HashMap<String, Integer>();
        
        for(def jvm : result.applicationResponseContent[0].jvms) {
            if(jvm.hostName == hostName) {
                resultMap.put(jvm.jvmName, jvm.id.id)            
            }
        }
        println(resultMap)
        return resultMap;
    }

    public String getWebServers(String groupId) {
        def wsUrl = httpClient.httpContext.urls.webServersUrl + "?groupId=" + groupId;
        println "url = ${wsUrl}";
        httpClient.get(wsUrl);
    }

    public String getWebServerConfig(String name) {
        def wsUrl = httpClient.httpContext.urls.webServersUrl + "/${name}/conf?ssl=true";
        println "url = ${wsUrl}";
        httpClient.doGet(wsUrl);
    }
}
