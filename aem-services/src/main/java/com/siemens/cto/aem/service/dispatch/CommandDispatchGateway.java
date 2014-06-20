package com.siemens.cto.aem.service.dispatch;

import org.springframework.integration.annotation.Payload;

import com.siemens.cto.aem.domain.model.dispatch.DispatchCommand;

/**
 * Gateway into the integration engine for command dispatch
 * @author horspe00
 *
 */
public interface CommandDispatchGateway {

    // Could return a Future<> if there was a result we cared about.
    public void asyncDispatchCommand(@Payload DispatchCommand command/*, @Header("force") String forceExecution */);
}
