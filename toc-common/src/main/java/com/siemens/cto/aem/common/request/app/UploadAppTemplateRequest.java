package com.siemens.cto.aem.common.request.app;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.request.Request;
import com.siemens.cto.aem.common.rule.MultipleRules;
import com.siemens.cto.aem.common.rule.ValidTemplateNameRule;
import com.siemens.cto.aem.common.rule.app.ApplicationIdRule;
import com.siemens.cto.aem.common.rule.app.GoodStreamRule;
import com.siemens.cto.aem.common.rule.jvm.JvmNameRule;

import java.io.InputStream;
import java.io.Serializable;

/**
 * Request wrapper for uploading an application resource template.
 */
public class UploadAppTemplateRequest implements Serializable, Request {
    private final Application application;
    private final String fileName;
    private final String jvmName;
    private final InputStream data;
    private final String confFileName;
    private final String medataData;

    public UploadAppTemplateRequest(final Application application, final String name, final String confFileName,
                                    final String jvmName, final InputStream data) {

        this.application = application;
        this.fileName = name;
        this.jvmName = jvmName;
        this.data = data;
        this.confFileName = confFileName;
        this.medataData = null;
    }

    public UploadAppTemplateRequest(final Application application, final String name, final String confFileName,
                                    final String jvmName, final InputStream data, final String metaData) {

        this.application = application;
        this.fileName = name;
        this.jvmName = jvmName;
        this.data = data;
        this.confFileName = confFileName;
        this.medataData = metaData;
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

    public String getMedataData() {
        return medataData;
    }
}
