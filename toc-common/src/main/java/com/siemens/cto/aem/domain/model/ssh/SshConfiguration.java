package com.siemens.cto.aem.domain.model.ssh;

import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.io.Serializable;

public class SshConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SshConfiguration.class);

    private final String userName;
    private final Integer port;
    private final String privateKeyFile;
    private final String knownHostsFile;
    private final String iAmNotThePasswordYoureLookingFor;
    private final Expression decryptExpression;
    private final String encryptExpressionString = "new com.siemens.cto.infrastructure.StpCryptoService().decryptBase64( #stringToDecrypt )";
    private final SpelExpressionParser expressionParser;

    public SshConfiguration(final String theUserName,
                            final Integer thePort,
                            final String thePrivateKeyFile,
                            final String theKnownHostsFile,
                            final String theEncPassword) {


        if (theUserName == null
                || thePort == null
                || thePrivateKeyFile == null
                || theKnownHostsFile == null) {
            String message = "Startup Aborted: Aem SSH Properties Not Set in Application Properties file";
            LOGGER.error(message);
            throw new InternalErrorException(AemFaultType.SSH_CONFIG_MISSING, message);
        }
        userName = theUserName;
        port = thePort;
        privateKeyFile = thePrivateKeyFile;
        knownHostsFile = theKnownHostsFile;
        if (theEncPassword == null) {
            decryptExpression = null;
            expressionParser = null;
            iAmNotThePasswordYoureLookingFor = null;
        } else {
            expressionParser = new SpelExpressionParser();
            decryptExpression = expressionParser.parseExpression(encryptExpressionString);

            // crypto bash
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariable("stringToDecrypt", theEncPassword);
            iAmNotThePasswordYoureLookingFor = decryptExpression.getValue(context, String.class);
        }
    }

    public String getUserName() {
        return userName;
    }

    public Integer getPort() {
        return port;
    }

    public String getPrivateKeyFile() {
        return privateKeyFile;
    }

    public String getKnownHostsFile() {
        return knownHostsFile;
    }

    public String getPassword() { return iAmNotThePasswordYoureLookingFor; }

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
        SshConfiguration rhs = (SshConfiguration) obj;
        return new EqualsBuilder()
                .append(this.userName, rhs.userName)
                .append(this.port, rhs.port)
                .append(this.privateKeyFile, rhs.privateKeyFile)
                .append(this.knownHostsFile, rhs.knownHostsFile)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(userName)
                .append(port)
                .append(privateKeyFile)
                .append(knownHostsFile)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("userName", userName)
                .append("port", port)
                .append("privateKeyFile", privateKeyFile)
                .append("knownHostsFile", knownHostsFile)
                .toString();
    }

}
