package com.siemens.cto.aem.ws.rest.v1.service.jvm.impl;

import com.siemens.cto.aem.service.jvm.JvmResourceFileService;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;
import com.siemens.cto.aem.ws.rest.v1.service.jvm.JvmResourceFileServiceRest;

import javax.ws.rs.core.Response;

/**
 * Created by JC043760 on 4/15/2016.
 */
public class JvmResourceFileServiceRestImpl implements JvmResourceFileServiceRest {

    private final JvmResourceFileService jvmResourceFileService;

    public JvmResourceFileServiceRestImpl(final JvmResourceFileService jvmResourceFileService) {
        this.jvmResourceFileService = jvmResourceFileService;
    }

    @Override
    public Response generateAndDeployFile(final String jvmName, final String templateName) {
        jvmResourceFileService.generateAndDeployFile(jvmName, templateName);
        return ResponseBuilder.ok();
    }
}
