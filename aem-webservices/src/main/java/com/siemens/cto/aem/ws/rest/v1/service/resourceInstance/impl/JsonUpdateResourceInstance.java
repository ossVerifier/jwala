package com.siemens.cto.aem.ws.rest.v1.service.resourceInstance.impl;

import com.siemens.cto.aem.ws.rest.v1.json.AbstractJsonDeserializer;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by z003e5zv on 3/16/2015.
 */
@JsonDeserialize(using = JsonUpdateResourceInstance.UpdateResourceInstanceJSONDeserializer.class)
public class JsonUpdateResourceInstance {

    private String resourceInstanceId;
    private Map<String, String> attributes;

    static final class UpdateResourceInstanceJSONDeserializer extends AbstractJsonDeserializer<JsonUpdateResourceInstance> {
        @Override
        public JsonUpdateResourceInstance deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
            Map<String, String> attributes = new HashMap<>();
            attributes.put("key","value");
            attributes.put("key1","value1");
            attributes.put("key2","value2");
            return  new JsonUpdateResourceInstance("dummyResourceInstanceId", attributes);
        }
    }
    public JsonUpdateResourceInstance(String resourceInstanceId, Map<String, String> attributes) {
        this.resourceInstanceId = resourceInstanceId;
        this.attributes = attributes;
    }
}
