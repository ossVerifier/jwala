package com.siemens.cto.aem.persistence.jpa.service.jvm.impl;

import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.SetJvmStateCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.persistence.jpa.domain.JpaCurrentJvmState;
import com.siemens.cto.aem.persistence.jpa.service.JpaQueryPaginator;
import com.siemens.cto.aem.persistence.jpa.service.jvm.JvmStateCrudService;

public class JvmStateCrudServiceImpl implements JvmStateCrudService {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;

    private final JpaQueryPaginator paginator;

    public JvmStateCrudServiceImpl() {
        paginator = new JpaQueryPaginator();
    }

    @Override
    public JpaCurrentJvmState updateJvmState(final Event<SetJvmStateCommand> anEvent) {

        final JpaCurrentJvmState currentState = new JpaCurrentJvmState();
        currentState.setId(anEvent.getCommand().getNewJvmState().getJvmId().getId());
        currentState.setState(anEvent.getCommand().getNewJvmState().getJvmState().toStateString());
        currentState.setAsOf(anEvent.getCommand().getNewJvmState().getAsOf().toCalendar(Locale.US));

        return entityManager.merge(currentState);
    }

    @Override
    public JpaCurrentJvmState getJvmState(final Identifier<Jvm> aJvmId) {

        final JpaCurrentJvmState currentState = entityManager.find(JpaCurrentJvmState.class,
                                                                   aJvmId.getId());

        return currentState;
    }

    @Override
    public List<JpaCurrentJvmState> getJvmStates(final PaginationParameter somePagination) {
        final Query query = entityManager.createQuery("SELECT j FROM JpaCurrentJvmState j");

        paginator.paginate(query,
                           somePagination);

        return query.getResultList();
    }
}
