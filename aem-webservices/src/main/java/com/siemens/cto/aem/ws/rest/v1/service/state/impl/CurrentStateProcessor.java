package com.siemens.cto.aem.ws.rest.v1.service.state.impl;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CurrentStateProcessor {

    private final List<CurrentState<?,?>> states;
    private final Comparator<CurrentState<?,?>> comparator;

    public CurrentStateProcessor(final List<CurrentState<?,?>> theStates,
                                 final Comparator<CurrentState<?,?>> theComparator) {
        states = new ArrayList<>(theStates);
        comparator = theComparator;
    }

    public Collection<CurrentState<?,?>> getUniqueStates() {
        Collections.sort(states, comparator);
        return unique();
    }

    Collection<CurrentState<?,?>> unique() {
        final ConcurrentMap<CurrentStateKey, CurrentState<?,?>> values = new ConcurrentHashMap<>();
        for (final CurrentState<?,?> state : states) {
            values.putIfAbsent(new CurrentStateKey(state), state);
        }
        return values.values();
    }

    private static class CurrentStateKey {
        private final Identifier<?> id;
        private final StateType type;

        private CurrentStateKey(final CurrentState<?,?> theCurrentState) {
            this(theCurrentState.getId(),
                 theCurrentState.getType());
        }

        private CurrentStateKey(final Identifier<?> theId,
                                final StateType theType) {
            id = theId;
            type = theType;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj.getClass() != getClass()) {
                return false;
            }
            CurrentStateKey rhs = (CurrentStateKey) obj;
            return new EqualsBuilder()
                    .append(this.id, rhs.id)
                    .append(this.type, rhs.type)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder()
                    .append(id)
                    .append(type)
                    .toHashCode();
        }
    }
}
