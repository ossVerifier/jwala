package com.siemens.cto.aem.ws.rest.v1.service.jvm.impl;

import java.io.IOException;

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

public class JsonCreateJvmDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setup() {
        mapper = new JsonDeserializationBehavior().addMapping(JsonCreateJvm.class, new JsonCreateJvm.JsonCreateJvmDeserializer()).toObjectMapper();
    }

    @Test
    public void testDeserializeMultipleGroups() throws Exception {

        final String jvmName = "a jvm name";
        final String hostName = "a host name";
        final String firstGroupId = "1";
        final String secondGroupId = "2";

        final String json = object(keyTextValue("jvmName", jvmName),
                                   keyTextValue("hostName", hostName),
                                   keyValue("groupIds", array(object(keyTextValue("groupId",
                                                                                  firstGroupId)),
                                                              object(keyTextValue("groupId",
                                                                                  secondGroupId)))));

        final JsonCreateJvm create = readValue(json);

        verifyAssertions(create,
                         jvmName,
                         hostName,
                         firstGroupId,
                         secondGroupId);
    }

    @Test
    public void testDeserializeSingleFromMultiple() throws Exception {

        final String jvmName = "a jvm name";
        final String hostName = "a host name";
        final String firstGroupId = "1";

        final String json = object(keyTextValue("jvmName", jvmName),
                                   keyTextValue("hostName", hostName),
                                   keyValue("groupIds", array(object(keyTextValue("groupId", firstGroupId)))));

        final JsonCreateJvm create = readValue(json);

        verifyAssertions(create,
                         jvmName,
                         hostName,
                         firstGroupId);
    }

    @Test
    public void testDeserializeSingle() throws Exception {

        final String jvmName = "a jvm name";
        final String hostName = "a host name";
        final String firstGroupId = "1";

        final String json = object(keyTextValue("jvmName", jvmName),
                                   keyTextValue("hostName", hostName),
                                   keyTextValue("groupId", firstGroupId));

        final JsonCreateJvm create = readValue(json);

        verifyAssertions(create,
                         jvmName,
                         hostName,
                         firstGroupId);
    }

    @Test(expected = IOException.class)
    public void testInvalidInput() throws Exception {

        final String json = "absdfl;jk;lkj;lkjjads";

        final JsonCreateJvm create = readValue(json);
    }

    protected void verifyAssertions(final JsonCreateJvm aCreate,
                                    final String aJvmName,
                                    final String aHostName,
                                    final String... groupIds) {

        assertEquals(aJvmName,
                     aCreate.getJvmName());
        assertEquals(aHostName,
                     aCreate.getHostName());
        assertEquals(groupIds.length,
                     aCreate.getGroupIds().size());
        for (final String groupId : groupIds) {
            assertTrue(aCreate.getGroupIds().contains(groupId));
        }
    }

    protected JsonCreateJvm readValue(final String someJson) throws IOException {
        return mapper.readValue(someJson, JsonCreateJvm.class);
    }
}
