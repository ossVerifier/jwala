package com.siemens.cto.aem.domain.model.state;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CurrentStateChronologicalComparatorTest {

    private List<CurrentState<?,?>> states;

    private CurrentStateChronologicalComparator chronological;

    @Before
    public void setup() throws Exception {

        states = new ArrayList<>();
        addCurrentStates(100);
        Collections.shuffle(states);

        chronological = CurrentStateChronologicalComparator.CHRONOLOGICAL;
    }

    @Test
    public void testChronological() {

        Collections.sort(states, chronological);

        verifyAssertions(new Verifier<DateTime>() {
            @Override
            public void verify(final DateTime aPreviousValue,
                               final DateTime aCurrentValue) {
                assertTrue(aCurrentValue.isAfter(aPreviousValue));
            }
        });
    }

    @Test
    public void testReverseChronological() {

        Collections.sort(states, Collections.reverseOrder(chronological));

        verifyAssertions(new Verifier<DateTime>() {
            @Override
            public void verify(final DateTime aPreviousValue,
                               final DateTime aCurrentValue) {
                assertTrue(aCurrentValue.isBefore(aPreviousValue));
            }
        });
    }

    private void verifyAssertions(final Verifier<DateTime> aVerifier) {

        DateTime previous = null;

        for (final CurrentState<?,?> state : states) {
            final DateTime current = state.getAsOf();
            if (previous != null) {
                aVerifier.verify(previous,
                                 current);
            }
            previous = current;
        }
    }

    private void addCurrentStates(final int aNumberToAdd) {
        for (int i = 0; i < aNumberToAdd; i++) {
            final CurrentState state = mock(CurrentState.class);
            when(state.getAsOf()).thenReturn(DateTime.now().plusDays(i));
            states.add(state);
        }
    }

    private static interface Verifier<T> {
        void verify(final T aPreviousValue,
                    final T aCurrentValue);
    }
}
