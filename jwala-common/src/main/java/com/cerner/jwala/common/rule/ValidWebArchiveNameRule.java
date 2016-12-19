package com.cerner.jwala.common.rule;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.exception.MessageResponseStatus;

public class ValidWebArchiveNameRule extends ValidNameRule {

    public ValidWebArchiveNameRule(final String theName) {
        super(theName);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && name.endsWith(".war");
    }

    protected MessageResponseStatus getMessageResponseStatus() { return FaultType.INVALID_WEB_ARCHIVE_NAME; }

    protected String getMessage() { return "Not a valid web archive filename. Must end in .war"; }
}
