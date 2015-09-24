package com.siemens.cto.aem.domain.model.app;

import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.rule.MultipleRules;
import com.siemens.cto.aem.domain.model.rule.ValidTemplateNameRule;
import com.siemens.cto.aem.domain.model.rule.app.ApplicationIdRule;
import com.siemens.cto.aem.domain.model.rule.app.GoodStreamRule;

import java.io.InputStream;
import java.io.Serializable;

/**
 * Created by z0033r5b on 9/17/2015.
 */
public class UploadAppTemplateCommand implements Serializable, Command {
    private final Application application;
    private final String fileName;
    private final InputStream data;
    private String confFileName;

    public UploadAppTemplateCommand(Application appName, String name, String confFileName, InputStream data) {

        this.application = appName;
        this.fileName = name;
        this.data = data;
        this.confFileName = confFileName;
    }

    public void validateCommand() {
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
