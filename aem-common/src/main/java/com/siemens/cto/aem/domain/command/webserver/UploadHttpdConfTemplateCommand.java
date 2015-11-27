package com.siemens.cto.aem.domain.command.webserver;

import com.siemens.cto.aem.domain.model.webserver.WebServer;

import java.io.InputStream;

/**
 * Created by z0033r5b on 8/26/2015.
 */
public class UploadHttpdConfTemplateCommand extends UploadWebServerTemplateCommand {
    private final String confFileName;

    public UploadHttpdConfTemplateCommand(WebServer webServer, String fileName, InputStream data) {
        super(webServer, fileName, data);
        confFileName = "httpd.conf";
    }

    public String getConfFileName() {
        return confFileName;
    }
}
