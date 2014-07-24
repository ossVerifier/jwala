package com.siemens.cto.aem.ws.rest.v1.service.jvm.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.exec.ExecReturnCode;
import com.siemens.cto.aem.domain.model.group.LiteGroup;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlHistory;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmAndAddToGroupsCommand;
import com.siemens.cto.aem.domain.model.jvm.command.CreateJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.UpdateJvmCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.jvm.impl.JvmServiceImpl;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.ws.rest.v1.provider.PaginationParamProvider;
import com.siemens.cto.aem.ws.rest.v1.response.ApplicationResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author meleje00
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class JvmServiceRestImplTest {

    private static final String name = "jvmName";
    private final static String hostName = "localhost";
    private static final List<Jvm> jvmList = createJvmList();
    private static final Jvm jvm = jvmList.get(0);

    // JVM ports
    private static final String httpPort = "80";
    private static final String httpsPort = "81";
    private static final String redirectPort = "82";
    private static final String shutdownPort = "83";
    private static final String ajpPort = "84";

    @Mock
    private JvmServiceImpl impl;
    @Mock
    private JvmControlService controlImpl;
    @Mock
    private JvmControlHistory jvmControlHistory;
    @Mock
    private StateService<Jvm, JvmState> jvmStateService;

    private JvmServiceRestImpl cut;

    private static List<Jvm> createJvmList() {
        final Set<LiteGroup> groups = new HashSet<>();
        final Jvm ws = new Jvm(Identifier.id(1L, Jvm.class),
                               name,
                               hostName,
                               groups,
                               Integer.valueOf(httpPort),
                               Integer.valueOf(httpsPort),
                               Integer.valueOf(redirectPort),
                               Integer.valueOf(shutdownPort),
                               Integer.valueOf(ajpPort));
        final List<Jvm> result = new ArrayList<>();
        result.add(ws);
        return result;
    }

    @Before
    public void setUp() {
        cut = new JvmServiceRestImpl(impl, controlImpl, jvmStateService);
    }

    @Test
    public void testGetJvmList() {
        when(impl.getJvms(any(PaginationParameter.class))).thenReturn(jvmList);

        final PaginationParamProvider paginationProvider = new PaginationParamProvider();
        final Response response = cut.getJvms(paginationProvider);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof List);

        final List<Jvm> receivedList = (List<Jvm>) content;
        final Jvm received = receivedList.get(0);
        assertEquals(jvm, received);
    }

    @Test
    public void testGetJvm() {
        when(impl.getJvm(any(Identifier.class))).thenReturn(jvm);

        final Response response = cut.getJvm(Identifier.id(Long.valueOf(1l), Jvm.class));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Jvm);

        final Jvm received = (Jvm) content;
        assertEquals(jvm, received);
    }

    @Test
    public void testCreateJvm() {
        when(impl.createJvm(any(CreateJvmCommand.class), any(User.class))).thenReturn(jvm);

        final JsonCreateJvm jsonCreateJvm = new JsonCreateJvm(name, hostName, httpPort, httpsPort, redirectPort, shutdownPort, ajpPort);
        final Response response = cut.createJvm(jsonCreateJvm);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Jvm);

        final Jvm received = (Jvm) content;
        assertEquals(jvm, received);
    }

    @Test
    public void testCreateJvmWithGroups() {
        when(impl.createAndAssignJvm(any(CreateJvmAndAddToGroupsCommand.class), any(User.class))).thenReturn(jvm);

        final Set<String> groupIds = new HashSet<String>();
        groupIds.add("1");
        final JsonCreateJvm jsonCreateJvm = new JsonCreateJvm(name, hostName, groupIds, httpPort, httpsPort,
                                                              redirectPort, shutdownPort, ajpPort);
        final Response response = cut.createJvm(jsonCreateJvm);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Jvm);

        final Jvm received = (Jvm) content;
        assertEquals(jvm, received);
    }

    @Test
    public void testUpdateJvm() {
        final Set<String> groupIds = new HashSet<String>();
        final JsonUpdateJvm jsonUpdateJvm = new JsonUpdateJvm("1", name, hostName, groupIds, "5", "4", "3", "2", "1");
        when(impl.updateJvm(any(UpdateJvmCommand.class), any(User.class))).thenReturn(jvm);

        final Response response = cut.updateJvm(jsonUpdateJvm);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();

        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof Jvm);

        final Jvm received = (Jvm) content;
        assertEquals(jvm, received);
    }

    @Test
    public void testRemoveJvm() {
        final Response response = cut.removeJvm(Identifier.id(Long.valueOf(1l), Jvm.class));
        verify(impl, atLeastOnce()).removeJvm(any(Identifier.class));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        assertNull(applicationResponse);
    }

    @Test
    public void testControlJvm() {
        when(jvmControlHistory.getExecData()).thenReturn(new ExecData(new ExecReturnCode(0), "", ""));
        when(controlImpl.controlJvm(any(ControlJvmCommand.class), any(User.class))).thenReturn(jvmControlHistory);

        final ExecData execData = mock(ExecData.class);
        final ExecReturnCode execDataReturnCode = mock(ExecReturnCode.class);
        when(execDataReturnCode.wasSuccessful()).thenReturn(true);
        when(execData.getReturnCode()).thenReturn(execDataReturnCode);
        when(jvmControlHistory.getExecData()).thenReturn(execData);

        final JsonControlJvm jsonControlJvm = new JsonControlJvm("start");
        final Response response = cut.controlJvm(Identifier.id(Long.valueOf(1l), Jvm.class), jsonControlJvm);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        final ApplicationResponse applicationResponse = (ApplicationResponse) response.getEntity();
        final Object content = applicationResponse.getApplicationResponseContent();
        assertTrue(content instanceof JvmControlHistory);

        final JvmControlHistory received = (JvmControlHistory) content;
        assertEquals(jvmControlHistory, received);
    }
}