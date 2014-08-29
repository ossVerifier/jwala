package com.siemens.cto.aem.service.webserver;

import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.exception.CommandFailureException;

/**
 * Defines non-state altering commands to a web server.
 *
 * Created by z003bpej on 8/25/14.
 */
public interface WebServerCommandService {

    ExecData getHttpdConf(Identifier<WebServer> aWebServerId) throws CommandFailureException;

}
