package com.siemens.cto.aem.ws.rest.v1.service.webserver.impl;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.webserver.CreateWebServerCommand;

@JsonDeserialize(using = JsonCreateWebServer.JsonCreateWebServerDeserializer.class)
public class JsonCreateWebServer {

    private String groupId;
    private String webserverName;
    private Integer portNumber;
    private String hostName;

    public JsonCreateWebServer() {
    }

    public JsonCreateWebServer(final String aGroupId,
                         final String aWebServerName,
                         final String aHostName,
                         final Integer aPortNumber) {
        groupId = aGroupId;
        webserverName = aWebServerName;
        hostName = aHostName;
        portNumber = aPortNumber;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(final String aGroupId) {
        groupId = aGroupId;
    }

    public String getWebServerName() {
        return webserverName;
    }

    public void setWebServerName(final String aWebServerName) {
        webserverName = aWebServerName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(final String aHostName) {
        hostName = aHostName;
    }

    public CreateWebServerCommand toCreateWebServerCommand() throws BadRequestException {

        try {
            return new CreateWebServerCommand(new Identifier<Group>(groupId),
                                        webserverName,
                                        hostName,
                                        portNumber);
        } catch (final NumberFormatException nfe) {
            throw new BadRequestException(AemFaultType.INVALID_IDENTIFIER,
                                          nfe.getMessage(),
                                          nfe);
        }
    }

    static class JsonCreateWebServerDeserializer extends JsonDeserializer<JsonCreateWebServer> {

        public JsonCreateWebServerDeserializer() {
        }

        @Override
        public JsonCreateWebServer deserialize(final JsonParser jp,
                                           final DeserializationContext ctxt) throws IOException, JsonProcessingException {

            final ObjectCodec obj = jp.getCodec();
            final JsonNode node = obj.readTree(jp).get(0);

            return new JsonCreateWebServer(node.get("groupId").getValueAsText(),
                                     node.get("webserverName").getTextValue(),
                                     node.get("hostName").getTextValue(),
                                     node.get("portNumber").getIntValue()
                                     );
        }
    }

	public Integer getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(Integer portNumber) {
		this.portNumber = portNumber;
	}
}
