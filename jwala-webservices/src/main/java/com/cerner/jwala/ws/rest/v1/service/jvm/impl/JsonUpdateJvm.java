package com.cerner.jwala.ws.rest.v1.service.jvm.impl;

import com.cerner.jwala.common.domain.model.fault.FaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.id.IdentifierSetBuilder;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.path.Path;
import com.cerner.jwala.common.domain.model.ssh.DecryptPassword;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.request.jvm.UpdateJvmRequest;
import com.cerner.jwala.ws.rest.v1.json.AbstractJsonDeserializer;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@JsonDeserialize(using = JsonUpdateJvm.JsonUpdateJvmDeserializer.class)
public class JsonUpdateJvm {

    private final String jvmId;
    private final String jvmName;
    private final String hostName;
    private final String statusPath;
    private final Set<String> groupIds;
    private final String httpPort;
    private final String httpsPort;
    private final String redirectPort;
    private final String shutdownPort;
    private final String ajpPort;
    private final String systemProperties;
    private final String userName;
    private final String encryptedPassword;
    private final String jdkVersion;
    private final String tomcatVersion;

    public JsonUpdateJvm(final String theJvmId,
                         final String theJvmName,
                         final String theHostName,
                         final Set<String> someGroupIds,
                         final String theHttpPort,
                         final String theHttpsPort,
                         final String theRedirectPort,
                         final String theShutdownPort,
                         final String theAjpPort,
                         final String theStatusPath,
                         final String theSystemProperties,
                         final String theUserName,
                         final String theEncryptedPassword,
                         final String theJdkVersion,
                         final String theTomcatVersion) {
        jvmId = theJvmId;
        jvmName = theJvmName;
        hostName = theHostName;
        groupIds = Collections.unmodifiableSet(new HashSet<>(someGroupIds));
        httpPort = theHttpPort;
        httpsPort = theHttpsPort;
        redirectPort = theRedirectPort;
        shutdownPort = theShutdownPort;
        ajpPort = theAjpPort;
        statusPath = theStatusPath;
        systemProperties = theSystemProperties;
        userName = theUserName;
        encryptedPassword = theEncryptedPassword;
        jdkVersion = theJdkVersion;
        tomcatVersion = theTomcatVersion;
    }

    public UpdateJvmRequest toUpdateJvmRequest() throws BadRequestException {

        return new UpdateJvmRequest(convertJvmId(),
                jvmName,
                hostName,
                convertGroupIds(),
                JsonUtilJvm.stringToInteger(httpPort),
                JsonUtilJvm.stringToInteger(httpsPort),
                JsonUtilJvm.stringToInteger(redirectPort),
                JsonUtilJvm.stringToInteger(shutdownPort),
                JsonUtilJvm.stringToInteger(ajpPort),
                new Path(statusPath),
                systemProperties,
                userName,
                encryptedPassword,
                jdkVersion,
                tomcatVersion);
    }

    protected Identifier<Jvm> convertJvmId() {
        try {
            return new Identifier<>(jvmId);
        } catch (final NumberFormatException nfe) {
            throw new BadRequestException(FaultType.INVALID_IDENTIFIER,
                                          nfe.getMessage(),
                                          nfe);
        }
    }

    protected Set<Identifier<Group>> convertGroupIds() {
        return new IdentifierSetBuilder(groupIds).build();
    }

    @Override
    public String toString() {
        return "JsonUpdateJvm{" +
                "jvmId='" + jvmId + '\'' +
                ", jvmName='" + jvmName + '\'' +
                ", hostName='" + hostName + '\'' +
                ", statusPath='" + statusPath + '\'' +
                ", groupIds=" + groupIds +
                ", httpPort='" + httpPort + '\'' +
                ", httpsPort='" + httpsPort + '\'' +
                ", redirectPort='" + redirectPort + '\'' +
                ", shutdownPort='" + shutdownPort + '\'' +
                ", ajpPort='" + ajpPort + '\'' +
                ", systemProperties='" + systemProperties + '\'' +
                ", userName='" + userName + '\'' +
                ", encryptedPassword='" + encryptedPassword + '\'' +
                '}';
    }

    static class JsonUpdateJvmDeserializer extends AbstractJsonDeserializer<JsonUpdateJvm> {

        public JsonUpdateJvmDeserializer() {
        }

        @Override
        public JsonUpdateJvm deserialize(final JsonParser jp,
                                         final DeserializationContext ctxt) throws IOException {

            final ObjectCodec obj = jp.getCodec();
            final JsonNode node = obj.readTree(jp);
            final JsonNode usernameNode = node.get("userName");
            final JsonNode passwordNode = node.get("encryptedPassword");

            final String pw = (passwordNode == null) ? null : new DecryptPassword().encrypt(passwordNode.getTextValue());

            return new JsonUpdateJvm(node.get("jvmId").getTextValue(),
                    node.get("jvmName").getTextValue(),
                    node.get("hostName").getTextValue(),
                    deserializeGroupIdentifiers(node),
                    node.get("httpPort").getTextValue(),
                    node.get("httpsPort").getTextValue(),
                    node.get("redirectPort").getTextValue(),
                    node.get("shutdownPort").getTextValue(),
                    node.get("ajpPort").getTextValue(),
                    node.get("statusPath").getTextValue(),
                    node.get("systemProperties").getTextValue(),
                    usernameNode == null ? null : node.get("userName").getTextValue(),
                    pw,
                    node.get("jdkVersion").getTextValue(),
                    node.get("tomcatVersion").getTextValue());
        }
    }
}
