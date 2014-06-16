package com.siemens.cto.aem.domain.model.webserver;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.id.Identifier;

public class WebServerControlHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Identifier<WebServerControlHistory> id;
    private final Identifier<WebServer> webServerId;
    private final WebServerControlOperation controlOperation;
    private final AuditEvent whenRequested;
    private final ExecData execData;

    public WebServerControlHistory(final Identifier<WebServerControlHistory> theId,
                                   final Identifier<WebServer> theWebServerId,
                                   final WebServerControlOperation theControlOperation,
                                   final AuditEvent theWhenRequested,
                                   final ExecData theExecData) {
        id = theId;
        webServerId = theWebServerId;
        controlOperation = theControlOperation;
        whenRequested = theWhenRequested;
        execData = theExecData;
    }

    public Identifier<WebServerControlHistory> getId() {
        return id;
    }

    public Identifier<WebServer> getWebServerId() {
        return webServerId;
    }

    public WebServerControlOperation getControlOperation() {
        return controlOperation;
    }

    public AuditEvent getWhenRequested() {
        return whenRequested;
    }

    public ExecData getExecData() {
        return execData;
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
        WebServerControlHistory rhs = (WebServerControlHistory) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .append(this.webServerId, rhs.webServerId)
                .append(this.controlOperation, rhs.controlOperation)
                .append(this.whenRequested, rhs.whenRequested)
                .append(this.execData, rhs.execData)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(webServerId)
                .append(controlOperation)
                .append(whenRequested)
                .append(execData)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("webServerId", webServerId)
                .append("controlOperation", controlOperation)
                .append("whenRequested", whenRequested)
                .append("execData", execData)
                .toString();
    }
}