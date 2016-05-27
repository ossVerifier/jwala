package com.siemens.cto.aem.common.request.jvm;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.common.request.Request;
import com.siemens.cto.aem.common.rule.HostNameRule;
import com.siemens.cto.aem.common.rule.MultipleRules;
import com.siemens.cto.aem.common.rule.PortNumberRule;
import com.siemens.cto.aem.common.rule.ShutdownPortNumberRule;
import com.siemens.cto.aem.common.rule.StatusPathRule;
import com.siemens.cto.aem.common.rule.jvm.JvmNameRule;

public class CreateJvmRequest implements Serializable, Request {

    private static final long serialVersionUID = 1L;

    private final String jvmName;
    private final String hostName;

    // JVM ports
    private final Integer httpPort;
    private final Integer httpsPort;
    private final Integer redirectPort;
    private final Integer shutdownPort;
    private final Integer ajpPort;

    private final Path statusPath;

    private final String systemsProperties;
    private final String userName;
    private final String encryptedPassword;
    
    public CreateJvmRequest(final String theName,
                            final String theHostName,
                            final Integer theHttpPort,
                            final Integer theHttpsPort,
                            final Integer theRedirectPort,
                            final Integer theShutdownPort,
                            final Integer theAjpPort,
                            final Path theStatusPath,
                            final String theSystemProperties,
                            final String theUserName,
                            final String theEncryptedPassword) {
        jvmName = theName;
        hostName = theHostName;
        httpPort = theHttpPort;
        httpsPort = theHttpsPort;
        redirectPort = theRedirectPort;
        shutdownPort = theShutdownPort;
        ajpPort = theAjpPort;
        statusPath = theStatusPath;
        systemsProperties = theSystemProperties;
        userName = theUserName;
        encryptedPassword = theEncryptedPassword;
        
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

    public Path getStatusPath() {
        return statusPath;
    }

    public String getSystemProperties() {
        return systemsProperties;
    }

    public String getUserName() {
        return userName;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    @Override
    public void validate() {
        new MultipleRules(new JvmNameRule(jvmName),
                          new HostNameRule(hostName),
                          new StatusPathRule(statusPath),
                          new PortNumberRule(httpPort, AemFaultType.INVALID_JVM_HTTP_PORT),
                          new PortNumberRule(httpsPort, AemFaultType.INVALID_JVM_HTTPS_PORT, true),
                          new PortNumberRule(redirectPort, AemFaultType.INVALID_JVM_REDIRECT_PORT),
                          new ShutdownPortNumberRule(shutdownPort, AemFaultType.INVALID_JVM_SHUTDOWN_PORT),
                          new PortNumberRule(ajpPort, AemFaultType.INVALID_JVM_AJP_PORT)).validate();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        CreateJvmRequest rhs = (CreateJvmRequest) obj;
        return new EqualsBuilder()
                .append(this.jvmName, rhs.jvmName)
                .append(this.hostName, rhs.hostName)
                .append(this.statusPath, rhs.statusPath)
                .append(this.httpPort, rhs.httpPort)
                .append(this.httpsPort, rhs.httpsPort)
                .append(this.redirectPort, rhs.redirectPort)
                .append(this.shutdownPort, rhs.shutdownPort)
                .append(this.ajpPort, rhs.ajpPort)
                .append(this.systemsProperties, rhs.systemsProperties)
                .append(this.userName,rhs.userName)
                .append(this.encryptedPassword, rhs.encryptedPassword)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(jvmName)
                .append(hostName)
                .append(statusPath)
                .append(httpPort)
                .append(httpsPort)
                .append(redirectPort)
                .append(shutdownPort)
                .append(ajpPort)
                .append(systemsProperties)
                .append(userName)
                .append(encryptedPassword)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("jvmName", jvmName)
                .append("hostName", hostName)
                .append("statusPath", statusPath)
                .append("httpPort", httpPort)
                .append("httpsPort", httpsPort)
                .append("redirectPort", redirectPort)
                .append("shutdownPort", shutdownPort)
                .append("ajpPort", ajpPort)
                .append("systemProperties", systemsProperties)
                .append("userName",userName)
                .toString();
    }
}
