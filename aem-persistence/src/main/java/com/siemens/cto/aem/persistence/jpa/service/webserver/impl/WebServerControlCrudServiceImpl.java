package com.siemens.cto.aem.persistence.jpa.service.webserver.impl;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlHistory;
import com.siemens.cto.aem.domain.model.webserver.command.CompleteControlWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.command.ControlWebServerCommand;
import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServerControlHistory;
import com.siemens.cto.aem.persistence.jpa.service.webserver.WebServerControlCrudService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class WebServerControlCrudServiceImpl implements WebServerControlCrudService {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;

    @Override
    public JpaWebServerControlHistory addIncompleteControlHistoryEvent(final Event<ControlWebServerCommand> anEvent) {

        final JpaWebServerControlHistory history = new JpaWebServerControlHistory();

        history.setRequestedBy(anEvent.getAuditEvent().getUser().getUserId());
        history.setRequestedDate(anEvent.getAuditEvent().getDateTime().getCalendar());
        history.setWebServerId(anEvent.getCommand().getWebServerId().getId());
        history.setControlOperation(anEvent.getCommand().getControlOperation().getExternalValue());

        entityManager.persist(history);
        entityManager.flush();

        return history;
    }

    @Override
    public JpaWebServerControlHistory completeControlHistoryEvent(final Event<CompleteControlWebServerCommand> anEvent) {

        final JpaWebServerControlHistory history = getWebServerControlHistory(anEvent.getCommand().getControlHistoryId());

        history.setCompletedDate(anEvent.getAuditEvent().getDateTime().getCalendar());
        final ExecData execData = anEvent.getCommand().getExecData();
        history.setReturnCode(execData.getReturnCode().getReturnCode());
        history.setReturnOutput(trimToMaxLength(execData.getStandardOutput(),
                                                JpaWebServerControlHistory.MAX_OUTPUT_LENGTH));
        history.setReturnErrorOutput(trimToMaxLength(execData.getStandardError(),
                                                     JpaWebServerControlHistory.MAX_OUTPUT_LENGTH));

        entityManager.flush();

        return history;
    }

    protected JpaWebServerControlHistory getWebServerControlHistory(final Identifier<WebServerControlHistory> anId) throws NotFoundException {

        final JpaWebServerControlHistory controlHistory = entityManager.find(JpaWebServerControlHistory.class,
                                                                       anId.getId());

        if (controlHistory == null) {
            throw new NotFoundException(AemFaultType.WEBSERVER_CONTROL_HISTORY_NOT_FOUND,
                                        "WebServerControlHistory not found: " + anId);
        }

        return controlHistory;
    }

    protected String trimToMaxLength(final String anInputString,
                                     final int aLength) {
        return anInputString.substring(0, Math.min(aLength, anInputString.length()));
    }
}
