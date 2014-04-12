package com.siemens.cto.aem.ws.rest.v1.service.group.impl;

import java.io.IOException;
import java.util.Set;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import com.siemens.cto.aem.ws.rest.v1.service.JsonDeserializationBehavior;

import static com.siemens.cto.aem.ws.rest.v1.service.JsonDeserializationBehavior.array;
import static com.siemens.cto.aem.ws.rest.v1.service.JsonDeserializationBehavior.keyTextValue;
import static com.siemens.cto.aem.ws.rest.v1.service.JsonDeserializationBehavior.keyValue;
import static com.siemens.cto.aem.ws.rest.v1.service.JsonDeserializationBehavior.object;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JsonJvmsDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setup() {

        mapper = new JsonDeserializationBehavior().addMapping(JsonJvms.class, new JsonJvms.JsonJvmsDeserializer()).toObjectMapper();
    }

    @Test
    public void testSingleValueFromMultiple() throws Exception {

        final String json = object(keyValue("jvmIds", array(object(keyTextValue("jvmId", "1")))));

        final JsonJvms jvms = readJvms(json);

        verifyAssertions(jvms,
                         "1");
    }

    @Test
    public void testMultipleValue() throws Exception {

        final String firstJvmId = "1";
        final String secondJvmId = "2";

        final String json = object(keyValue("jvmIds", array(object(keyTextValue("jvmId", firstJvmId)),
                                                            object(keyTextValue("jvmId", secondJvmId)))));

        final JsonJvms jvms = readJvms(json);

        verifyAssertions(jvms,
                         firstJvmId,
                         secondJvmId);
    }

    @Test
    public void testSingleValue() throws Exception {

        final String jvmId = "1";
        final String json = object(keyTextValue("jvmId", jvmId));

        final JsonJvms jvms = readJvms(json);

        verifyAssertions(jvms,
                         jvmId);
    }

    @Test(expected = IOException.class)
    public void testInvalidJson() throws Exception {

        final String json = "alksd';";

        final JsonJvms jvms = readJvms(json);
    }

    protected void verifyAssertions(final JsonJvms someJvms,
                                    final String... expectedIds) {

        final Set<String> jvmIds = someJvms.getJvmIds();

        assertEquals(expectedIds.length,
                     jvmIds.size());

        for (final String id : expectedIds) {
            assertTrue(jvmIds.contains(id));
        }
    }

    protected JsonJvms readJvms(final String someJson) throws IOException {
        return mapper.readValue(someJson, JsonJvms.class);
    }
}
