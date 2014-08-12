package com.siemens.cto.aem.domain.model.rule.webserver;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.path.Path;
import com.siemens.cto.aem.domain.model.rule.Rule;

public class StatusPathRule implements Rule {

    private final Path statusPath;

    public StatusPathRule(final Path thePath) {
        statusPath = thePath;
    }

    @Override
    public boolean isValid() {
        return (statusPath != null) && statusPath.isAbsolute();
    }

    @Override
    public void validate() throws BadRequestException {
        if (!isValid()) {
            throw new BadRequestException(AemFaultType.INVALID_STATUS_PATH,
                                          "Invalid status path URL : \"" + statusPath + "\"");
        }
    }
}
