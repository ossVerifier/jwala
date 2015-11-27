package com.siemens.cto.aem.service.dispatch;

import com.siemens.cto.aem.domain.command.dispatch.DispatchCommand;

/**
 * Gateway into the integration engine for command dispatch
 * @author horspe00
 *
 */
public interface CommandDispatchGateway {

    public void asyncDispatchCommand(DispatchCommand command);
}
