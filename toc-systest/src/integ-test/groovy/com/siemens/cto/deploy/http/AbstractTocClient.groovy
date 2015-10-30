package com.siemens.cto.deploy.http

import groovy.json.JsonSlurper
// see http://usmlvv1srn464/jenkins/userContent/tocapi/index.html

public abstract class AbstractTocClient {

    protected TocHttpClient tocHttpClient;
    private String apiPath;
    
    protected String encodeQueryParam(String param) {
        return new URI(null,null,null,param,null).toASCIIString().substring(1)
    }
    protected String encodeMatrixParam(String param) {
        return encodePathParam(param);
    }
    protected String encodePathParam(String param) {
        return new URI(null,null,param,null,null).toASCIIString()
    }

    public AbstractTocClient(TocHttpClient tocHttpClient, String apiPath) {
        this.tocHttpClient = tocHttpClient;
        this.apiPath = apiPath;
        this.login();
    }
    public void login() {
        tocHttpClient.login();
    }

    public String getV1Url() {
        return this.tocHttpClient.httpContext.getV1BaseUrl() + "/" + apiPath;
    }
    
    /**
     * @since 1.2
     */
    public def getJvm(String jvmName) {
        def jvmUrl = this.tocHttpClient.httpContext.getV1BaseUrl() + "/jvms" ;
        def responseString = tocHttpClient.get(jvmUrl);
        def slurper = new JsonSlurper();
        def result = slurper.parseText(responseString);
        for(def jvm in result.applicationResponseContent) {
            if(jvm.jvmName == jvmName) {
                return jvm;
            }
        }
        return null;
    }

    /**
     * @since 1.2
     */
    public Integer getJvmIdForName(String jvmName) {
        def jvmUrl = this.tocHttpClient.httpContext.getV1BaseUrl() + "/jvms" ;
        def responseString = tocHttpClient.get(jvmUrl);
        def slurper = new JsonSlurper();
        def result = slurper.parseText(responseString);
        for(def jvm in result.applicationResponseContent) {
            if(jvm.jvmName == jvmName) {
                return jvm.id.id;
            }
        }
        return null;
    }

    
    /**
     * Add groupName and jvmName as matrix parameters to a URL if not null
     */
    protected String addMatrixForApplication(url, groupName, jvmName) {
        def appUrl = url;
        def hasmatrix = false;
        if(groupName != null) {
            if(!hasmatrix) appUrl = appUrl + ";"
            appUrl = appUrl + "groupName=" +encodeMatrixParam(groupName);
        }
        if(jvmName != null) {
            if(!hasmatrix) appUrl = appUrl + ";"
            appUrl = appUrl + "jvmName=" +encodeMatrixParam(jvmName);
        }
        return appUrl;
    }

}
