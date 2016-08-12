package com.cerner.jwala.common.request.jvm;

import java.io.InputStream;
import java.io.Serializable;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.request.Request;
import com.cerner.jwala.common.rule.MultipleRules;
import com.cerner.jwala.common.rule.ValidTemplateNameRule;
import com.cerner.jwala.common.rule.app.GoodStreamRule;
import com.cerner.jwala.common.rule.jvm.JvmIdRule;

/**
 * Request wrapper to upload JVM resource template.
 *
 * Created by z0033r5b on 8/25/2015.
 */
public abstract class UploadJvmTemplateRequest implements Serializable, Request {
    private final Jvm jvm;
    private final String fileName;
    private final InputStream data;
    private final String metaData;

    public UploadJvmTemplateRequest(final Jvm jvm, final String fileName, final InputStream data) {
        this.jvm = jvm;
        this.fileName = fileName;
        this.data = data;
        this.metaData = null;
    }

    public UploadJvmTemplateRequest(final Jvm jvm, final String fileName, final InputStream data, final String metaData) {
        this.jvm = jvm;
        this.fileName = fileName;
        this.data = data;
        this.metaData = metaData;
    }

    @Override
    public void validate() {
        new MultipleRules(
                new ValidTemplateNameRule(this.fileName),
                new GoodStreamRule(this.data),
                new JvmIdRule(this.jvm.getId())
        ).validate();

    }

    public Jvm getJvm() {
        return jvm;
    }

    public InputStream getData(){
        return data;
    }

    public abstract String getConfFileName();

    public String getMetaData() {
        return metaData;
    }
}
