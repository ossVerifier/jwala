package com.siemens.cto.deploy.http

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

/**
 * Created by z003e5zv on 4/7/2015.
 */
class TocClientForJvm extends AbstractTocClient {


    TocClientForJvm(TocHttpClient httpClient) {
        super(httpClient, "jvms");
    }

    public int addJvm(String jvmName, String hostName, int httpPort, int httpsPort, int redirectPort, int shutdownPort, int ajpPort, String statusPath, String systemProperties, int groupId) {
        println("Creating jvm ${jvmName}")

        def json = new JsonBuilder();
        json jvmName: jvmName, groupId: groupId, hostName: hostName, httpPort: httpPort, httpsPort: httpsPort, redirectPort: redirectPort, shutdownPort: shutdownPort, ajpPort: ajpPort, statusPath: statusPath, systemProperties: systemProperties;

        def response = tocHttpClient.execute(getV1Url(), json.toString());
        def jvms = new JsonSlurper().parseText(response).applicationResponseContent
        def id = jvms.id.id;
        return id;
    }

    public int getOrCreateJvm(String jvmName, String hostName, int httpPort, int httpsPort, int redirectPort, int shutdownPort, int ajpPort, String statusPath, String systemProperties, int groupId) {
        // there is no API for get by name, so will find it ourselves
        def jvms = new JsonSlurper().parseText(tocHttpClient.get(getV1Url() + "?all")).applicationResponseContent.findAll { it.jvmName == jvmName; }

        if(jvms.size() == 0)
        {
            return addJvm(jvmName, hostName, httpPort, httpsPort, redirectPort, shutdownPort, ajpPort, statusPath, systemProperties, groupId);
        } else {
            println("Jvm ${jvmName} exists, id ${jvms[0].id.id}")
            return jvms[0].id.id
        }
    }

    public void deleteJvm(int jvmId) {
        tocHttpClient.delete(this.getV1Url() + "/" + jvmId)
    }

    /**
     * Generate a JVM instance as configured
     */
    public void deployJvmInstance(String jvmName) {
        def jvmUrl = getV1Url() + "/${jvmName}/conf"
        println "url = ${jvmUrl}"
        tocHttpClient.put(jvmUrl, "");
    }
    
    /**
     * PUT https://localhost:9101/aem/v1.0/jvms/<jvm>/resources/template/<templateName.xml>
     * @since 1.2
     */
    public String updateJvmTemplate(String jvmName, String templateName, String content) {
        def jvmUrl = getV1Url() + "/"+jvmName+"/resources/template/" + templateName
        println "url = ${jvmUrl}, content.length()=" + content.length();
        tocHttpClient.putText(jvmUrl, content);
    }

    /**
     * @since 1.2
     */
    public def start(String jvmName) {
        def jvmId = getJvmIdForName(jvmName);
        def jvmUrl = getV1Url() + "/"+jvmId+"/commands"
        def jvmOp = "{\"controlOperation\":\"start\"}"
        println "url = ${jvmUrl}, op=${jvmOp}";
        tocHttpClient.execute(jvmUrl, jvmOp);
    }

    /**
     * @since 1.2
     */
    public def stop(String jvmName) {
        def jvmId = getJvmIdForName(jvmName);
        def jvmUrl = getV1Url() + "/"+jvmId+"/commands"
        def jvmOp = "{\"controlOperation\":\"stop\"}"
        println "url = ${jvmUrl}, op=${jvmOp}";
        tocHttpClient.execute(jvmUrl, jvmOp);
    }
    
    /** Verify /stp.png
     * @since 1.2 */
    public def checkJvm(String jvmName) {
        def jvmUrl = getJvm(jvmName).statusUri
        tocHttpClient.getAnywhere(jvmUrl);
    } 
}
