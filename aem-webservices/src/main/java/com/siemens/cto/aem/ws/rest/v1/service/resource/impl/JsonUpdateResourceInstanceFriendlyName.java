package com.siemens.cto.aem.ws.rest.v1.service.resource.impl;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.domain.model.resource.command.UpdateResourceInstanceNameCommand;
import com.siemens.cto.aem.ws.rest.v1.json.AbstractJsonDeserializer;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.io.IOException;

/**
 * Created by z003e5zv on 3/16/2015.
 */
@JsonDeserialize(using = JsonUpdateResourceInstanceFriendlyName.UpdateResourceInstanceJSONDeserializer.class)
public class JsonUpdateResourceInstanceFriendlyName {

    private static final Logger LOGGER = Logger.getLogger(JsonUpdateResourceInstanceFriendlyName.class);

    public String getResourceInstanceId() {
        return resourceInstanceId;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    private String resourceInstanceId;
    private String friendlyName;

    static final class UpdateResourceInstanceJSONDeserializer extends AbstractJsonDeserializer<JsonUpdateResourceInstanceFriendlyName> {
        @Override
        public JsonUpdateResourceInstanceFriendlyName deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
            final ObjectCodec obj = jp.getCodec();
            final JsonNode rootNode = obj.readTree(jp);
            final JsonNode resourceInstanceIdNode = rootNode.get("resourceInstanceId");
            final JsonNode friendlyNameNode = rootNode.get("friendlyName");
            return new JsonUpdateResourceInstanceFriendlyName(resourceInstanceIdNode.getTextValue(), friendlyNameNode.getTextValue());
        }
    }
    public JsonUpdateResourceInstanceFriendlyName(String resourceInstanceId, String friendlyName) {
        this.resourceInstanceId = resourceInstanceId;
        this.friendlyName = friendlyName;
    }

    public UpdateResourceInstanceNameCommand parseCommand() {
        Long resourceInstanceId = null;
        try {
            resourceInstanceId =  Long.valueOf(this.getResourceInstanceId());
        } catch (NumberFormatException nfe) {
            LOGGER.info("Unable to convert String to Long", nfe);
        }
        return new UpdateResourceInstanceNameCommand(new Identifier<ResourceInstance>(resourceInstanceId), friendlyName);
    }
}
