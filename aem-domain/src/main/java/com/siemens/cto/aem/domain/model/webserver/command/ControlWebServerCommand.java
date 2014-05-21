package com.siemens.cto.aem.domain.model.webserver.command;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.rule.webserver.WebServerIdRule;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlOperation;

import java.io.Serializable;

public class ControlWebServerCommand implements Serializable, Command {

    private static final long serialVersionUID = 1L;

    private final Identifier<WebServer> webServerId;
    private final WebServerControlOperation controlOperation;

    public ControlWebServerCommand(final Identifier<WebServer> theId,
                                   final WebServerControlOperation theControlOperation) {
        webServerId = theId;
        controlOperation = theControlOperation;
    }

    public Identifier<WebServer> getWebServerId() {
        return webServerId;
    }

    public WebServerControlOperation getControlOperation() {
        return controlOperation;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        new WebServerIdRule(webServerId).validate();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ControlWebServerCommand that = (ControlWebServerCommand) o;

        if (controlOperation != that.controlOperation) {
            return false;
        }
        if (webServerId != null ? !webServerId.equals(that.webServerId) : that.webServerId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = webServerId != null ? webServerId.hashCode() : 0;
        result = 31 * result + (controlOperation != null ? controlOperation.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ControlWebServerCommand{" +
               "webServerId=" + webServerId +
               ", controlOperation=" + controlOperation +
               '}';
    }
}
