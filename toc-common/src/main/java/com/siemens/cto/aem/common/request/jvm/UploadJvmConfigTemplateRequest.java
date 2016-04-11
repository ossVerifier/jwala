package com.siemens.cto.aem.common.request.jvm;

import com.siemens.cto.aem.common.domain.model.jvm.Jvm;

import java.io.InputStream;

/**
 * Created by z0033r5b on 9/15/2015.
 */
public class UploadJvmConfigTemplateRequest extends UploadJvmTemplateRequest {
    private String confFileName;

    public UploadJvmConfigTemplateRequest(final Jvm jvm, final String fileName, final InputStream data, final String metaData) {
        super(jvm, fileName, data, metaData);
    }

    public void setConfFileName(String fileName){
        confFileName = fileName;
    }

    @Override
    public String getConfFileName() {
        return confFileName;
    }
}
