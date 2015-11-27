package com.siemens.cto.aem.domain.command.jvm;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.command.Command;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.rule.MultipleRules;
import com.siemens.cto.aem.rule.ValidTemplateNameRule;
import com.siemens.cto.aem.rule.app.GoodStreamRule;
import com.siemens.cto.aem.rule.jvm.JvmIdRule;

import java.io.InputStream;
import java.io.Serializable;

/**
 * Created by z0033r5b on 8/25/2015.
 */
public abstract class UploadJvmTemplateCommand implements Serializable, Command {
    private final Jvm jvm;
    private String fileName;
    private final InputStream data;

    public UploadJvmTemplateCommand(Jvm jvm, String fileName, InputStream data) {
        this.jvm = jvm;
        this.fileName = fileName;
        this.data = data;
    }

    @Override
    public void validateCommand() throws BadRequestException {
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
