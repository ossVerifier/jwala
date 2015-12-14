package com.siemens.cto.aem.control.webserver.command;

import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerControlOperation;

public interface WebServerExecCommandBuilder {

    WebServerExecCommandBuilder setWebServer(final WebServer aWebServer);

    WebServerExecCommandBuilder setOperation(final WebServerControlOperation anOperation);

    WebServerExecCommandBuilder setParameter(final String...aParams);

    ExecCommand build();
}
