package com.siemens.cto.aem.persistence.jpa.domain.builder;

import com.siemens.cto.aem.domain.model.audit.AuditDateTime;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.audit.AuditUser;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlHistory;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;

public class JpaJvmControlHistoryBuilder {

    private com.siemens.cto.aem.persistence.jpa.domain.JpaJvmControlHistory history;

    public JpaJvmControlHistoryBuilder() {
    }

    public JpaJvmControlHistoryBuilder(final com.siemens.cto.aem.persistence.jpa.domain.JpaJvmControlHistory aHistory) {
        history = aHistory;
    }

    public JpaJvmControlHistoryBuilder setHistory(final com.siemens.cto.aem.persistence.jpa.domain.JpaJvmControlHistory aHistory) {
        history = aHistory;
        return this;
    }

    public JvmControlHistory build() {
        return new JvmControlHistory(getId(),
                                     getJvmId(),
                                     getControlOperation(),
                                     getAuditEvent());
    }

    protected Identifier<JvmControlHistory> getId() {
        return new Identifier<>(history.getId());
    }

    protected Identifier<Jvm> getJvmId() {
        return new Identifier<>(history.getJvmId());
    }

    protected JvmControlOperation getControlOperation() {
        return JvmControlOperation.convertFrom(history.controlOperation);
    }

    protected AuditEvent getAuditEvent() {
        return new AuditEvent(new AuditUser(history.getRequestedBy()),
                              new AuditDateTime(history.getRequestedDate().getTime()));
    }
}

