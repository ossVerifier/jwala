package com.siemens.cto.aem.ws.rest.v1.service.state.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.siemens.cto.aem.domain.model.state.*;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.OperationalState;

import static org.junit.Assert.assertEquals;

public class CurrentStateProcessorTest {

    private CurrentStateProcessor processor;
    private List<CurrentState> states;

    @Before
    public void setUp() throws Exception {
        states = new ArrayList<>();
    }

    @Test
    public void testGetUniqueStates() {

        final int totalIndividualEntities = 5;
        final int totalOfEach = 100;
        addInstancesOfAllTypes(totalIndividualEntities,
                               totalOfEach);
        processor = new CurrentStateProcessor(states,
                                              Collections.reverseOrder(CurrentStateChronologicalComparator.CHRONOLOGICAL));
        final Collection<CurrentState> uniqueStates = processor.getUniqueStates();
        for (final CurrentState state : uniqueStates) {
            assertEquals(MarkerState.RIGHT,
                         state.getState());
        }
    }

    private void addInstancesOfAllTypes(final int aNumberOfIndividualEntities,
                                        final int aNumberToAdd) {
        for (final StateType type : StateType.values()) {
            for (int i = 1; i <= aNumberOfIndividualEntities; i++) {
                addInstancesOfSingleType(i,
                                         type,
                                         aNumberToAdd);
            }
        }
    }

    private void addInstancesOfSingleType(final int anId,
                                          final StateType aType,
                                          final int aNumberToAdd) {
        for (int i = 1; i <= aNumberToAdd; i++) {
            final MarkerState state;
            if (i != aNumberToAdd) {
                state = MarkerState.WRONG;
            } else {
                state = MarkerState.RIGHT;
            }
            final CurrentState<?, MarkerState> currentState = new CurrentState<>(new Identifier<>((long)anId),
                                                                                 state,
                                                                                 DateTime.now().plusDays(i),
                                                                                 aType);
            states.add(currentState);
        }
    }

    private static enum MarkerState implements OperationalState {
        RIGHT,
        WRONG;

        @Override
        public String toStateString() {
            return this.toString();
        }

        @Override
        public Transience getTransience() {
            return Transience.PERMANENT;
        }

        @Override
        public Stability getStability() {
            return Stability.STABLE;
        }
    }

}
