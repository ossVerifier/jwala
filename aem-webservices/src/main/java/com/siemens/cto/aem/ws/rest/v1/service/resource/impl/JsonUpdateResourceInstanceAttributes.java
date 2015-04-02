package com.siemens.cto.aem.ws.rest.v1.service.resource.impl;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.domain.model.resource.command.UpdateResourceInstanceAttributesCommand;
import com.siemens.cto.aem.ws.rest.v1.json.AbstractJsonDeserializer;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by z003e5zv on 3/16/2015.
 */
@JsonDeserialize(using = JsonUpdateResourceInstanceAttributes.UpdateResourceInstanceJSONDeserializer.class)
public class JsonUpdateResourceInstanceAttributes {

    private static final Logger LOGGER = Logger.getLogger(JsonUpdateResourceInstanceAttributes.class);

    private String resourceInstanceId;

    private Map<String, String> attributes;

    static final class UpdateResourceInstanceJSONDeserializer extends AbstractJsonDeserializer<JsonUpdateResourceInstanceAttributes> {
        @Override
        public JsonUpdateResourceInstanceAttributes deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
            final ObjectCodec obj = jp.getCodec();
            final JsonNode rootNode = obj.readTree(jp);
            final JsonNode resourceIdNode = rootNode.get("resourceInstanceId");
            final JsonNode attributesNode = rootNode.get("attributes");
            Iterator<JsonNode> attributesIt = attributesNode.getElements();
            Map<String, String> attributes = new HashMap<>();
            while (attributesIt.hasNext()) {
                JsonNode attributesEntry = attributesIt.next();
                attributes.put((attributesEntry.get("key")).getTextValue(), (attributesEntry.get("value")).getTextValue());
            }
            return new JsonUpdateResourceInstanceAttributes(resourceIdNode.getTextValue(), attributes);
        }
    }
    public JsonUpdateResourceInstanceAttributes(String resourceInstanceId, Map<String, String> attributes) {
        this.resourceInstanceId = resourceInstanceId;
        this.attributes = attributes;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public String getResourceInstanceId() {
        return resourceInstanceId;
    }

    public UpdateResourceInstanceAttributesCommand getComand() {
        Long resourceInstanceId = null;
        try {
            resourceInstanceId =  Long.valueOf(this.getResourceInstanceId());
        } catch (NumberFormatException nfe) {
            LOGGER.info("Unable to convert String to Long", nfe);
        }
        return new UpdateResourceInstanceAttributesCommand(new Identifier<ResourceInstance>(resourceInstanceId), this.getAttributes());
    }
}
