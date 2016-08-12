package com.cerner.jwala.common.request.app;

import java.io.InputStream;
import java.io.Serializable;

import com.cerner.jwala.common.domain.model.app.Application;
import com.cerner.jwala.common.request.Request;
import com.cerner.jwala.common.rule.MultipleRules;
import com.cerner.jwala.common.rule.ValidTemplateNameRule;
import com.cerner.jwala.common.rule.app.ApplicationIdRule;
import com.cerner.jwala.common.rule.app.GoodStreamRule;
import com.cerner.jwala.common.rule.jvm.JvmNameRule;

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
                                    final String jvmName, final String metaData, final InputStream data) {

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
