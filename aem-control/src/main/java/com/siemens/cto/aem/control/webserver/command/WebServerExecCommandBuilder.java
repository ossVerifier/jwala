package com.siemens.cto.aem.control.webserver.command;

import com.siemens.cto.aem.domain.model.exec.ExecCommand;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlOperation;

public interface WebServerExecCommandBuilder {

    WebServerExecCommandBuilder setWebServer(final WebServer aWebServer);

    WebServerExecCommandBuilder setOperation(final WebServerControlOperation anOperation);

    ExecCommand build();
}
