package com.siemens.cto.aem.domain.model.state;

import java.util.Comparator;

public enum CurrentStateChronologicalComparator implements Comparator<CurrentState> {

    CHRONOLOGICAL;

    @Override
    public int compare(final CurrentState o1,
                       final CurrentState o2) {
        return o1.getAsOf().compareTo(o2.getAsOf());
    }
}
