package com.cerner.jwala.ws.rest.v1.service.app.impl;

import java.util.List;

/**
 * Created by SP043299 on 10/21/2016.
 */
public class JsonDeployApplication {
    private List<String> fileNames;
    private List<String> hostNames;

    public JsonDeployApplication() {
    }

    public JsonDeployApplication(List<String> fileNames, List<String> hostNames) {
        this.fileNames = fileNames;
        this.hostNames = hostNames;
    }

    public List<String> getFileNames() {
        return fileNames;
    }

    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }

    public List<String> getHostNames() {
        return hostNames;
    }

    public void setHostNames(List<String> hostNames) {
        this.hostNames = hostNames;
    }
}
