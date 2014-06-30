package com.siemens.cto.aem.persistence.jpa.domain.builder;

import com.siemens.cto.aem.domain.model.audit.AuditDateTime;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.audit.AuditUser;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.group.GroupControlHistory;
import com.siemens.cto.aem.domain.model.group.GroupControlOperation;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroupControlHistory;

public class JpaGroupControlHistoryBuilder {

    private JpaGroupControlHistory history;

    public JpaGroupControlHistoryBuilder() {
    }

    public JpaGroupControlHistoryBuilder(final JpaGroupControlHistory aHistory) {
        history = aHistory;
    }

    public JpaGroupControlHistoryBuilder setHistory(final JpaGroupControlHistory aHistory) {
        history = aHistory;
        return this;
    }

    public GroupControlHistory build() {
        return new GroupControlHistory(getId(),
                                     getGroupId(),
                                     getControlOperation(),
                                     getAuditEvent());
    }

    protected Identifier<GroupControlHistory> getId() {
        return new Identifier<>(history.getId());
    }

    protected Identifier<Group> getGroupId() {
        return new Identifier<>(history.getGroupId());
    }

    protected GroupControlOperation getControlOperation() {
        return GroupControlOperation.convertFrom(history.controlOperation);
    }

    protected AuditEvent getAuditEvent() {
        return new AuditEvent(new AuditUser(history.getRequestedBy()),
                              new AuditDateTime(history.getRequestedDate().getTime()));
    }

}

