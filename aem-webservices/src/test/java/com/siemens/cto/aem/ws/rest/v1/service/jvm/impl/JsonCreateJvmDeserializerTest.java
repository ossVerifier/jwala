package com.siemens.cto.aem.ws.rest.v1.service.jvm.impl;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.id.IdentifierSetBuilder;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmAndAddToGroupsCommand;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmCommand;
import com.siemens.cto.aem.ws.rest.v1.service.JsonDeserializationBehavior;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static com.siemens.cto.aem.ws.rest.v1.service.JsonDeserializationBehavior.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JsonCreateJvmDeserializerTest {

    private ObjectMapper mapper;

    private static final String httpPort = "5";
    private static final String httpsPort = "4";
    private static final String redirectPort = "3";
    private static final String shutdownPort = "2";
    private static final String ajpPort = "1";
    private static final String statusPath = "/statusPath";
    private static final String systemProperties = "EXAMPLE_OPTS=%someEnv%/someVal";

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
                                                                                  secondGroupId)))),
                                   keyTextValue("httpPort", httpPort),
                                   keyTextValue("httpsPort", httpsPort),
                                   keyTextValue("redirectPort", redirectPort),
                                   keyTextValue("shutdownPort", shutdownPort),
                                   keyTextValue("ajpPort", ajpPort),
                                   keyTextValue("statusPath", statusPath),
                                   keyTextValue("systemProperties", systemProperties));

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
                                   keyValue("groupIds", array(object(keyTextValue("groupId", firstGroupId)))),
                                   keyTextValue("httpPort", httpPort),
                                   keyTextValue("httpsPort", httpsPort),
                                   keyTextValue("redirectPort", redirectPort),
                                   keyTextValue("shutdownPort", shutdownPort),
                                   keyTextValue("ajpPort", ajpPort),
                                   keyTextValue("statusPath", statusPath),
                                   keyTextValue("systemProperties", systemProperties));

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
                                   keyTextValue("groupId", firstGroupId),
                                   keyTextValue("httpPort", httpPort),
                                   keyTextValue("httpsPort", httpsPort),
                                   keyTextValue("redirectPort", redirectPort),
                                   keyTextValue("shutdownPort", shutdownPort),
                                   keyTextValue("ajpPort", ajpPort),
                                   keyTextValue("statusPath", statusPath),
                                   keyTextValue("systemProperties", systemProperties));

        final JsonCreateJvm create = readValue(json);

        verifyAssertions(create,
                         jvmName,
                         hostName,
                         firstGroupId);
    }

    @Test
    public void testDeserializeNoGroups() throws Exception {

        final String jvmName = "a jvm name";
        final String hostName = "a host name";

        final String json = object(keyTextValue("jvmName", jvmName),
                                   keyTextValue("hostName", hostName),
                                   keyTextValue("httpPort", httpPort),
                                   keyTextValue("httpsPort", httpsPort),
                                   keyTextValue("redirectPort", redirectPort),
                                   keyTextValue("shutdownPort", shutdownPort),
                                   keyTextValue("ajpPort", ajpPort),
                                   keyTextValue("statusPath", statusPath),
                                   keyTextValue("systemProperties", systemProperties));

        final JsonCreateJvm create = readValue(json);

        verifyAssertions(create,
                         jvmName,
                         hostName);
    }

    @Test(expected = IOException.class)
    public void testInvalidInput() throws Exception {

        final String json = "absdfl;jk;lkj;lkjjads";

        final JsonCreateJvm create = readValue(json);
    }

    @Test(expected = BadRequestException.class)
    public void testInvalidGroupId() throws Exception {

        final String jvmName = "a jvm name";
        final String hostName = "a host name";
        final String firstGroupId = "this is not a valid group id";

        final String json = object(keyTextValue("jvmName", jvmName),
                                   keyTextValue("hostName", hostName),
                                   keyTextValue("groupId", firstGroupId),
                                   keyTextValue("httpPort", httpPort),
                                   keyTextValue("httpsPort", httpsPort),
                                   keyTextValue("redirectPort", redirectPort),
                                   keyTextValue("shutdownPort", shutdownPort),
                                   keyTextValue("ajpPort", ajpPort),
                                   keyTextValue("statusPath", statusPath),
                                   keyTextValue("systemProperties", systemProperties));

        final JsonCreateJvm create = readValue(json);
        verifyAssertions(create,
                         jvmName,
                         hostName,
                         firstGroupId);
    }

    protected void verifyAssertions(final JsonCreateJvm aCreate,
                                    final String aJvmName,
                                    final String aHostName,
                                    final String... groupIds) {

        final CreateJvmAndAddToGroupsCommand createAndAddCommand = aCreate.toCreateAndAddCommand();
        final CreateJvmCommand createCommand = createAndAddCommand.getCreateCommand();

        assertEquals(aCreate.toCreateJvmCommand(),
                     createCommand);
        assertEquals(aJvmName,
                     createCommand.getJvmName());
        assertEquals(aHostName,
                     createCommand.getHostName());
        assertEquals(groupIds.length,
                     createAndAddCommand.getGroups().size());
        assertTrue(new IdentifierSetBuilder(Arrays.asList(groupIds)).build().containsAll(createAndAddCommand.getGroups()));
        assertEquals(groupIds.length > 0,
                     aCreate.areGroupsPresent());

    }

    protected JsonCreateJvm readValue(final String someJson) throws IOException {
        return mapper.readValue(someJson, JsonCreateJvm.class);
    }
}