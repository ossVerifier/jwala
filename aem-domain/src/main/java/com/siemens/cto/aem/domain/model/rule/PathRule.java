package com.siemens.cto.aem.domain.model.rule;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.path.Path;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * General rule for a generic path such as a local path, absolute path, relative path etc...
 *
 * Created by Z003BPEJ on 1/13/15.
 */
public class PathRule implements Rule {

    private final Path path;

    // Note: The regEx below allows spaces since there can be paths with spaces
    //       e.g. in linux ~/some\ dir\ with\ spaces/someDir
    private static final Pattern pattern = Pattern.compile("[a-zA-Z0-9_~?+\\s\\\\/:.-]*");

    private final Matcher matcher;

    public PathRule(final Path aPath) {
        path = aPath;
        matcher = pattern.matcher(path.getPath() == null ? "" : path.getPath());
    }

    @Override
    public boolean isValid() {
        if (path != null && path.getPath() != null && !path.getPath().trim().isEmpty()) {
            return matcher.matches();
        }
        return false;
    }

    @Override
    public void validate() {
        if (!isValid()) {
           throw new BadRequestException(AemFaultType.INVALID_PATH, "Invalid path : \"" + path.getPath() + "\"");
        }
    }

}