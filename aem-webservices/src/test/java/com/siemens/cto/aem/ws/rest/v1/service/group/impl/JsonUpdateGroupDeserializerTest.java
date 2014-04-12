package com.siemens.cto.aem.ws.rest.v1.service.group.impl;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import com.siemens.cto.aem.ws.rest.v1.service.JsonDeserializationBehavior;

import static com.siemens.cto.aem.ws.rest.v1.service.JsonDeserializationBehavior.keyTextValue;
import static com.siemens.cto.aem.ws.rest.v1.service.JsonDeserializationBehavior.object;
import static org.junit.Assert.assertEquals;

public class JsonUpdateGroupDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setup() {
        mapper = new JsonDeserializationBehavior().addMapping(JsonUpdateGroup.class, new JsonUpdateGroup.JsonUpdateGroupDeserializer()).toObjectMapper();
    }

    @Test
    public void testDeserialize() throws Exception {

        final String groupId = "1";
        final String groupName = "a group name";
        final String json = object(keyTextValue("id", groupId),
                                   keyTextValue("name", groupName));

        final JsonUpdateGroup update = readUpdate(json);

        assertEquals(groupId,
                     update.getId());
        assertEquals(groupName,
                     update.getName());
    }

    @Test(expected = IOException.class)
    public void testDeserializeInvalidJson() throws Exception {

        final String json = "alksdjfl;askdjga;lskda;sdlf4kjas;df4kljasd;f";

        final JsonUpdateGroup update = readUpdate(json);
    }

    protected JsonUpdateGroup readUpdate(final String someJson) throws IOException {
        return mapper.readValue(someJson, JsonUpdateGroup.class);
    }

}
