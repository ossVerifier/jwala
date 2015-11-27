package com.siemens.cto.aem.web.controller;

import com.siemens.cto.aem.domain.command.exec.CommandOutput;
import com.siemens.cto.aem.domain.command.exec.ExecReturnCode;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.command.jvm.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.user.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.webserver.WebServerCommandService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CommandController}.
 *
 * Created by Z003BPEJ on 9/2/14.
 */
@RunWith(MockitoJUnitRunner.class)
public class CommandControllerTest {

    private CommandController commandController;

    @Mock
    private JvmControlService jvmControlService;

    @Mock
    private WebServerCommandService webServerCommandService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Before
    public void setUp() {
        commandController = new CommandController(jvmControlService, webServerCommandService);
    }

    @Test
    public void jvmCommandTest() {
        when(request.getParameter(eq("jvmId"))).thenReturn("1");
        when(request.getParameter(eq("operation"))).thenReturn("threadDump");

        final CommandOutput commandOutput = mock(CommandOutput.class);
        when(commandOutput.getStandardOutput()).thenReturn("Standard Output");
        when(commandOutput.getStandardError()).thenReturn("Standard Error");

        final ControlJvmCommand cmd = new ControlJvmCommand(new Identifier<Jvm>("1"), JvmControlOperation.THREAD_DUMP);

        when(jvmControlService.controlJvm(eq(cmd), any(User.class))).thenReturn(commandOutput);

        final ModelAndView mv = commandController.jvmCommand(request, response);

        assertEquals("Standard Output", mv.getModelMap().get("stdOut"));
        assertEquals("Standard Error", mv.getModelMap().get("stdErr"));
    }

    @Test
    public void webServerCommandTest() throws CommandFailureException, IOException {
        when(request.getParameter(eq("webServerId"))).thenReturn("1");

        final CommandOutput execData = mock(CommandOutput.class);
        when(execData.getReturnCode()).thenReturn(mock(ExecReturnCode.class));
        when(execData.getReturnCode().wasSuccessful()).thenReturn(true);
        when(execData.getStandardOutput()).thenReturn("The contents of httpd.conf...");

        final Identifier<WebServer> webServerId = new Identifier<>("1");
        when(webServerCommandService.getHttpdConf(eq(webServerId))).thenReturn(execData);

        final PrintWriter printWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(printWriter);

        commandController.webServerCommand(request, response);

        verify(response).setContentType(eq("text/plain"));
        verify(response).getWriter();
        verify(printWriter).print(eq(execData.getStandardOutput()));
    }

    @Test
    public void webServerCommandTestWithStandardErr() throws CommandFailureException, IOException {
        when(request.getParameter(eq("webServerId"))).thenReturn("1");

        final CommandOutput execData = mock(CommandOutput.class);
        when(execData.getReturnCode()).thenReturn(mock(ExecReturnCode.class));
        when(execData.getReturnCode().wasSuccessful()).thenReturn(false);
        when(execData.getStandardError()).thenReturn("Error!");

        final Identifier<WebServer> webServerId = new Identifier<>("1");
        when(webServerCommandService.getHttpdConf(eq(webServerId))).thenReturn(execData);

        final PrintWriter printWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(printWriter);

        commandController.webServerCommand(request, response);

        verify(response).setContentType(eq("text/plain"));
        verify(response).getWriter();
        verify(printWriter).print(eq("Error reading httpd.conf: " + execData.getStandardError()));
    }

    @Test
    public void webServerCommandTestWithCommandFailureException() throws CommandFailureException, IOException {
        when(request.getParameter(eq("webServerId"))).thenReturn("1");
        final Identifier<WebServer> webServerId = new Identifier<>("1");

        final CommandFailureException cmdFailEx = mock(CommandFailureException.class);
        when(cmdFailEx.getMessage()).thenReturn("Error!");

        when(webServerCommandService.getHttpdConf(eq(webServerId))).thenThrow(cmdFailEx);

        final PrintWriter printWriter = mock(PrintWriter.class);
        when(response.getWriter()).thenReturn(printWriter);

        commandController.webServerCommand(request, response);
        verify(response).setContentType(eq("text/plain"));
        verify(response).getWriter();
        verify(printWriter).print(eq("Error reading httpd.conf: " + cmdFailEx.getMessage()));
    }

}
