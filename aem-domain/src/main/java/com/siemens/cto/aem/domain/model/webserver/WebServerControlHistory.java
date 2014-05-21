package com.siemens.cto.aem.domain.model.webserver;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.id.Identifier;

import java.io.Serializable;

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
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final WebServerControlHistory that = (WebServerControlHistory) o;

        if (controlOperation != that.controlOperation) {
            return false;
        }
        if (execData != null ? !execData.equals(that.execData) : that.execData != null) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (webServerId != null ? !webServerId.equals(that.webServerId) : that.webServerId != null) {
            return false;
        }
        if (whenRequested != null ? !whenRequested.equals(that.whenRequested) : that.whenRequested != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (webServerId != null ? webServerId.hashCode() : 0);
        result = 31 * result + (controlOperation != null ? controlOperation.hashCode() : 0);
        result = 31 * result + (whenRequested != null ? whenRequested.hashCode() : 0);
        result = 31 * result + (execData != null ? execData.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "WebServerControlHistory{" +
               "id=" + id +
               ", webServerId=" + webServerId +
               ", controlOperation=" + controlOperation +
               ", whenRequested=" + whenRequested +
               ", execData=" + execData +
               '}';
    }
}