package com.siemens.cto.aem.service.webserver;

import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.request.webserver.ControlWebServerRequest;
import com.siemens.cto.aem.exception.CommandFailureException;

public interface WebServerControlService {

    CommandOutput controlWebServer(final ControlWebServerRequest controlWebServerRequest, final User aUser);

    CommandOutput secureCopyFile(final String aWebServerName, final String sourcePath, final String destPath, String userId) throws CommandFailureException;

    CommandOutput createDirectory(WebServer webServer, String dirAbsolutePath) throws CommandFailureException;

    CommandOutput changeFileMode(WebServer webServer, String fileMode, String targetDirPath, String targetFile) throws CommandFailureException;

    boolean waitForState(final ControlWebServerRequest controlWebServerRequest, final Long waitTimeout);
}
