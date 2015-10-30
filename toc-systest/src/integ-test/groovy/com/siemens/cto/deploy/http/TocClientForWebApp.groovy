package com.siemens.cto.deploy.http

import com.sun.org.apache.bcel.internal.generic.NEW;

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

/**
 * Created by z003e5zv on 4/7/2015.
 */
class TocClientForWebApp extends AbstractTocClient {

    TocClientForWebApp(TocHttpClient tocHttpClient) {
        super(tocHttpClient, "applications");
    }

    public int addWebApp(String contextPath, String appName, int groupId) {
        return addWebApp(contextPath, appName, groupId, false, false);
    }
    
    /**
     * @since 1.2
     */
    public int addWebApp(String contextPath, String appName, int groupId, boolean isSecure, boolean shouldLoadBalanceAcrossServers) {
        println("Creating application ${appName}")
        def json = new JsonBuilder();
        json name: appName, webappContext: contextPath, groupId: groupId, secure:isSecure,loadBalanceAcrossServers:shouldLoadBalanceAcrossServers

        def response = tocHttpClient.execute(getV1Url(), json.toString())
        def slurper = new JsonSlurper()
        def result = slurper.parseText(response);

        if(result.msgCode == "0" ) {
            return result.applicationResponseContent.id.id;
        }

        return -1; // this will never be reached, an error response will be an exception.
    }

    public int getOrCreateWebApp(String contextPath, String appName, int groupId) {
        return getOrCreateWebApp(contextPath, appName, groupId, false, false);
    }

    /**
     * @since 1.2
     */
    public int getOrCreateWebApp(String contextPath, String appName, int groupId, boolean isSecure, boolean shouldLoadBalanceAcrossServers) {
        // there is no API for get by name, so will find it ourselves
        def apps = new JsonSlurper().parseText(tocHttpClient.get(getV1Url() + "?all")).applicationResponseContent.findAll { it.name == appName; }

        if(apps.size() == 0)
        {
            return addWebApp(contextPath, appName, groupId, isSecure, shouldLoadBalanceAcrossServers)
        } else {
            println("App ${appName} exists, id ${apps[0].id.id}")
            return apps[0].id.id
        }
    }

    public void deleteApp(int appId) {
        tocHttpClient.delete(getV1Url() +"/" + appId)
    }

    /**
     * https://localhost:9101/aem/v1.0/applications/Cluster3/resources/template/cluster.xml;groupName=null;jvmName=null?tokensReplaced=false&_=1444833670000
     * @param appName
     * @return
     */
    public String getApplicationXmlTemplate(String appName, String contextName, String groupName) {
        return getApplicationXmlTemplate(appName, contextName, groupName, null);
    }

    /**
     * GET https://localhost:9101/aem/v1.0/applications/<app>/resources/template/<context>.xml;groupName=<group>;jvmName=<jvm>?tokensReplaced=false&_=1444833670000
     * @param appName
     * @return
     */
    public String getApplicationXmlTemplate(String appName, String contextName, String groupName, String jvmName) {
        def appUrl = getV1Url() + "/"+encodePathParam(appName)+"/resources/template/"+encodePathParam(contextName)+".xml"
        appUrl = addMatrixForApplication(appUrl, groupName, jvmName);
        appUrl = appUrl + "?tokensReplaced=false";

        println "url = ${appUrl}"
        return tocHttpClient.get(appUrl);
    }

    /**
     * GET https://localhost:9101/aem/v1.0/applications/<app>/resources/preview;groupName=<group>;jvmName=<jvm>
     */
    public String getApplicationXmlPreview(String appName, String contextName, String groupName, String jvmName) {
        def appUrl = getV1Url() + "/"+encodePathParam(appName)+"/resources/preview/"+encodePathParam(contextName)+".xml"
        appUrl = addMatrixForApplication(appUrl, groupName, jvmName);
        println "url = ${appUrl}"
        return tocHttpClient.get(appUrl);
    }

    /**
     * PUT https://localhost:9101/aem/v1.0/applications/<app>/resources/template/<context>.xml
     */
    public String updateApplicationXml(String appName, String contextName, String groupName, String jvmName, String content) {
        def appUrl = getV1Url() + "/"+encodePathParam(appName)+"/resources/template/"+encodePathParam(contextName)+".xml"
        appUrl = addMatrixForApplication(appUrl, groupName, jvmName);
        println "url = ${appUrl}, content.length()=" + content.length();
        tocHttpClient.putText(appUrl, content);
    }

    /**
     * PUT https://localhost:9101/aem/v1.0/applications/<appName>/conf/<context>.xml;groupName=<groupName>;jvmName=<jvmName>
     */
    public String deployApplicationToInstance(String appName, String contextName, String groupName, String jvmName) {
        def appUrl = getV1Url() + "/"+encodePathParam(appName)+"/conf/"+encodePathParam(contextName)+".xml"
        appUrl = addMatrixForApplication(appUrl, groupName, jvmName);
        println "url = ${appUrl}"
        return tocHttpClient.put(appUrl,"");
    }

    /**
     * PUT https://localhost:9101/aem/v1.0/applications/<appName>/conf/<context>RoleMapping.properties;groupName=<groupName>;jvmName=<jvmName>
     */
    public String deployRoleMappingToInstance(String appName, String contextName, String groupName, String jvmName) {
        def appUrl = getV1Url() + "/"+encodePathParam(appName)+"/conf/"+encodePathParam(contextName)+"RoleMapping.properties"
        appUrl = addMatrixForApplication(appUrl, groupName, jvmName);
        println "url = ${appUrl}"
        return tocHttpClient.put(appUrl,"");
    }
    /**
     * @since 1.2
     */
    public getApplication(String appName) {
        def appUrl = getV1Url()
        def responseString = tocHttpClient.get(appUrl);
        def slurper = new JsonSlurper();
        def result = slurper.parseText(responseString);
        for(def app in result.applicationResponseContent) {
            if(app.name == appName) {
                return app;
            }
        }
        return null;
    }

    /**
     * @since 1.2
     */
    public Integer getApplicationIdForName(String appName) {
        return getApplication(appName).id.id;
    }

    /**
     * Multipart form upload https://localhost:9101/aem/v1.0/applications/<appId>/war
     *
     */
    public def uploadWebArchive(String appName, File webapp) {
        def appId = getApplicationIdForName(appName);
        def appUrl = getV1Url() + "/" + appId + "/war"
        return tocHttpClient.multipartPostReturningJson(appUrl, webapp);
    }

    /** Verify /app
     * @since 1.2 */
    public def checkApp(String jvmName, String appName) {
        def app = getApplication(appName)
        def jvmUrl = getJvm(jvmName).statusUri.replace("/stp.png", app.webAppContext)
        tocHttpClient.getAnywhere(jvmUrl);
    }
}
