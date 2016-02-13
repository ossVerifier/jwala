package com.siemens.cto.aem.common.request.app;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.request.Request;
import com.siemens.cto.aem.common.rule.MultipleRules;
import com.siemens.cto.aem.common.rule.ValidTemplateNameRule;
import com.siemens.cto.aem.common.rule.app.ApplicationIdRule;
import com.siemens.cto.aem.common.rule.app.GoodStreamRule;
import com.siemens.cto.aem.common.rule.jvm.JvmIdRule;
import com.siemens.cto.aem.common.rule.jvm.JvmNameRule;

import java.io.InputStream;
import java.io.Serializable;

public class UploadAppTemplateRequest implements Serializable, Request {
    private final Application application;
    private final String fileName;
    private String jvmName;
    private final InputStream data;
    private String confFileName;

    public UploadAppTemplateRequest(Application application, String name, String confFileName, String jvmName, InputStream data) {

        this.application = application;
        this.fileName = name;
        this.jvmName = jvmName;
        this.data = data;
        this.confFileName = confFileName;
    }

    public void validate() {
        new MultipleRules(
                new ValidTemplateNameRule(this.fileName),
                new GoodStreamRule(this.data),
                new ApplicationIdRule(this.application.getId()),
                new JvmNameRule(this.jvmName)
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

    public String getJvmName() {
        return jvmName;
    }
}
