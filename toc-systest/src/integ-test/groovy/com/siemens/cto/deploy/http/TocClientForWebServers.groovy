package com.siemens.cto.deploy.http

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

/**
 * Created by z003e5zv on 4/7/2015.
 */
class TocClientForWebServers extends AbstractTocClient {

    TocClientForWebServers(TocHttpClient httpClient) {
        super(httpClient, "webservers");
    }

    public int addWebServer(String webServerName, String hostName, int httpPort, int httpsPort, String statusPath, String configPath, String svrRoot, String docRoot, int groupId) {
        println("Creating web server ${webServerName}")

        def json = new JsonBuilder();
        json webserverName: webServerName, groupId: groupId, hostName: hostName, portNumber: httpPort, httpsPort: httpsPort, statusPath: statusPath, httpConfigFile: configPath, svrRoot: svrRoot, docRoot: docRoot;

        def response = tocHttpClient.execute(getV1Url(), "[${json.toString()}]");
        def slurper = new JsonSlurper()
        def result = slurper.parseText(response);

        if(result.msgCode == "0" ) {
            return result.applicationResponseContent.id.id;
        }

        return -1; // this will never be reached, an error response will be an exception.
    }
    public int getOrCreateWebServer(String webServerName, String hostName, int httpPort, int httpsPort, String statusPath, String configPath, String svrRoot, String docRoot, int cachedGroupId) {
        // there is no API for get by name, so will find it ourselves
        def ws = new JsonSlurper().parseText(tocHttpClient.get(getV1Url() + "?all")).applicationResponseContent.findAll { it.name == webServerName; }

        if(ws.size() == 0)
        {
            def id = addWebServer(webServerName, hostName, httpPort, httpsPort, statusPath, configPath, svrRoot, docRoot)
            println("Created web server ${webServerName} exists, id ${id}")
            return id
        } else {

            def groupIds = ws[0].groupIds.collect({ [groupId:it.id] })
            if(groupIds.find({it.groupId == Integer.parseInt(cachedGroupId)}) == null) {
                println("Editing web server ${webServerName}, id ${ws[0].id.id}, to add group ${cachedGroupId}")

                def json = new JsonBuilder()

                groupIds.add([groupId:Integer.parseInt(cachedGroupId)])

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

                tocHttpClient.put(getV1Url(), "["+json.toString()+"]")
            }
            else {
                println("Utilizing web server ${webServerName}, id ${ws[0].id.id}")
            }
            return ws[0].id.id
        }
    }
    public void deleteWebServer(int webServerId) {
        tocHttpClient.delete(getV1Url() + "/" + webServerId)
    }
    public String getWebServers(String groupId) {
        def wsUrl = getV1Url() + "?groupId=" + groupId;
        println "url = ${wsUrl}";
        tocHttpClient.get(wsUrl);
    }

    public String getWebServerConfig(String name) {
        def wsUrl = getV1Url() + "/${name}/conf?ssl=true";
        println "url = ${wsUrl}";
        tocHttpClient.doGet(wsUrl);
    }
}
