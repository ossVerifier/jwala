package com.siemens.cto.aem.persistence.jpa.service.jvm.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvmControlHistory;
import com.siemens.cto.aem.persistence.jpa.service.jvm.JvmControlCrudService;

public class JvmControlCrudServiceImpl implements JvmControlCrudService {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;

    @Override
    public JpaJvmControlHistory addControlHistoryEvent(final Event<ControlJvmCommand> anEvent) {

        final JpaJvmControlHistory history = new JpaJvmControlHistory();

        history.setRequestedBy(anEvent.getAuditEvent().getUser().getUserId());
        history.setRequestedDate(anEvent.getAuditEvent().getDateTime().getCalendar());
        history.setJvmId(anEvent.getCommand().getJvmId().getId());
        history.setControlOperation(anEvent.getCommand().getControlOperation().getExternalValue());

        entityManager.persist(history);
        entityManager.flush();

        return history;
    }
}
