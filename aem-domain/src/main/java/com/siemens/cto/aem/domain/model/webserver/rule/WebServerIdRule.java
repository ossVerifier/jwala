package com.siemens.cto.aem.domain.model.webserver.rule;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.rule.Rule;
import com.siemens.cto.aem.domain.model.webserver.WebServer;

public class WebServerIdRule implements Rule {

    private final Identifier<WebServer> webServerId;

    public WebServerIdRule(final Identifier<WebServer> theId) {
        webServerId = theId;
    }

    @Override
    public boolean isValid() {
        return (webServerId != null);
    }

    @Override
    public void validate() throws BadRequestException {
        if (!isValid()) {
            throw new BadRequestException(AemFaultType.WEBSERVER_NOT_SPECIFIED,
                                          "WebServer Id was not specified");
        }
    }
}
