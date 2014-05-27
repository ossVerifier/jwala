package com.siemens.cto.aem.persistence.jpa.domain.builder;

import com.siemens.cto.aem.domain.model.audit.AuditDateTime;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.audit.AuditUser;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.exec.ExecReturnCode;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlHistory;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServerControlHistory;

public class JpaWebServerControlHistoryBuilder {

    private JpaWebServerControlHistory history;

    public JpaWebServerControlHistoryBuilder() {
    }

    public JpaWebServerControlHistoryBuilder(final JpaWebServerControlHistory aHistory) {
        history = aHistory;
    }

    public JpaWebServerControlHistoryBuilder setHistory(final JpaWebServerControlHistory aHistory) {
        history = aHistory;
        return this;
    }

    public WebServerControlHistory build() {
        return new WebServerControlHistory(getId(),
                                     getWebServerId(),
                                     getControlOperation(),
                                     getAuditEvent(),
                                     getExecData());
    }

    protected Identifier<WebServerControlHistory> getId() {
        return new Identifier<>(history.getId());
    }

    protected Identifier<WebServer> getWebServerId() {
        return new Identifier<>(history.getWebServerId());
    }

    protected WebServerControlOperation getControlOperation() {
        return WebServerControlOperation.convertFrom(history.controlOperation);
    }

    protected AuditEvent getAuditEvent() {
        return new AuditEvent(new AuditUser(history.getRequestedBy()),
                              new AuditDateTime(history.getRequestedDate().getTime()));
    }

    protected ExecData getExecData() {
        return new ExecData(new ExecReturnCode(history.getReturnCode()),
                            history.getReturnOutput(),
                            history.getReturnErrorOutput());
    }
}