package com.siemens.cto.aem.common.request.app;

import com.siemens.cto.aem.common.request.Request;
import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.rule.MultipleRules;
import com.siemens.cto.aem.common.rule.ValidTemplateNameRule;
import com.siemens.cto.aem.common.rule.app.ApplicationIdRule;
import com.siemens.cto.aem.common.rule.app.GoodStreamRule;

import java.io.InputStream;
import java.io.Serializable;

/**
 * Created by z0033r5b on 9/17/2015.
 */
public class UploadAppTemplateRequest implements Serializable, Request {
    private final Application application;
    private final String fileName;
    private final InputStream data;
    private String confFileName;

    public UploadAppTemplateRequest(Application appName, String name, String confFileName, InputStream data) {

        this.application = appName;
        this.fileName = name;
        this.data = data;
        this.confFileName = confFileName;
    }

    public void validate() {
        new MultipleRules(
                new ValidTemplateNameRule(this.fileName),
                new GoodStreamRule(this.data),
                new ApplicationIdRule(this.application.getId())
        ).validate();
    }

    public Application getApp() {
        return application;
    }

    public InputStream getData() {
        return data;
    }

    public String getConfFileName() {
        return confFileName;
    }
}
