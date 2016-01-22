package com.siemens.cto.aem.common.domain.model.state;

import java.util.Comparator;

/**
 * Describes a state that can be described as "operational".
 */
public interface OperationalState {

    class OperationalStateComparator implements Comparator<OperationalState> {

        @Override
        public int compare(final OperationalState state1, final OperationalState state2) {
            return state1.toPersistentString().compareTo(state2.toPersistentString());
        }
        
    }

    String toStateString();

    String toPersistentString();

}
