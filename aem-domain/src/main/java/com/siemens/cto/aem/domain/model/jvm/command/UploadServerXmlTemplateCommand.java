package com.siemens.cto.aem.domain.model.jvm.command;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.rule.MultipleRules;
import com.siemens.cto.aem.domain.model.rule.ValidTemplateNameRule;
import com.siemens.cto.aem.domain.model.rule.app.GoodStreamRule;
import com.siemens.cto.aem.domain.model.rule.jvm.JvmIdRule;

import java.io.InputStream;
import java.io.Serializable;

/**
 * Created by z0033r5b on 8/17/2015.
 */
public class UploadServerXmlTemplateCommand implements Serializable, Command {
    private final Jvm jvm;
    private String fileName;
    private final InputStream data;

    public UploadServerXmlTemplateCommand(Jvm jvm, String fileName, InputStream data) {
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
}
