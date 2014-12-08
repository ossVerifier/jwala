package com.siemens.cto.aem.domain.model.rule;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.path.Path;

import java.net.URI;
import java.net.URISyntaxException;

public class StatusPathRule implements Rule {

    private final Path statusPath;

    public StatusPathRule(final Path thePath) {
        statusPath = thePath;
    }

    @Override
    public boolean isValid() {
        if ((statusPath != null) && statusPath.isAbsolute()) {
            try {
                new URI("http", null, "hostName", 8080, statusPath.getPath(), "", "");
            } catch (URISyntaxException e) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void validate() throws BadRequestException {
        if (!isValid()) {
            throw new BadRequestException(AemFaultType.INVALID_STATUS_PATH,
                                          "Invalid status path URL : \"" + statusPath + "\"");
        }
    }
}