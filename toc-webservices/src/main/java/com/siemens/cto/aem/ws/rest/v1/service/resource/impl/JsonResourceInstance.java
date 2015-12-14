package com.siemens.cto.aem.ws.rest.v1.service.resource.impl;

import com.siemens.cto.aem.common.request.resource.ResourceInstanceRequest;
import com.siemens.cto.aem.ws.rest.v1.json.AbstractJsonDeserializer;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by z003e5zv on 3/16/2015.
 */
@JsonDeserialize(using = JsonResourceInstance.CreateResourceInstanceJSONDeserializer.class)
public class JsonResourceInstance {

    private static final Logger logger = Logger.getLogger(JsonResourceInstance.class);

    private final String resourceTypeName;
    private final String groupName;
    private final String name;
    private final Map<String, String> attributes;

    static final class CreateResourceInstanceJSONDeserializer extends AbstractJsonDeserializer<JsonResourceInstance> {
        @Override
        public JsonResourceInstance deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
            logger.debug("deserialize");
            final ObjectCodec obj = jp.getCodec();
            final JsonNode rootNode = obj.readTree(jp);
            final JsonNode resourceTypeNameNode = rootNode.get("resourceTypeName");
            final JsonNode groupNameNode = rootNode.get("groupName");
            final JsonNode nameNode = rootNode.get("name");
            final JsonNode attributesNode = rootNode.get("attributes");

            Map<String, String> attributesMap = null;
            if (!attributesNode.isNull()) {
                attributesMap = new HashMap<>();
                for (JsonNode node: attributesNode) {
                    attributesMap.put(node.get("key").getTextValue(), node.get("value").getTextValue());
                }
            }
            JsonResourceInstance results = new JsonResourceInstance(resourceTypeNameNode.getTextValue(), nameNode.getTextValue(), groupNameNode.getTextValue(), attributesMap);
            return results;
        }
    }
    public JsonResourceInstance(final String resourceTypeName, final String name, final String groupName, final Map<String, String> attributes) {
        this.resourceTypeName = resourceTypeName;
        this.groupName = groupName;
        this.name = name;
        this.attributes = attributes;
    }
    public String getName() {
        return this.name;
    }
    public String getResourceTypeName() {
        return resourceTypeName;
    }

    public String getGroupName() {
        return groupName;
    }

    public ResourceInstanceRequest getCommand() {
        return new ResourceInstanceRequest(this.resourceTypeName, this.name, this.groupName, attributes);
    }
}
