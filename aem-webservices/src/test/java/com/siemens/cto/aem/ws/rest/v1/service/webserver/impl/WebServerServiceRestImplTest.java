package com.siemens.cto.aem.ws.rest.v1.service.webserver.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.CreateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.UpdateWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.service.webserver.impl.WebServerServiceImpl;
import com.siemens.cto.aem.ws.rest.v1.response.ApplicationResponse;

@RunWith(MockitoJUnitRunner.class)
public class WebServerServiceRestImplTest {

        @Mock
        WebServerServiceImpl impl;
        
        private WebServerServiceRestImpl cut;
        private JsonFactory factory = new JsonFactory();
        
        JsonCreateWebServer templateCWS = new JsonCreateWebServer("1", "webserver", "localhost","8080");
        Group templateGroup = new Group(Identifier.id(1L, Group.class), "ws-group");
        WebServer templateWS = new WebServer(Identifier.id(1L, WebServer.class), templateGroup, "webserver", "localhost",8080);
        WebServer templateWSmodified = new WebServer(Identifier.id(1L, WebServer.class), templateGroup, "webserver-modified", "localhost",8080);

        
        JsonUpdateWebServer templateUWS = new JsonUpdateWebServer("1","1","webserver-modified","localhost","8080");

        @Before
        public void createWebServerService() {
            cut = new WebServerServiceRestImpl(impl);
            
        }
        @Test
        public void testCreateWebServer() throws JsonParseException, IOException {

            when(impl.createWebServer(any(CreateWebServerCommand.class), any(User.class))).thenReturn(templateWS);
            
            JsonParser parser = factory.createJsonParser("[{ \"webserverName\": \"webserver\", \"groupId\": 1, \"hostName\":\"localhost\", \"portNumber\":8080}]");
            
            ObjectMapper mapper = new ObjectMapper();

            parser.setCodec(mapper);
            
            JsonCreateWebServer cmd = mapper.readValue(parser, JsonCreateWebServer.class);
            
            assertEquals(cmd, templateCWS);
            
            Response response = cut.createWebServer(cmd);
            
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

            final ApplicationResponse applicationResponse =
                    (ApplicationResponse) response.getEntity();

            Object content = applicationResponse.getApplicationResponseContent();
            
            Writer writer = new StringWriter();
            mapper.writeValue(writer, applicationResponse);

            final String jsonStr = writer.toString();
            assertTrue(jsonStr.contains("\"id\":1"));
            assertTrue(jsonStr.contains("\"name\":\"webserver\""));
            assertTrue(jsonStr.contains("\"host\":\"localhost\""));

            assertEquals(content, templateWS);
        }
        
        @Test
        public void testUpdateWebServer() throws JsonParseException, IOException {

            when(impl.updateWebServer(any(UpdateWebServerCommand.class), any(User.class))).thenReturn(templateWSmodified);
            
            JsonParser parser = factory.createJsonParser("[{ \"webserverId\" : \"1\", \"groupId\":\"1\", \"webserverName\":\"webserver-modified\", \"hostName\":\"localhost\",\"portNumber\":\"8080\"}]");
            
            ObjectMapper mapper = new ObjectMapper();

            parser.setCodec(mapper);
            
            JsonUpdateWebServer cmd = mapper.readValue(parser, JsonUpdateWebServer.class);
            
            assertEquals(templateUWS, cmd);
            
            Response response = cut.updateWebServer(cmd);
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            final ApplicationResponse applicationResponse =
                    (ApplicationResponse) response.getEntity();

            Object content = applicationResponse.getApplicationResponseContent();
            
            Writer writer = new StringWriter();
            mapper.writeValue(writer, applicationResponse);

            final String jsonStr = writer.toString();
            assertTrue(jsonStr.contains("\"id\":1"));
            assertTrue(jsonStr.contains("\"name\":\"webserver-modified\""));
            assertTrue(jsonStr.contains("\"host\":\"localhost\""));
        
            assertEquals(content, templateWSmodified);            
        }
/*
        WebServer testGetWebServer(final Identifier<WebServer> aWebServerId);
        
        List<WebServer> testGetWebServers(final PaginationParameter aPaginationParam);
        
        List<WebServer> testFindWebServers(final String aWebServerNameFragment,
                         final PaginationParameter aPaginationParam);
        
        List<WebServer> testFindWebServers(final Identifier<Group> aWebServerId,
                         final PaginationParameter aPaginationParam);
        
        WebServer testUpdateWebServer(final UpdateWebServerCommand anUpdateWebServerCommand,
                    final User anUpdatingUser);
        
        void testRemoveWebServer(final Identifier<WebServer> aWebServerId);
        
        void testRemoveWebServersBelongingTo(final Identifier<Group> aGroupId);
*/
}
