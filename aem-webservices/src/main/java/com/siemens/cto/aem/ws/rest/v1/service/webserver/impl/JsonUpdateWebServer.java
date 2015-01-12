package com.siemens.cto.aem.ws.rest.v1.service.webserver.impl;

import java.io.IOException;
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
import com.siemens.cto.aem.domain.model.webserver.UpdateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.ws.rest.v1.json.AbstractJsonDeserializer;

@JsonDeserialize(using = JsonUpdateWebServer.JsonUpdateWebServerDeserializer.class)
public class JsonUpdateWebServer {

    private final Set<String> groupIds;
    private final String webServerId;
    private final String webServerName;
    private final String portNumber;
    private final String httpsPort;
    private final String hostName;
    private final String statusPath;
    private final String httpConfigFile;

    public JsonUpdateWebServer(final String aWebServerId,
                               final String aWebServerName,
                               final String aHostName,
                               final String aPortNumber,
                               final String aHttpsPort,
                               final Set<String> someGroupIds,
                               final String aStatusPath,
                               final String aHttpConfigFile) {

        webServerName = aWebServerName;
        hostName = aHostName;
        portNumber = aPortNumber;
        httpsPort = aHttpsPort;
        webServerId = aWebServerId;
        groupIds = someGroupIds;
        statusPath = aStatusPath;
        httpConfigFile = aHttpConfigFile;
    }

    public UpdateWebServerCommand toUpdateWebServerCommand() throws BadRequestException {

        final Set<Identifier<Group>> groups = new IdentifierSetBuilder(groupIds).build();
        final Identifier<WebServer> webServerId = convertWebServerId();
        final Integer port = convertPortNumber();
        final Integer httpsPort = convertHttpsPortNumber();
        return new UpdateWebServerCommand(webServerId,
                                          groups,
                                          webServerName,
                                          hostName,
                                          port,
                                          httpsPort,
                                          new Path(statusPath),
                                          new FileSystemPath(httpConfigFile));
    }

    protected Identifier<WebServer> convertWebServerId() {
        try {
            return new Identifier<>(webServerId);
        } catch (final NumberFormatException nfe) {
            throw new BadRequestException(AemFaultType.INVALID_IDENTIFIER, nfe.getMessage(), nfe);
        }
    }

    protected Integer convertPortNumber() {
        try {
            return Integer.valueOf(portNumber);
        } catch (final NumberFormatException nfe) {
            throw new BadRequestException(AemFaultType.INVALID_WEBSERVER_PORT, nfe.getMessage(), nfe);
        }
    }

    protected Integer convertHttpsPortNumber() {
        try {
            if (httpsPort != null && !"".equals(httpsPort.trim())) {
                return Integer.valueOf(httpsPort);
            }
            return null;
        } catch (final NumberFormatException nfe) {
            throw new BadRequestException(AemFaultType.INVALID_WEBSERVER_HTTPS_PORT, nfe.getMessage(), nfe);
        }
    }

    static class JsonUpdateWebServerDeserializer extends AbstractJsonDeserializer<JsonUpdateWebServer> {

        public JsonUpdateWebServerDeserializer() {
        }
        @Override
        public JsonUpdateWebServer deserialize(final JsonParser jp, final DeserializationContext ctxt)
                throws IOException {

            final ObjectCodec obj = jp.getCodec();
            final JsonNode node = obj.readTree(jp).get(0);

            final Set<String> groupIds = deserializeGroupIdentifiers(node);
            final JsonUpdateWebServer juws =
                    new JsonUpdateWebServer(node.get("webserverId").getValueAsText(),
                                            node.get("webserverName").getTextValue(),
                                            node.get("hostName").getTextValue(),
                                            node.get("portNumber").getValueAsText(),
                                            node.get("httpsPort").getValueAsText(),
                                            groupIds,
                                            node.get("statusPath").getTextValue(),
                                            node.get("httpConfigFile").getTextValue());
            return juws;
        }
    }
}