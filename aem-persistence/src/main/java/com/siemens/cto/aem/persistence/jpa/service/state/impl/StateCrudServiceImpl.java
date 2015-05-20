package com.siemens.cto.aem.persistence.jpa.service.state.impl;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.OperationalState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.command.SetStateCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.persistence.jpa.domain.JpaCurrentState;
import com.siemens.cto.aem.persistence.jpa.domain.JpaCurrentStateId;
import com.siemens.cto.aem.persistence.jpa.service.JpaQueryPaginator;
import com.siemens.cto.aem.persistence.jpa.service.state.StateCrudService;

@SuppressWarnings("unchecked")
public class StateCrudServiceImpl<S, T extends OperationalState> implements StateCrudService<S, T> {

    @PersistenceContext(unitName = "aem-unit")
    private EntityManager entityManager;

    private final StateType stateType;
    private final JpaQueryPaginator paginator;

    public StateCrudServiceImpl(final StateType theStateType) {
        stateType = theStateType;
        paginator = new JpaQueryPaginator();
    }

    @Override
    public JpaCurrentState updateState(final Event<SetStateCommand<S, T>> anEvent) {

        final JpaCurrentState currentState = new JpaCurrentState();
        final CurrentState<S,T> newState = anEvent.getCommand().getNewState();
        final JpaCurrentStateId id = new JpaCurrentStateId(newState.getId().getId(),
                                                           stateType);
        currentState.setId(id);
        currentState.setState(newState.getState().toPersistentString());
        currentState.setAsOf(newState.getAsOf().toCalendar(Locale.US));
        currentState.setMessage(newState.getMessage());

        return entityManager.merge(currentState);
    }

    @Override
    public JpaCurrentState getState(final Identifier<S> anId) {

        final JpaCurrentState currentState = entityManager.find(JpaCurrentState.class,
                                                                new JpaCurrentStateId(anId.getId(),
                                                                                      stateType));

        return currentState;
    }

    @Override
    public List<JpaCurrentState> getStates(final PaginationParameter somePagination) {
        final Query query = entityManager.createQuery("SELECT j FROM JpaCurrentState j WHERE j.id.stateType = :stateType");

        query.setParameter("stateType", stateType);
        paginator.paginate(query,
                           somePagination);

        return query.getResultList();
    }

    @Override
    public List<JpaCurrentState> markStaleStates(StateType stateType, T staleState, Date cutoff, AuditEvent auditData) {
        final Query updateQuery = entityManager.createNamedQuery(JpaCurrentState.UPDATE_STALE_STATES_QUERY);
        final Query getQuery = entityManager.createNamedQuery(JpaCurrentState.FIND_STALE_STATES_QUERY);

        getQuery.setParameter(JpaCurrentState.CUTOFF, cutoff);
        updateQuery.setParameter(JpaCurrentState.CUTOFF, cutoff);
        
        paginator.paginate(getQuery, PaginationParameter.all());

        List<JpaCurrentState> results = getQuery.getResultList();
        
        updateQuery.setParameter(JpaCurrentState.STATE_NAME, staleState.toPersistentString());
        updateQuery.executeUpdate();
        return results;
    }
    
    @Override
    public List<JpaCurrentState> markStaleStates(StateType stateType, T staleState, Collection<String> statesToCheck, Date cutoff, AuditEvent auditData) {
        final Query updateQuery = entityManager.createNamedQuery(JpaCurrentState.UPDATE_STALE_STATES_SUBSET_QUERY);
        final Query getQuery = entityManager.createNamedQuery(JpaCurrentState.FIND_STALE_STATES_SUBSET_QUERY);

        getQuery.setParameter(JpaCurrentState.CUTOFF, cutoff);
        getQuery.setParameter(JpaCurrentState.CHECK_STATES, statesToCheck);
        updateQuery.setParameter(JpaCurrentState.CUTOFF, cutoff);
        updateQuery.setParameter(JpaCurrentState.CHECK_STATES, statesToCheck);
        
        paginator.paginate(getQuery, PaginationParameter.all());

        List<JpaCurrentState> results = getQuery.getResultList();
        
        updateQuery.setParameter(JpaCurrentState.STATE_NAME, staleState.toPersistentString());
        updateQuery.executeUpdate();
        return results;
    }
}
