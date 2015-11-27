package com.siemens.cto.aem.domain.command.jvm;

import com.siemens.cto.aem.domain.model.jvm.Jvm;

import java.io.InputStream;

/**
 * Created by z0033r5b on 9/15/2015.
 */
public class UploadJvmConfigTemplateCommand extends UploadJvmTemplateCommand {
    private String confFileName;

    public UploadJvmConfigTemplateCommand(Jvm jvm, String fileName, InputStream data) {
        super(jvm, fileName, data);
    }

    public void setConfFileName(String fileName){
        confFileName = fileName;
    }

    @Override
    public String getConfFileName() {
        return confFileName;
    }
}
