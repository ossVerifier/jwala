package com.cerner.jwala.tomcat.plugin.jgroups;

/**
 * Exception wrapper for {@link JGroupsStateReporter} related errors
 *
 * Created by JC043760 on 8/18/2016
 */
public class JGroupsStateReporterException extends RuntimeException {

    public JGroupsStateReporterException(final String msg, final Throwable t) {
        super(msg, t);
    }
}
