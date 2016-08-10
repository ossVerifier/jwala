package com.cerner.jwala.common.rule.group;

import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.domain.model.group.GroupState;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.rule.Rule;

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
