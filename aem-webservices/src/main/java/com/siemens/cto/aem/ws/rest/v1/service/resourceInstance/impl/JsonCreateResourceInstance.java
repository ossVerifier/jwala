package com.siemens.cto.aem.ws.rest.v1.service.resourceInstance.impl;

import com.siemens.cto.aem.domain.model.resource.command.CreateResourceInstanceCommand;
import com.siemens.cto.aem.ws.rest.v1.json.AbstractJsonDeserializer;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by z003e5zv on 3/16/2015.
 */
@JsonDeserialize(using = JsonCreateResourceInstance.CreateResourceInstanceJSONDeserializer.class)
public class JsonCreateResourceInstance {

    private String resourceTypeName;
    private String jvmId;
    private String groupId;
    private Map<String, String> attributes;

    static final class CreateResourceInstanceJSONDeserializer extends AbstractJsonDeserializer<JsonCreateResourceInstance> {
        @Override
        public JsonCreateResourceInstance deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
            final ObjectCodec obj = jp.getCodec();
            final JsonNode rootNode = obj.readTree(jp);
            final JsonNode resourceTypeNameNode = rootNode.get("resourceTypeName");
            final JsonNode jvmIdNode = rootNode.get("jvmId");
            final JsonNode groupIdNode = rootNode.get("groupId");
            final JsonNode attributesNode = rootNode.get("attributes");
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.readValue(attributesNode.traverse(), HashMap.class);

            Map<String, String> attributes = new HashMap<>();
            attributes.put("key","value");
            attributes.put("key1","value1");
            attributes.put("key2","value2");
            JsonCreateResourceInstance results = new JsonCreateResourceInstance(resourceTypeNameNode.getTextValue(), "dummyJvm", "dummyGroupId", attributes);
            return results;
        }
    }
    public JsonCreateResourceInstance(String resourceTypeName, String jvmId, String groupId, Map<String, String> attributes) {
        this.resourceTypeName = resourceTypeName;
        this.jvmId = jvmId;
        this.groupId = groupId;
        this.attributes = attributes;
    }
    public String getResourceTypeName() {
        return resourceTypeName;
    }

    public String getJvmId() {
        return jvmId;
    }

    public String getGroupId() {
        return groupId;
    }
}
