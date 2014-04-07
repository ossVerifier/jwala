package com.siemens.cto.aem.ws.rest.v1.service.webserver.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    private List<String> groupIds = new ArrayList<>(1);
    private String webserverName;
    private Integer portNumber;
    private String hostName;

    public JsonCreateWebServer() {
    }

    public JsonCreateWebServer(
                         final String aWebServerName,
                         final String aHostName,
                         final String aPortNumber) {
        webserverName = aWebServerName;
        hostName = aHostName;
        portNumber = Integer.parseInt(aPortNumber);
    }

    public void addGroupId(final String aGroupId) {
        groupIds.add(aGroupId);
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

        List<Identifier<Group>> ids = new ArrayList<Identifier<Group>>(groupIds.size());
        try {
            return new CreateWebServerCommand(ids,
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

            JsonCreateWebServer jcws = new 
                 JsonCreateWebServer(node.get("webserverName").getTextValue(),
                                     node.get("hostName").getTextValue(),
                                     node.get("portNumber").getValueAsText()
                                     );

            final JsonNode groupNode = node.get("groupIds");
            if(groupNode != null) {
                Iterator<JsonNode> groupIt = groupNode.getElements();
                while(groupIt.hasNext()) {
                    JsonNode groupEntry = groupIt.next();
                    jcws.addGroupId(groupEntry.get("groupId").getValueAsText());
                }
            } else if(node.get("groupId") != null) {
                jcws.addGroupId(node.get("groupId").getValueAsText());
            }
            return jcws;
        }
    }

	public Integer getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(Integer portNumber) {
		this.portNumber = portNumber;
	}

    @SuppressWarnings({"PMD.CyclomaticComplexity","PMD.InsufficientBranchCoverage"})
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((groupIds == null) ? 0 : groupIds.hashCode());
        result = prime * result + ((hostName == null) ? 0 : hostName.hashCode());
        result = prime * result + ((portNumber == null) ? 0 : portNumber.hashCode());
        result = prime * result + ((webserverName == null) ? 0 : webserverName.hashCode());
        return result;
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity","PMD.InsufficientBranchCoverage"})
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        JsonCreateWebServer other = (JsonCreateWebServer) obj;
        if (groupIds == null) {
            if (other.groupIds != null) {
                return false;
            }
        } else if (!groupIds.equals(other.groupIds)) {
            return false;
        }
        if (hostName == null) {
            if (other.hostName != null) {
                return false;
            }
        } else if (!hostName.equals(other.hostName)) {
            return false;
        }
        if (portNumber == null) {
            if (other.portNumber != null) {
                return false;
            }
        } else if (!portNumber.equals(other.portNumber)) {
            return false;
        }
        if (webserverName == null) {
            if (other.webserverName != null) {
                return false;
            }
        } else if (!webserverName.equals(other.webserverName)) {
            return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity","PMD.InsufficientBranchCoverage"})
    public String toString() {
        return "JsonCreateWebServer {groupIds=" + groupIds + ", webserverName=" + webserverName + ", portNumber="
                + portNumber + ", hostName=" + hostName + "}";
    }


}
