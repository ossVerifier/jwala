package com.siemens.cto.aem.domain.model.state;

import java.util.Comparator;

public interface OperationalState {

    public class OSComparator implements Comparator<OperationalState> {

        @Override
        public int compare(OperationalState arg0, OperationalState arg1) {
            return arg0.toPersistentString().compareTo(arg1.toPersistentString());
        }
        
    }

    String toStateString();

    String toPersistentString();

    Transience getTransience();

    Stability getStability();
}
