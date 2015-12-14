package com.siemens.cto.aem.common.request.jvm;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.request.Request;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.rule.MultipleRules;
import com.siemens.cto.aem.common.rule.ValidTemplateNameRule;
import com.siemens.cto.aem.common.rule.app.GoodStreamRule;
import com.siemens.cto.aem.common.rule.jvm.JvmIdRule;

import java.io.InputStream;
import java.io.Serializable;

/**
 * Created by z0033r5b on 8/25/2015.
 */
public abstract class UploadJvmTemplateRequest implements Serializable, Request {
    private final Jvm jvm;
    private String fileName;
    private final InputStream data;

    public UploadJvmTemplateRequest(Jvm jvm, String fileName, InputStream data) {
        this.jvm = jvm;
        this.fileName = fileName;
        this.data = data;
    }

    @Override
    public void validate() throws BadRequestException {
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
}
