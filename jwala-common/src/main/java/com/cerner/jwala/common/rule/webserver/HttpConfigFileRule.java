package com.cerner.jwala.common.rule.webserver;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.path.FileSystemPath;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.rule.Rule;

/**
 * Defines rules for a http config file.
 *
 * Created by Jedd Cuison on 8/28/14.
 */
@Deprecated
// Note: HTTP Config was removed in the UI. Remove this too once nobody looks for HTTP config anymore.
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
        return fileSystemPath != null &&
                fileSystemPath.isAbsolute() &&
               !fileSystemPath.getUriPath().endsWith("/") &&
               !fileSystemPath.getUriPath().endsWith("\\");
    }

    @Override
    public void validate() throws BadRequestException {
        if (!isValid()) {
            throw new BadRequestException(FaultType.INVALID_HTTP_CONFIG_FILE,
                    "Invalid http configuration file : \"" + fileSystemPath + "\"");
        }
    }

}
