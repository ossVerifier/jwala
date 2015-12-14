package com.siemens.cto.aem.common.domain.model.state;

import java.util.Comparator;

public interface OperationalState {

    public class OSComparator implements Comparator<OperationalState> {

        @Override
        public int compare(OperationalState arg0, OperationalState arg1) {
            return arg0.toPersistentString().compareTo(arg1.toPersistentString());
        }
        
    }
    
    static final boolean _IS_A_STARTED_STATE = true;
    static final boolean _NOT_A_STARTED_STATE = false;

    String toStateString();

    String toPersistentString();
}
