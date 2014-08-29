package com.siemens.cto.aem.ws.rest.v1.service.webserver.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.siemens.cto.aem.domain.model.path.FileSystemPath;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.id.IdentifierSetBuilder;
import com.siemens.cto.aem.domain.model.path.Path;
import com.siemens.cto.aem.domain.model.webserver.CreateWebServerCommand;
import com.siemens.cto.aem.ws.rest.v1.json.AbstractJsonDeserializer;

@JsonDeserialize(using = JsonCreateWebServer.JsonCreateWebServerDeserializer.class)
public class JsonCreateWebServer {

    private final Set<String> groupIds;
    private final String webserverName;
    private final String portNumber;
    private final String hostName;
    private final String httpsPort;
    private final String statusPath;
    private final String httpConfigFile;

    public JsonCreateWebServer(final String theName,
                               final String theHostName,
                               final String thePortNumber,
                               final String theHttpsPort,
                               final Set<String> theGroupIds,
                               final String theStatusPath,
                               final String theHttpConfigFile) {
        webserverName = theName;
        hostName = theHostName;
        portNumber = thePortNumber;
        httpsPort = theHttpsPort;
        groupIds = Collections.unmodifiableSet(new HashSet<>(theGroupIds));
        statusPath = theStatusPath;
        httpConfigFile = theHttpConfigFile;
    }

    public CreateWebServerCommand toCreateWebServerCommand() throws BadRequestException {
        final Set<Identifier<Group>> ids = new IdentifierSetBuilder(groupIds).build();

        final Integer port = convertFrom(portNumber,
                                         AemFaultType.INVALID_WEBSERVER_PORT);
        final Integer securePort = convertIfPresentFrom(httpsPort,
                                                        AemFaultType.INVALID_WEBSERVER_HTTPS_PORT,
                                                        null);

        return new CreateWebServerCommand(ids,
                                          webserverName,
                                          hostName,
                                          port,
                                          securePort,
                                          new Path(statusPath),
                                          new FileSystemPath(httpConfigFile));
    }

    private Integer convertFrom(final String aValue,
                                AemFaultType aFaultType) {
        try {
            return Integer.valueOf(aValue);
        } catch (final NumberFormatException nfe) {
            throw new BadRequestException(aFaultType,
                                          nfe.getMessage(),
                                          nfe);
        }
    }

    private Integer convertIfPresentFrom(final String aValue,
                                         final AemFaultType aFaultType,
                                         final Integer aDefault) {
        if (aValue != null && !"".equals(aValue.trim())) {
            return convertFrom(aValue,
                               aFaultType);
        }

        return aDefault;
    }

    static class JsonCreateWebServerDeserializer extends AbstractJsonDeserializer<JsonCreateWebServer> {
        public JsonCreateWebServerDeserializer() {
        }

        @Override
        public JsonCreateWebServer deserialize(final JsonParser jp, final DeserializationContext ctxt)
                throws IOException {

            final ObjectCodec obj = jp.getCodec();
            final JsonNode node = obj.readTree(jp).get(0);

            final JsonCreateWebServer jcws = new JsonCreateWebServer(node.get("webserverName").getTextValue(),
                                                                     node.get("hostName").getTextValue(),
                                                                     node.get("portNumber").getValueAsText(),
                                                                     node.get("httpsPort").getValueAsText(),
                                                                     deserializeGroupIdentifiers(node),
                                                                     node.get("statusPath").getTextValue(),
                                                                     node.get("httpConfigFile").getTextValue());
            return jcws;
        }
    }
}
