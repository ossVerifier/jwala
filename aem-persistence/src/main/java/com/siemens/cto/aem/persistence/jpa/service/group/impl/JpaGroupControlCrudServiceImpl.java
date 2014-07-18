package com.siemens.cto.aem.persistence.jpa.service.group.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.GroupControlHistory;
import com.siemens.cto.aem.domain.model.group.command.CompleteControlGroupCommand;
import com.siemens.cto.aem.domain.model.group.command.GroupCommand;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroupControlHistory;
import com.siemens.cto.aem.persistence.jpa.service.group.GroupControlCrudService;

public class JpaGroupControlCrudServiceImpl implements GroupControlCrudService {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;
    
    @Override
    public JpaGroupControlHistory addIncompleteControlHistoryEvent(Event<GroupCommand> anEvent) {        
        final JpaGroupControlHistory history = new JpaGroupControlHistory();

        history.setRequestedBy(anEvent.getAuditEvent().getUser().getUserId());
        history.setRequestedDate(anEvent.getAuditEvent().getDateTime().getCalendar());
        history.setGroupId(anEvent.getCommand().getId());
        history.setControlOperation(anEvent.getCommand().getExternalOperationName());

        entityManager.persist(history);
        entityManager.flush();

        return history;
    }

    @Override
    public JpaGroupControlHistory completeControlHistoryEvent(Event<CompleteControlGroupCommand> anEvent) {


        final JpaGroupControlHistory history = getGroupControlHistory(anEvent.getCommand().getControlHistoryId());

        history.setCompletedDate(anEvent.getAuditEvent().getDateTime().getCalendar());

        entityManager.flush();

        return history;
    }
    

    protected JpaGroupControlHistory getGroupControlHistory(final Identifier<GroupControlHistory> anId) throws NotFoundException {

        final JpaGroupControlHistory controlHistory = entityManager.find(JpaGroupControlHistory.class,
                                                                       anId.getId());

        if (controlHistory == null) {
            throw new NotFoundException(AemFaultType.GROUP_CONTROL_HISTORY_NOT_FOUND,
                                        "GroupControlHistory not found: " + anId);
        }

        return controlHistory;
    }

}
