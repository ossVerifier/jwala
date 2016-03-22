package com.siemens.cto.aem.service.webserver;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.exception.CommandFailureException;

/**
 * Defines non-state altering commands to a web server.
 * <p/>
 * Created by z003bpej on 8/25/14.
 */
public interface WebServerCommandService {

    CommandOutput getHttpdConf(Identifier<WebServer> webServerId) throws CommandFailureException;

}
