package com.siemens.cto.aem.common.rule.webserver;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.rule.Rule;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;

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
