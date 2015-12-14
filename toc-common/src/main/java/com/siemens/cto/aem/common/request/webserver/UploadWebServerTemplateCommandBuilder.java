package com.siemens.cto.aem.common.request.webserver;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class UploadWebServerTemplateCommandBuilder {
    private static final String WS_HTTPD_CONF_TEMPLATE = "HttpdSslConfTemplate.tpl";

    public UploadHttpdConfTemplateRequest buildHttpdConfCommand(WebServer webServer) {
        UploadHttpdConfTemplateRequest httpdConfTemplateCommand;
        File httpdConfTemplate = new File(ApplicationProperties.get("paths.resource-types") + "/" + WS_HTTPD_CONF_TEMPLATE);
        try {
            httpdConfTemplateCommand = new UploadHttpdConfTemplateRequest(webServer,
                    httpdConfTemplate.getName(),
                    new FileInputStream(httpdConfTemplate));
        } catch (FileNotFoundException e) {
            throw new BadRequestException(AemFaultType.WEB_SERVER_HTTPD_CONF_TEMPLATE_NOT_FOUND, "Failed to find the httpd.conf template on the file system", e);
        }
        return httpdConfTemplateCommand;}
}
