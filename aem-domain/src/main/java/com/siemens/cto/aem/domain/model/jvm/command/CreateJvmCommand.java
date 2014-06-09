package com.siemens.cto.aem.domain.model.jvm.command;

import java.io.Serializable;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.command.Command;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.rule.MultipleRules;
import com.siemens.cto.aem.domain.model.rule.PortNumberRule;
import com.siemens.cto.aem.domain.model.rule.jvm.JvmHostNameRule;
import com.siemens.cto.aem.domain.model.rule.jvm.JvmNameRule;

public class CreateJvmCommand implements Serializable, Command {

    private static final long serialVersionUID = 1L;

    private final String jvmName;
    private final String hostName;

    // JVM ports
    private final Integer httpPort;
    private final Integer httpsPort;
    private final Integer redirectPort;
    private final Integer shutdownPort;
    private final Integer ajpPort;

    public CreateJvmCommand(final String theName,
                            final String theHostName,
                            final Integer theHttpPort,
                            final Integer theHttpsPort,
                            final Integer theRedirectPort,
                            final Integer theShutdownPort,
                            final Integer theAjpPort) {
        jvmName = theName;
        hostName = theHostName;
        httpPort = theHttpPort;
        httpsPort = theHttpsPort;
        redirectPort = theRedirectPort;
        shutdownPort = theShutdownPort;
        ajpPort = theAjpPort;
    }

    public String getJvmName() {
        return jvmName;
    }

    public String getHostName() {
        return hostName;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public Integer getHttpsPort() {
        return httpsPort;
    }

    public Integer getRedirectPort() {
        return redirectPort;
    }

    public Integer getShutdownPort() {
        return shutdownPort;
    }

    public Integer getAjpPort() {
        return ajpPort;
    }

    @Override
    public void validateCommand() throws BadRequestException {
        new MultipleRules(new JvmNameRule(jvmName),
                          new JvmHostNameRule(hostName),
                          new PortNumberRule(httpPort, AemFaultType.INVALID_JVM_HTTP_PORT),
                          new PortNumberRule(httpsPort, AemFaultType.INVALID_JVM_HTTPS_PORT, true),
                          new PortNumberRule(redirectPort, AemFaultType.INVALID_JVM_REDIRECT_PORT),
                          new PortNumberRule(shutdownPort, AemFaultType.INVALID_JVM_SHUTDOWN_PORT),
                          new PortNumberRule(ajpPort, AemFaultType.INVALID_JVM_AJP_PORT)).validate();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final CreateJvmCommand that = (CreateJvmCommand) o;

        if (hostName != null ? !hostName.equals(that.hostName) : that.hostName != null) {
            return false;
        }
        if (jvmName != null ? !jvmName.equals(that.jvmName) : that.jvmName != null) {
            return false;
        }
        if (httpPort != null ? !httpPort.equals(that.httpPort) : that.httpPort != null) {
            return false;
        }
        if (httpsPort != null ? !httpsPort.equals(that.httpsPort) : that.httpsPort != null) {
            return false;
        }
        if (redirectPort != null ? !redirectPort.equals(that.redirectPort) : that.redirectPort != null) {
            return false;
        }
        if (shutdownPort != null ? !shutdownPort.equals(that.shutdownPort) : that.shutdownPort != null) {
            return false;
        }
        if (ajpPort != null ? !ajpPort.equals(that.ajpPort) : that.ajpPort != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = jvmName != null ? jvmName.hashCode() : 0;
        result = 31 * result + (hostName != null ? hostName.hashCode() : 0);

        result = 31 * result + (httpPort != null ? httpPort.hashCode() : 0);
        result = 31 * result + (httpsPort != null ? httpsPort.hashCode() : 0);
        result = 31 * result + (redirectPort != null ? redirectPort.hashCode() : 0);
        result = 31 * result + (shutdownPort != null ? shutdownPort.hashCode() : 0);
        result = 31 * result + (ajpPort != null ? ajpPort.hashCode() : 0);

        return result;
    }

    @Override
    public String toString() {
        return "CreateJvmCommand{" +
               "jvmName='" + jvmName + '\'' +
               ", hostName='" + hostName + '\'' +
               ", httpPort='" + httpPort + '\'' +
               ", httpsPort='" + httpsPort + '\'' +
               ", redirectPort='" + redirectPort + '\'' +
               ", shutdownPort='" + shutdownPort + '\'' +
               ", ajpPort='" + ajpPort + '\'' +
               '}';
    }
}
