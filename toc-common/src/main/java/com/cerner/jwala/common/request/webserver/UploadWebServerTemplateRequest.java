package com.cerner.jwala.common.request.webserver;

import java.io.InputStream;
import java.io.Serializable;

import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.request.Request;
import com.cerner.jwala.common.rule.MultipleRules;
import com.cerner.jwala.common.rule.ValidTemplateNameRule;
import com.cerner.jwala.common.rule.app.GoodStreamRule;
import com.cerner.jwala.common.rule.webserver.WebServerIdRule;

/**
 * Request wrapper for uploading web server resource templates.
 *
 * Created by z0033r5b on 8/26/2015.
 */
public abstract class UploadWebServerTemplateRequest implements Serializable, Request {
    private final WebServer webServer;
    private final String fileName;
    private final InputStream data;
    private final String metaData;

    public UploadWebServerTemplateRequest(final WebServer webServer, final String fileName, final InputStream data) {
        this.webServer = webServer;
        this.fileName = fileName;
        this.data = data;
        this.metaData = null;
    }

    public UploadWebServerTemplateRequest(final WebServer webServer, final String fileName, final String metaData,
                                          final InputStream data) {
        this.webServer = webServer;
        this.fileName = fileName;
        this.data = data;
        this.metaData = metaData;
    }

    @Override
    public void validate() {
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

    public String getMetaData() {
        return metaData;
    }
}
