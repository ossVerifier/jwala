package com.siemens.cto.aem.ws.rest.v1.service.webserver.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;

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

/**
 * New toMany Group JSON syntax:
 * [{ \"webserverName\": \"webserver\", [{\"groupId\": 1},\"groupId\": 2}], \"hostName\":\"localhost\", \"portNumber\":8080}]");
 * 
 * @author horspe00
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class WebServerServiceRestImplTest {

        @Mock
        WebServerServiceImpl impl;
        
        private WebServerServiceRestImpl cut;
        private JsonFactory factory = new JsonFactory();
        
        JsonCreateWebServer templateCWS = new JsonCreateWebServer("webserver", "localhost","8080");
        JsonUpdateWebServer templateUWS = new JsonUpdateWebServer("1","webserver-modified","localhost","8080");
        JsonCreateWebServer templateCWSmulti = new JsonCreateWebServer("webserver", "localhost","8080");
        JsonUpdateWebServer templateUWSmulti = new JsonUpdateWebServer("1","webserver-modified","localhost","8080");

        Group templateGroup;
        Group templateGroup2;
        Collection<Group> templateGroups;
        Collection<Group> templateGroupsMulti;
        WebServer templateWS;
        WebServer templateWSmodified;
        WebServer templateWSmulti;
        WebServer templateWSmodifiedmulti;

        String cwsJsonSingleGroup = "[{ \"webserverName\": \"webserver\", \"groupId\": 1, \"hostName\":\"localhost\", \"portNumber\":8080}]";
        String cwsJsonMultiGroup = "[{ \"webserverName\": \"webserver\", \"groupIds\": [{\"groupId\": 1},{\"groupId\": \"2\"}], \"hostName\":\"localhost\", \"portNumber\":8080}]";
        String uwsJsonSingleGroup = "[{ \"webserverId\" : \"1\", \"groupId\":\"1\", \"webserverName\":\"webserver-modified\", \"hostName\":\"localhost\",\"portNumber\":\"8080\"}]";
        String uwsJsonMultiGroup = "[{ \"webserverId\" : \"1\", \"groupIds\": [{\"groupId\": \"1\"},{\"groupId\": \"2\"}], \"webserverName\":\"webserver-modified\", \"hostName\":\"localhost\",\"portNumber\":\"8080\"}]";

        @Before
        public void createWebServerService() {
            cut = new WebServerServiceRestImpl(impl);
            templateCWS.addGroupId("1");
            templateUWS.addGroupId("1");
            templateCWSmulti.addGroupId("1");
            templateUWSmulti.addGroupId("1");
            templateCWSmulti.addGroupId("2");
            templateUWSmulti.addGroupId("2");

            templateGroups = new ArrayList<>();
            templateGroupsMulti = new ArrayList<>();
            templateGroup = new Group(Identifier.id(1L, Group.class), "ws-group");
            templateGroup2 = new Group(Identifier.id(2L, Group.class), "ws-group2");
            templateGroups.add(templateGroup);
            templateGroupsMulti.add(templateGroup);
            templateGroupsMulti.add(templateGroup2);
            templateWS = new WebServer(Identifier.id(1L, WebServer.class), templateGroups, "webserver", "localhost",8080);
            templateWSmodified = new WebServer(Identifier.id(1L, WebServer.class), templateGroups, "webserver-modified", "localhost",8080);
            templateWSmulti = new WebServer(Identifier.id(1L, WebServer.class), templateGroupsMulti, "webserver", "localhost",8080);
            templateWSmodifiedmulti = new WebServer(Identifier.id(1L, WebServer.class), templateGroupsMulti, "webserver-modified", "localhost",8080);

        }
        
        @Test
        public void testCreateWebServer() throws JsonParseException, IOException {

            when(impl.createWebServer(any(CreateWebServerCommand.class), any(User.class))).thenReturn(templateWS);
            
            JsonParser parser = factory.createJsonParser(cwsJsonSingleGroup);
            
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

        @Test
        public void testCreateWebServerMG() throws JsonParseException, IOException {

            when(impl.createWebServer(any(CreateWebServerCommand.class), any(User.class))).thenReturn(templateWSmulti);
            
            JsonParser parser = factory.createJsonParser(cwsJsonMultiGroup);
            
            ObjectMapper mapper = new ObjectMapper();

            parser.setCodec(mapper);
            
            JsonCreateWebServer cmd = mapper.readValue(parser, JsonCreateWebServer.class);
            
            assertEquals(cmd, templateCWSmulti);
            
            Response response = cut.createWebServer(cmd);
            
            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

            final ApplicationResponse applicationResponse =
                    (ApplicationResponse) response.getEntity();

            Object content = applicationResponse.getApplicationResponseContent();
            
            Writer writer = new StringWriter();
            mapper.writeValue(writer, applicationResponse);

            final String jsonStr = writer.toString();
            assertTrue(jsonStr.contains("\"id\":1"));
            assertTrue(jsonStr.contains("\"groups\":[{\"name\":\"ws-group2\",\"id\":{\"id\":2}},{\"name\":\"ws-group\",\"id\":{\"id\":1}}],\"groupIds\":[{\"id\":2},{\"id\":1}]}"));
            assertTrue(jsonStr.contains("\"name\":\"webserver\""));
            assertTrue(jsonStr.contains("\"host\":\"localhost\""));

            assertEquals(content, templateWSmulti);
        }
        
        @Test
        public void testUpdateWebServerMG() throws JsonParseException, IOException {

            when(impl.updateWebServer(any(UpdateWebServerCommand.class), any(User.class))).thenReturn(templateWSmodifiedmulti);
            
            JsonParser parser = factory.createJsonParser(uwsJsonMultiGroup);
            
            ObjectMapper mapper = new ObjectMapper();

            parser.setCodec(mapper);
            
            JsonUpdateWebServer cmd = mapper.readValue(parser, JsonUpdateWebServer.class);
            
            assertEquals(templateUWSmulti, cmd);
            
            Response response = cut.updateWebServer(cmd);
            
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            final ApplicationResponse applicationResponse =
                    (ApplicationResponse) response.getEntity();

            Object content = applicationResponse.getApplicationResponseContent();
            
            Writer writer = new StringWriter();
            mapper.writeValue(writer, applicationResponse);

            final String jsonStr = writer.toString();
            assertTrue(jsonStr.contains("\"id\":1"));
            assertTrue(jsonStr.contains("\"groups\":[{\"name\":\"ws-group2\",\"id\":{\"id\":2}},{\"name\":\"ws-group\",\"id\":{\"id\":1}}],\"groupIds\":[{\"id\":2},{\"id\":1}]}"));
            assertTrue(jsonStr.contains("\"name\":\"webserver-modified\""));
            assertTrue(jsonStr.contains("\"host\":\"localhost\""));
        
            assertEquals(content, templateWSmodifiedmulti);            
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
