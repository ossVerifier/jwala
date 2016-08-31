package com.cerner.jwala.service.webserver;

import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.request.webserver.ControlWebServerRequest;
import com.cerner.jwala.exception.CommandFailureException;

public interface WebServerControlService {

    CommandOutput controlWebServer(final ControlWebServerRequest controlWebServerRequest, final User aUser);

    CommandOutput secureCopyFile(final String aWebServerName, final String sourcePath, final String destPath, String userId) throws CommandFailureException;

    CommandOutput createDirectory(WebServer webServer, String dirAbsolutePath) throws CommandFailureException;

    CommandOutput makeExecutableUnixFormat(WebServer webServer, String fileMode, String targetDirPath, String targetFile) throws CommandFailureException;
}
