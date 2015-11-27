package com.siemens.cto.aem.ws.rest.v1.service.jvm.impl;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.id.IdentifierSetBuilder;
import com.siemens.cto.aem.domain.command.jvm.CreateJvmAndAddToGroupsCommand;
import com.siemens.cto.aem.domain.command.jvm.CreateJvmCommand;
import com.siemens.cto.aem.domain.model.path.Path;
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

@JsonDeserialize(using = JsonCreateJvm.JsonCreateJvmDeserializer.class)
public class JsonCreateJvm {

    private final String jvmName;
    private final String hostName;
    private final String httpPort;
    private final String httpsPort;
    private final String redirectPort;
    private final String shutdownPort;
    private final String ajpPort;
    private final String statusPath;
    private final String systemProperties;

    private final Set<String> groupIds;

    public JsonCreateJvm(final String theJvmName,
                         final String theHostName,
                         final String theHttpPort,
                         final String theHttpsPort,
                         final String theRedirectPort,
                         final String theShutdownPort,
                         final String theAjpPort,
                         final String theStatusPath,
                         final String theSystemProperties) {
        this(theJvmName,
             theHostName,
             Collections.<String>emptySet(),
             theHttpPort,
             theHttpsPort,
             theRedirectPort,
             theShutdownPort,
             theAjpPort,
             theStatusPath,
             theSystemProperties);
    }

    public JsonCreateJvm(final String theJvmName,
                         final String theHostName,
                         final Set<String> someGroupIds,
                         final String theHttpPort,
                         final String theHttpsPort,
                         final String theRedirectPort,
                         final String theShutdownPort,
                         final String theAjpPort,
                         final String theStatusPath,
                         final String theSystemProperties) {
        jvmName = theJvmName;
        hostName = theHostName;
        httpPort = theHttpPort;
        httpsPort = theHttpsPort;
        redirectPort = theRedirectPort;
        shutdownPort = theShutdownPort;
        ajpPort = theAjpPort;
        statusPath = theStatusPath;
        systemProperties = theSystemProperties;
        groupIds = Collections.unmodifiableSet(new HashSet<>(someGroupIds));
    }

    public boolean areGroupsPresent() {
        return !groupIds.isEmpty();
    }

    public CreateJvmCommand toCreateJvmCommand() throws BadRequestException {

        return new CreateJvmCommand(jvmName,
                                    hostName,
                                    JsonUtilJvm.stringToInteger(httpPort),
                                    JsonUtilJvm.stringToInteger(httpsPort),
                                    JsonUtilJvm.stringToInteger(redirectPort),
                                    JsonUtilJvm.stringToInteger(shutdownPort),
                                    JsonUtilJvm.stringToInteger(ajpPort),
                                    new Path(statusPath),
                                    systemProperties);
    }

    public CreateJvmAndAddToGroupsCommand toCreateAndAddCommand() throws BadRequestException {
        final Set<Identifier<Group>> groups = convertGroupIds();

        return new CreateJvmAndAddToGroupsCommand(jvmName,
                                                  hostName,
                                                  groups,
                                                  JsonUtilJvm.stringToInteger(httpPort),
                                                  JsonUtilJvm.stringToInteger(httpsPort),
                                                  JsonUtilJvm.stringToInteger(redirectPort),
                                                  JsonUtilJvm.stringToInteger(shutdownPort),
                                                  JsonUtilJvm.stringToInteger(ajpPort),
                                                  new Path(statusPath),
                                                  systemProperties);
    }

    protected Set<Identifier<Group>> convertGroupIds() {
        return new IdentifierSetBuilder(groupIds).build();
    }

    static class JsonCreateJvmDeserializer extends AbstractJsonDeserializer<JsonCreateJvm> {

        public JsonCreateJvmDeserializer() {
        }

        @Override
        public JsonCreateJvm deserialize(final JsonParser jp,
                                         final DeserializationContext ctxt) throws IOException {

            final ObjectCodec obj = jp.getCodec();
            final JsonNode rootNode = obj.readTree(jp);
            final JsonNode jvmNode = rootNode.get("jvmName");
            final JsonNode hostNameNode = rootNode.get("hostName");

            final JsonNode httpPortNode = rootNode.get("httpPort");
            final JsonNode httpsPortNode = rootNode.get("httpsPort");
            final JsonNode redirectPortNode = rootNode.get("redirectPort");
            final JsonNode shutdownPortNode = rootNode.get("shutdownPort");
            final JsonNode ajpPortNode = rootNode.get("ajpPort");
            final JsonNode statusPathNode = rootNode.get("statusPath");
            final JsonNode systemProperties = rootNode.get("systemProperties");

            final Set<String> rawGroupIds = deserializeGroupIdentifiers(rootNode);

            return new JsonCreateJvm(jvmNode.getTextValue(),
                                     hostNameNode.getTextValue(),
                                     rawGroupIds,
                                     httpPortNode.getValueAsText(),
                                     httpsPortNode.getValueAsText(),
                                     redirectPortNode.getValueAsText(),
                                     shutdownPortNode.getValueAsText(),
                                     ajpPortNode.getValueAsText(),
                                     statusPathNode.getTextValue(),
                                     systemProperties.getTextValue());
        }
    }
}
