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
import com.siemens.cto.aem.domain.model.webserver.UpdateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.WebServer;

@JsonDeserialize(using = JsonUpdateWebServer.JsonUpdateWebServerDeserializer.class)
public class JsonUpdateWebServer {

	private String groupId;
	private String webServerId;
	private String webserverName;
	private Integer portNumber;
	private String hostName;

	public JsonUpdateWebServer() {
	}

	public JsonUpdateWebServer(final String aWebServerId,
			final String aGroupId, final String aWebServerName,
			final String aHostName, final Integer aPortNumber) {
		groupId = aGroupId;
		webserverName = aWebServerName;
		hostName = aHostName;
		portNumber = aPortNumber;
		webServerId = aWebServerId;
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

	public UpdateWebServerCommand toUpdateWebServerCommand()
			throws BadRequestException {

		try {
			return new UpdateWebServerCommand(new Identifier<WebServer>(
					webServerId), new Identifier<Group>(groupId),
					webserverName, hostName, portNumber);
		} catch (final NumberFormatException nfe) {
			throw new BadRequestException(AemFaultType.INVALID_IDENTIFIER,
					nfe.getMessage(), nfe);
		}
	}

	static class JsonUpdateWebServerDeserializer extends
			JsonDeserializer<JsonUpdateWebServer> {

		public JsonUpdateWebServerDeserializer() {
		}

		@Override
		public JsonUpdateWebServer deserialize(final JsonParser jp,
				final DeserializationContext ctxt) throws IOException,
				JsonProcessingException {

			final ObjectCodec obj = jp.getCodec();
			final JsonNode node = obj.readTree(jp).get(0);

			return new JsonUpdateWebServer(
					node.get("webserverId").getValueAsText(), 
					node.get("groupId").getValueAsText(),
					node.get("webserverName").getTextValue(), 
					node.get("hostName").getTextValue(), 
					node.get("portNumber").getIntValue());
		}
	}

	public Integer getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(Integer portNumber) {
        this.portNumber = portNumber;
    }
}
