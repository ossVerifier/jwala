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

public class JsonUpdateJvmDeserializer {

    private ObjectMapper mapper;

    @Before
    public void seup() {
        mapper = new JsonDeserializationBehavior().addMapping(JsonUpdateJvm.class, new JsonUpdateJvm.JsonUpdateJvmDeserializer()).toObjectMapper();
    }

    @Test
    public void testDeserializeUpdateMultipleGroups() throws Exception {

        final String jvmId = "1";
        final String jvmName = "a jvm name";
        final String hostName = "a host name";
        final String firstGroupId = "1";
        final String secondGroupId = "2";

        final String json = object(keyTextValue("jvmId", jvmId),
                                   keyTextValue("jvmName", jvmName),
                                   keyTextValue("hostName", hostName),
                                   keyValue("groupIds", array(object(keyTextValue("groupId", firstGroupId)),
                                                              object(keyTextValue("groupId", secondGroupId)))));

        final JsonUpdateJvm update = readValue(json);

        verifyAssertions(update,
                         jvmId,
                         jvmName,
                         hostName,
                         firstGroupId,
                         secondGroupId);
    }

    @Test
    public void testDeserializeUpdateSingleGroupFromMultiple() throws Exception {

        final String jvmId = "1";
        final String jvmName = "a jvm name";
        final String hostName = "a host name";
        final String firstGroupId = "1";

        final String json = object(keyTextValue("jvmId", jvmId),
                                   keyTextValue("jvmName", jvmName),
                                   keyTextValue("hostName", hostName),
                                   keyValue("groupIds", array(object(keyTextValue("groupId", firstGroupId)))));

        final JsonUpdateJvm update = readValue(json);

        verifyAssertions(update,
                         jvmId,
                         jvmName,
                         hostName,
                         firstGroupId);
    }

    @Test
    public void testDeserializeSingleGroup() throws Exception {

        final String jvmId = "1";
        final String jvmName = "a jvm name";
        final String hostName = "a host name";
        final String firstGroupId = "1";

        final String json = object(keyTextValue("jvmId", jvmId),
                                   keyTextValue("jvmName", jvmName),
                                   keyTextValue("hostName", hostName),
                                   keyTextValue("groupId", firstGroupId));

        final JsonUpdateJvm update = readValue(json);

        verifyAssertions(update,
                         jvmId,
                         jvmName,
                         hostName,
                         firstGroupId);
    }

    protected JsonUpdateJvm readValue(final String someJson) throws IOException {
        return mapper.readValue(someJson, JsonUpdateJvm.class);
    }

    protected void verifyAssertions(final JsonUpdateJvm anUpdate,
                                    final String aJvmId,
                                    final String aJvmName,
                                    final String aHostName,
                                    final String... someGroupIds) {

        assertEquals(aJvmId,
                     anUpdate.getJvmId());
        assertEquals(aJvmName,
                     anUpdate.getJvmName());
        assertEquals(aHostName,
                     anUpdate.getHostName());
        assertEquals(someGroupIds.length,
                     anUpdate.getGroupIds().size());
        for (final String groupId : someGroupIds) {
            assertTrue(anUpdate.getGroupIds().contains(groupId));
        }
    }
}
