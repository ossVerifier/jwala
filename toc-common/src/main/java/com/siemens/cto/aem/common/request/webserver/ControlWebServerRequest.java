package com.siemens.cto.aem.common.request.webserver;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.request.Request;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.rule.webserver.WebServerIdRule;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerControlOperation;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class ControlWebServerRequest implements Serializable, Request {

    private static final long serialVersionUID = 1L;

    private final Identifier<WebServer> webServerId;
    private final WebServerControlOperation controlOperation;

    public ControlWebServerRequest(final Identifier<WebServer> theId,
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
    public void validate() throws BadRequestException {
        new WebServerIdRule(webServerId).validate();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        ControlWebServerRequest rhs = (ControlWebServerRequest) obj;
        return new EqualsBuilder()
                .append(this.webServerId, rhs.webServerId)
                .append(this.controlOperation, rhs.controlOperation)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(webServerId)
                .append(controlOperation)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("webServerId", webServerId)
                .append("controlOperation", controlOperation)
                .toString();
    }
}
