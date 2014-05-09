package com.siemens.cto.aem.ws.rest.v1.service.webserver.impl;

import static com.siemens.cto.aem.ws.rest.v1.service.JsonDeserializationBehavior.array;
import static com.siemens.cto.aem.ws.rest.v1.service.JsonDeserializationBehavior.keyTextValue;
import static com.siemens.cto.aem.ws.rest.v1.service.JsonDeserializationBehavior.keyValue;
import static com.siemens.cto.aem.ws.rest.v1.service.JsonDeserializationBehavior.object;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.domain.model.id.IdentifierSetBuilder;
import com.siemens.cto.aem.domain.model.webserver.CreateWebServerCommand;
import com.siemens.cto.aem.ws.rest.v1.service.JsonDeserializationBehavior;

public class JsonCreateWebServerDeserializerTest {

    private static final String webserverName = "webserverName";
    private static final String hostName = "localhost";
    private static final String portNumber = "10000";
    private static final String groupIdOne = "1";
    private static final String groupIdTwo = "2";

    private ObjectMapper mapper;

    @Before
    public void setup() {
        mapper = new JsonDeserializationBehavior().addMapping(JsonCreateWebServer.class,
                                                              new JsonCreateWebServer.JsonCreateWebServerDeserializer())
                                                  .toObjectMapper();
    }

    @Test
    public void testDeserializeMultipleGroups() throws Exception {
        String json = array(object(keyTextValue("webserverName", webserverName),
                                   keyTextValue("hostName", hostName),
                                   keyTextValue("portNumber", portNumber),
                                   keyValue("groupIds",
                                            array(object(keyTextValue("groupId", groupIdOne)),
                                                  object(keyTextValue("groupId", groupIdTwo))))));
        final JsonCreateWebServer create = readValue(json);
        verifyAssertions(create, webserverName, hostName, groupIdOne, groupIdTwo);
    }

    @Test
    public void testDeserializeSingleGroup() throws Exception {
        String json = array(object(keyTextValue("webserverName", webserverName),
                                   keyTextValue("hostName", hostName),
                                   keyTextValue("portNumber", portNumber),
                                   keyValue("groupIds", array(object(keyTextValue("groupId", groupIdOne))))));
        final JsonCreateWebServer create = readValue(json);
        verifyAssertions(create, webserverName, hostName, groupIdOne);
    }

    @Test
    public void testDeserializeNoGroup() throws Exception {
        String json = array(object(keyTextValue("webserverName", webserverName),
                                   keyTextValue("hostName", hostName),
                                   keyTextValue("portNumber", portNumber)));
        final JsonCreateWebServer create = readValue(json);
        verifyAssertions(create, webserverName, hostName);
    }

    @Test(expected = BadRequestException.class)
    public void testInvalidPortNumber() throws Exception {
        String json = array(object(keyTextValue("webserverName", webserverName),
                                   keyTextValue("hostName", hostName),
                                   keyTextValue("portNumber", "abcd")));
        final JsonCreateWebServer create = readValue(json);
        create.toCreateWebServerCommand();
    }

    @Test(expected = IOException.class)
    public void testInvalidInput() throws Exception {

        final String json = "absdfl;jk;lkj;lkjjads";

        readValue(json);
    }

    protected void verifyAssertions(final JsonCreateWebServer aCreate, final String aWebServerName,
            final String aHostName, final String... groupIds) {
        CreateWebServerCommand createCommand = aCreate.toCreateWebServerCommand();

        assertEquals(aWebServerName, createCommand.getName());
        assertEquals(aHostName, createCommand.getHost());
        assertEquals(groupIds.length, createCommand.getGroups().size());
        assertTrue(new IdentifierSetBuilder(Arrays.asList(groupIds)).build().containsAll(createCommand.getGroups()));
        assertEquals(groupIds.length, createCommand.getGroups().size());

    }

    protected JsonCreateWebServer readValue(final String someJson) throws IOException {
        return mapper.readValue(someJson, JsonCreateWebServer.class);
    }
}
