package com.siemens.cto.deploy.http

import groovy.json.JsonSlurper
// see http://usmlvv1srn464/jenkins/userContent/tocapi/index.html

public abstract class AbstractTocClient {

    protected TocHttpClient tocHttpClient;
    private String apiPath;


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
        def jvmUrl = getV1Url()
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


}
