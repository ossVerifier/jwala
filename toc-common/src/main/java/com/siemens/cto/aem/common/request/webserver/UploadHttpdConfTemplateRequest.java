package com.siemens.cto.aem.common.request.webserver;

import com.siemens.cto.aem.common.domain.model.webserver.WebServer;

import java.io.InputStream;

/**
 * Created by z0033r5b on 8/26/2015.
 */
public class UploadHttpdConfTemplateRequest extends UploadWebServerTemplateRequest {
    private final String confFileName;

    public UploadHttpdConfTemplateRequest(WebServer webServer, String fileName, InputStream data) {
        super(webServer, fileName, data);
        confFileName = "httpd.conf";
    }

    public String getConfFileName() {
        return confFileName;
    }
}
