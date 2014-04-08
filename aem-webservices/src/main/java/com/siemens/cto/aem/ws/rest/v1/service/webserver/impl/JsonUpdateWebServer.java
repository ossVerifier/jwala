package com.siemens.cto.aem.ws.rest.v1.service.webserver.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import com.siemens.cto.aem.domain.model.webserver.UpdateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.ws.rest.v1.json.AbstractJsonDeserializer;

@JsonDeserialize(using = JsonUpdateWebServer.JsonUpdateWebServerDeserializer.class)
public class JsonUpdateWebServer {

	private List<String> groupIds = new ArrayList<>(1);
	private String webServerId;
	private String webserverName;
	private Integer portNumber;
	private String hostName;

	public JsonUpdateWebServer() {
	}

    public JsonUpdateWebServer(final String aWebServerId,
            final String aWebServerName,
            final String aHostName, final String aPortNumber) {
        
        webserverName = aWebServerName;
        hostName = aHostName;
        portNumber = Integer.parseInt(aPortNumber);
        webServerId = aWebServerId;
    }

    public void addGroupId(String gid) {
        groupIds.add(gid);
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
		    List<Identifier<Group> > groups= new ArrayList<>(groupIds.size());
		    for(String grp : groupIds) {
		        groups.add(Identifier.id(Long.parseLong(grp), Group.class));
		    }
			return new UpdateWebServerCommand(new Identifier<WebServer>(
					webServerId), groups,
					webserverName, hostName, portNumber);
		} catch (final NumberFormatException nfe) {
			throw new BadRequestException(AemFaultType.INVALID_IDENTIFIER,
					nfe.getMessage(), nfe);
		}
	}

	static class JsonUpdateWebServerDeserializer extends
			AbstractJsonDeserializer<JsonUpdateWebServer> {

		public JsonUpdateWebServerDeserializer() {
		}

		@Override
		public JsonUpdateWebServer deserialize(final JsonParser jp,
				final DeserializationContext ctxt) throws IOException,
				JsonProcessingException {

			final ObjectCodec obj = jp.getCodec();
			final JsonNode node = obj.readTree(jp).get(0);

			JsonUpdateWebServer juws = new JsonUpdateWebServer(
					node.get("webserverId").getValueAsText(), 
					node.get("webserverName").getTextValue(), 
					node.get("hostName").getTextValue(), 
					node.get("portNumber").getValueAsText());
			
            for(String aGroupId : deserializeGroupIdentifiers(node)) {
                juws.addGroupId(aGroupId);
            }

            return juws;
		}
	}

	public Integer getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(Integer portNumber) {
        this.portNumber = portNumber;
    }

    @SuppressWarnings({"PMD.InsufficientBranchCoverage"})
    @Override
    public String toString() {
        return "JsonUpdateWebServer {groupIds=" + groupIds + ", webServerId=" + webServerId + ", webserverName="
                + webserverName + ", portNumber=" + portNumber + ", hostName=" + hostName + "}";
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity","PMD.InsufficientBranchCoverage"})
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((groupIds == null) ? 0 : groupIds.hashCode());
        result = prime * result + ((hostName == null) ? 0 : hostName.hashCode());
        result = prime * result + ((portNumber == null) ? 0 : portNumber.hashCode());
        result = prime * result + ((webServerId == null) ? 0 : webServerId.hashCode());
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
        JsonUpdateWebServer other = (JsonUpdateWebServer) obj;
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
        if (webServerId == null) {
            if (other.webServerId != null) {
                return false;
            }
        } else if (!webServerId.equals(other.webServerId)) {
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
    
	
}
