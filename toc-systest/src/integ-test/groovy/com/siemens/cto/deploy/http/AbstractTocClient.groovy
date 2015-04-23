package com.siemens.cto.deploy.http
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

}
