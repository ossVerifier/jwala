package com.siemens.cto.aem.ws.rest.v1.service.webserver.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.id.IdentifierSetBuilder;
import com.siemens.cto.aem.domain.model.webserver.CreateWebServerCommand;
import com.siemens.cto.aem.ws.rest.v1.json.AbstractJsonDeserializer;

@JsonDeserialize(using = JsonCreateWebServer.JsonCreateWebServerDeserializer.class)
public class JsonCreateWebServer {

    private final Set<String> groupIds;
    private String webserverName;
    private String portNumber;
    private String hostName;

    public JsonCreateWebServer(final String theName, final String theHostName, final String thePortNumber,
            Set<String> theGroupIds) {
        webserverName = theName;
        hostName = theHostName;
        portNumber = thePortNumber;
        groupIds = Collections.unmodifiableSet(new HashSet<>(theGroupIds));
    }

    public CreateWebServerCommand toCreateWebServerCommand() throws BadRequestException {
        final Set<Identifier<Group>> ids = new IdentifierSetBuilder(groupIds).build();

        Integer port = null;
        try {
            port = Integer.valueOf(portNumber);
        } catch (final NumberFormatException nfe) {
            throw new BadRequestException(AemFaultType.INVALID_WEBSERVER_PORT, nfe.getMessage(), nfe);
        }
        return new CreateWebServerCommand(ids, webserverName, hostName, port);
    }

    static class JsonCreateWebServerDeserializer extends AbstractJsonDeserializer<JsonCreateWebServer> {
        public JsonCreateWebServerDeserializer() {
        }

        @Override
        public JsonCreateWebServer deserialize(final JsonParser jp, final DeserializationContext ctxt)
                throws IOException, JsonProcessingException {

            final ObjectCodec obj = jp.getCodec();
            final JsonNode node = obj.readTree(jp).get(0);

            final JsonCreateWebServer jcws = new JsonCreateWebServer(node.get("webserverName").getTextValue(),
                                                                     node.get("hostName").getTextValue(),
                                                                     node.get("portNumber").getValueAsText(),
                                                                     deserializeGroupIdentifiers(node));
            return jcws;
        }
    }
}
