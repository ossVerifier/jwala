package com.siemens.cto.aem.service.webserver;

import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.request.webserver.ControlWebServerRequest;
import com.siemens.cto.aem.exception.CommandFailureException;

public interface WebServerControlService {

    CommandOutput controlWebServer(final ControlWebServerRequest controlWebServerRequest, final User aUser);

    CommandOutput secureCopyHttpdConf(String aWebServerName, String sourcePath, String destPath) throws CommandFailureException;
}
