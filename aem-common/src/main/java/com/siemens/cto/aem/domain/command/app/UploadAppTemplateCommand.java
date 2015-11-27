package com.siemens.cto.aem.domain.command.app;

import com.siemens.cto.aem.domain.command.Command;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.rule.MultipleRules;
import com.siemens.cto.aem.rule.ValidTemplateNameRule;
import com.siemens.cto.aem.rule.app.ApplicationIdRule;
import com.siemens.cto.aem.rule.app.GoodStreamRule;

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
