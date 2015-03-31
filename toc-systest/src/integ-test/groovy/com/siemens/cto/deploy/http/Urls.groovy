package com.siemens.cto.deploy.http

/**
 * Created by z002xuvs on 8/21/2014.
 */
public class Urls {

    def String loginUrl;
    def String groupsUrl;
    def String applicationsUrl;
    def String webServersUrl;
    def String jvmsUrl;

    public Urls(String protocol, String host, String port) {
        String url = "${protocol}://${host}:${port}/aem/v1.0";
        loginUrl = "${url}/user/login"
        groupsUrl = "${url}/groups"
        applicationsUrl = "${url}/applications"
        webServersUrl = "${url}/webservers"
        jvmsUrl = "${url}/jvms"
    }
}
