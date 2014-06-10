package com.siemens.cto.aem.persistence.service.jvm.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.SetJvmStateCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.persistence.jpa.domain.JpaCurrentJvmState;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaCurrentJvmStateBuilder;
import com.siemens.cto.aem.persistence.jpa.service.jvm.JvmStateCrudService;
import com.siemens.cto.aem.persistence.service.jvm.JvmStatePersistenceService;

public class JpaJvmStatePersistenceServiceImpl implements JvmStatePersistenceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaJvmStatePersistenceServiceImpl.class);

    private final JvmStateCrudService jvmStateCrudService;

    public JpaJvmStatePersistenceServiceImpl(final JvmStateCrudService theService) {
        jvmStateCrudService = theService;
    }

    public CurrentJvmState updateJvmState(final Event<SetJvmStateCommand> aNewState) {
        final SetJvmStateCommand command = aNewState.getCommand();
        LOGGER.info("Persisting new JVM state {}", command);
        final JpaCurrentJvmState currentState = jvmStateCrudService.updateJvmState(aNewState);
        return build(currentState);
    }

    @Override
    public CurrentJvmState getJvmState(final Identifier<Jvm> aJvmId) {
        final JpaCurrentJvmState currentState = jvmStateCrudService.getJvmState(aJvmId);
        return build(currentState);
    }

    @Override
    public Set<CurrentJvmState> getAllKnownJvmStates(final PaginationParameter somePagination) {
        final Set<CurrentJvmState> results = new HashSet<>();
        final List<JpaCurrentJvmState> currentJpaStates = jvmStateCrudService.getJvmStates(somePagination);
        for (final JpaCurrentJvmState state : currentJpaStates) {
            results.add(build(state));
        }
        return results;
    }

    CurrentJvmState build(final JpaCurrentJvmState aCurrentState) {
        return new JpaCurrentJvmStateBuilder(aCurrentState).build();
    }
}
