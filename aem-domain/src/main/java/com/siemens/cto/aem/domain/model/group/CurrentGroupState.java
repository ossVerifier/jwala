package com.siemens.cto.aem.domain.model.group;

import org.joda.time.DateTime;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;

public class CurrentGroupState extends CurrentState<Group, GroupState> {

    public CurrentGroupState(Identifier<Group> theId, GroupState theState, DateTime theAsOf) {
        super(theId, theState, theAsOf);
    }

}
