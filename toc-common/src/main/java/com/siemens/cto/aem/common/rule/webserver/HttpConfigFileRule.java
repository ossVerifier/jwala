package com.siemens.cto.aem.common.rule.webserver;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.path.FileSystemPath;
import com.siemens.cto.aem.common.rule.Rule;

/**
 * Defines rules for a http config file.
 *
 * Created by z003bpej on 8/28/14.
 */
public class HttpConfigFileRule implements Rule {

    private final FileSystemPath fileSystemPath;

    public HttpConfigFileRule(final FileSystemPath theFileSystemPath) {
        fileSystemPath = theFileSystemPath;
    }

    @Override
    public boolean isValid() {

        /**
         * Note: Path IS absolute since config file can be
         *       d:/some-dir/httpd.conf or /cygdrive/some-dir/httpd-conf
         *       Both of which are interpreted as absolute paths by Java.
         */
        return (fileSystemPath != null) &&
                fileSystemPath.isAbsolute() &&
               !fileSystemPath.getUriPath().endsWith("/") &&
               !fileSystemPath.getUriPath().endsWith("\\");
    }

    @Override
    public void validate() throws BadRequestException {
        if (!isValid()) {
            throw new BadRequestException(AemFaultType.INVALID_HTTP_CONFIG_FILE,
                    "Invalid http configuration file : \"" + fileSystemPath + "\"");
        }
    }

}
