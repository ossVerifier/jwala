package com.siemens.cto.deploy.http

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

/**
 * Created by z003e5zv on 4/7/2015.
 */
class TocClientForWebApp extends AbstractTocClient {

    TocClientForWebApp(TocHttpClient httpClient) {
        super(httpClient, "applications");
    }

    public int addWebApp(String contextPath, String appName, int groupId) {
        println("Creating application ${appName}")
        def json = new JsonBuilder();
        json name: appName, webappContext: contextPath, groupId: groupId

        def response = tocHttpClient.execute(getV1Url(), json.toString())
        def slurper = new JsonSlurper()
        def result = slurper.parseText(response);

        if(result.msgCode == "0" ) {
            return result.applicationResponseContent.id.id;
        }

        return -1; // this will never be reached, an error response will be an exception.
    }

    public int getOrCreateWebApp(String contextPath, String appName) {
        // there is no API for get by name, so will find it ourselves
        def apps = new JsonSlurper().parseText(tocHttpClient.get(getV1Url() + "?all")).applicationResponseContent.findAll { it.name == appName; }

        if(apps.size() == 0)
        {
            return addWebApp(contextPath, appName)
        } else {
            println("App ${appName} exists, id ${apps[0].id.id}")
            return apps[0].id.id
        }
    }
    public void deleteApp(int appId) {
        tocHttpClient.delete(getV1Url() +"/" + appId)
    }
}
