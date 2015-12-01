package com.siemens.cto.aem.service.group;

import com.siemens.cto.aem.request.dispatch.WebServerDispatchCommandResult;
import com.siemens.cto.aem.request.webserver.ControlGroupWebServerRequest;
import com.siemens.cto.aem.domain.model.user.User;

import java.util.List;

public interface GroupWebServerControlService {

    void controlGroup(final ControlGroupWebServerRequest aCommand, final User aUser);
    
    void dispatchCommandComplete(List<WebServerDispatchCommandResult> results);

}
