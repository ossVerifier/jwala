package com.siemens.cto.aem.ws.rest.v1.service.jvm.impl;

import com.siemens.cto.aem.common.request.jvm.UpdateJvmRequest;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.id.IdentifierSetBuilder;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.ws.rest.v1.json.AbstractJsonDeserializer;
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
                         final String theSystemProperties) {
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
    }

    public UpdateJvmRequest toUpdateJvmCommand() throws BadRequestException {

        final Identifier<Jvm> id = convertJvmId();
        final Set<Identifier<Group>> groupIds = convertGroupIds();

        return new UpdateJvmRequest(id,
                                    jvmName,
                                    hostName,
                                    groupIds,
                                    JsonUtilJvm.stringToInteger(httpPort),
                                    JsonUtilJvm.stringToInteger(httpsPort),
                                    JsonUtilJvm.stringToInteger(redirectPort),
                                    JsonUtilJvm.stringToInteger(shutdownPort),
                                    JsonUtilJvm.stringToInteger(ajpPort),
                                    new Path(statusPath),
                                    systemProperties
        );
    }

    protected Identifier<Jvm> convertJvmId() {
        try {
            return new Identifier<>(jvmId);
        } catch (final NumberFormatException nfe) {
            throw new BadRequestException(AemFaultType.INVALID_IDENTIFIER,
                                          nfe.getMessage(),
                                          nfe);
        }
    }

    protected Set<Identifier<Group>> convertGroupIds() {
        return new IdentifierSetBuilder(groupIds).build();
    }

    static class JsonUpdateJvmDeserializer extends AbstractJsonDeserializer<JsonUpdateJvm> {

        public JsonUpdateJvmDeserializer() {
        }

        @Override
        public JsonUpdateJvm deserialize(final JsonParser jp,
                                         final DeserializationContext ctxt) throws IOException {

            final ObjectCodec obj = jp.getCodec();
            final JsonNode node = obj.readTree(jp);

            return new JsonUpdateJvm(node.get("jvmId").getValueAsText(),
                                     node.get("jvmName").getTextValue(),
                                     node.get("hostName").getTextValue(),
                                     deserializeGroupIdentifiers(node),
                                     node.get("httpPort").getValueAsText(),
                                     node.get("httpsPort").getValueAsText(),
                                     node.get("redirectPort").getValueAsText(),
                                     node.get("shutdownPort").getValueAsText(),
                                     node.get("ajpPort").getValueAsText(),
                                     node.get("statusPath").getTextValue(),
                                     node.get("systemProperties").getTextValue());
        }
    }
}
