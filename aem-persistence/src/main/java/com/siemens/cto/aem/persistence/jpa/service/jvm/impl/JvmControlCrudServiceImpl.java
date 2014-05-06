package com.siemens.cto.aem.persistence.jpa.service.jvm.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.JvmControlHistory;
import com.siemens.cto.aem.domain.model.jvm.command.CompleteControlJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvmControlHistory;
import com.siemens.cto.aem.persistence.jpa.service.jvm.JvmControlCrudService;

public class JvmControlCrudServiceImpl implements JvmControlCrudService {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;

    @Override
    public JpaJvmControlHistory addIncompleteControlHistoryEvent(final Event<ControlJvmCommand> anEvent) {

        final JpaJvmControlHistory history = new JpaJvmControlHistory();

        history.setRequestedBy(anEvent.getAuditEvent().getUser().getUserId());
        history.setRequestedDate(anEvent.getAuditEvent().getDateTime().getCalendar());
        history.setJvmId(anEvent.getCommand().getJvmId().getId());
        history.setControlOperation(anEvent.getCommand().getControlOperation().getExternalValue());

        entityManager.persist(history);
        entityManager.flush();

        return history;
    }

    @Override
    public JpaJvmControlHistory completeControlHistoryEvent(final Event<CompleteControlJvmCommand> anEvent) {

        final JpaJvmControlHistory history = getJvmControlHistory(anEvent.getCommand().getControlHistoryId());

        history.setCompletedDate(anEvent.getAuditEvent().getDateTime().getCalendar());
        final ExecData execData = anEvent.getCommand().getExecData();
        history.setReturnCode(execData.getReturnCode().getReturnCode());
        history.setReturnOutput(trimToMaxLength(execData.getStandardOutput(),
                                                JpaJvmControlHistory.MAX_OUTPUT_LENGTH));
        history.setReturnErrorOutput(trimToMaxLength(execData.getStandardError(),
                                                     JpaJvmControlHistory.MAX_OUTPUT_LENGTH));

        entityManager.flush();

        return history;
    }

    protected JpaJvmControlHistory getJvmControlHistory(final Identifier<JvmControlHistory> anId) throws NotFoundException {

        final JpaJvmControlHistory controlHistory = entityManager.find(JpaJvmControlHistory.class,
                                                                       anId.getId());

        if (controlHistory == null) {
            throw new NotFoundException(AemFaultType.JVM_CONTROL_HISTORY_NOT_FOUND,
                                        "JvmControlHistory not found: " + anId);
        }

        return controlHistory;
    }

    protected String trimToMaxLength(final String anInputString,
                                     final int aLength) {
        return anInputString.substring(0, Math.min(aLength, anInputString.length()));
    }
}
