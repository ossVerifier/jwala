package com.siemens.cto.aem.rule.group;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.GroupState;
import com.siemens.cto.aem.rule.Rule;

public class GroupStateRule implements Rule {

    private final GroupState groupState;

    public GroupStateRule(final GroupState theGroupState) {
        groupState = theGroupState;
    }

    @Override
    public boolean isValid() {
        return groupState != null;
    }

    @Override
    public void validate() throws BadRequestException {
        if (!isValid()) {
            throw new BadRequestException(AemFaultType.GROUP_STATE_NOT_SPECIFIED,
                                          "A non-null Group State was not specified");
        }
    }
}
