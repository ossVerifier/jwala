package com.cerner.jwala.common.request.webserver;

import java.io.InputStream;

import com.cerner.jwala.common.domain.model.webserver.WebServer;

/**
 * Upload request wrapper specific for httpd.conf.
 * Note: Deprecated in lieu of a more generic approach ???
 *
 * Created by z0033r5b on 8/26/2015.
 */
@Deprecated
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
