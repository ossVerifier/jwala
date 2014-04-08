package com.siemens.cto.aem.domain.model.rule.group;

import com.siemens.cto.aem.common.exception.MessageResponseStatus;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.rule.Rule;
import com.siemens.cto.aem.domain.model.rule.ValidNameRule;

public class GroupNameRule extends ValidNameRule implements Rule {

    public GroupNameRule(final String theName) {
        super(theName);
    }

    @Override
    protected MessageResponseStatus getMessageResponseStatus() {
        return AemFaultType.INVALID_GROUP_NAME;
    }

    @Override
    protected String getMessage() {
        return "Invalid Group Name: \"" + name + "\"";
    }
}
