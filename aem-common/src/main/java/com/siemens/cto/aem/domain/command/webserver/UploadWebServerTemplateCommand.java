package com.siemens.cto.aem.domain.command.webserver;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.command.Command;
import com.siemens.cto.aem.rule.MultipleRules;
import com.siemens.cto.aem.rule.ValidTemplateNameRule;
import com.siemens.cto.aem.rule.app.GoodStreamRule;
import com.siemens.cto.aem.rule.webserver.WebServerIdRule;
import com.siemens.cto.aem.domain.model.webserver.WebServer;

import java.io.InputStream;
import java.io.Serializable;

/**
 * Created by z0033r5b on 8/26/2015.
 */
public abstract class UploadWebServerTemplateCommand implements Serializable, Command {
    private final WebServer webServer;
    private String fileName;
    private final InputStream data;

    public UploadWebServerTemplateCommand(WebServer webServer, String fileName, InputStream data) {
        this.webServer = webServer;
        this.fileName = fileName;
        this.data = data;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        new MultipleRules(
                new ValidTemplateNameRule(this.fileName),
                new GoodStreamRule(this.data),
                new WebServerIdRule(this.webServer.getId())
        ).validate();

    }

    public WebServer getWebServer() {
        return webServer;
    }

    public InputStream getData(){
        return data;
    }

    public abstract String getConfFileName();
}
