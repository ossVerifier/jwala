package com.siemens.cto.aem.service.spring.component.impl;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.common.request.group.SetGroupStateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaCurrentState;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.service.JvmCrudService;
import com.siemens.cto.aem.persistence.jpa.service.StateCrudService;
import com.siemens.cto.aem.persistence.jpa.service.WebServerCrudService;
import com.siemens.cto.aem.service.spring.component.GrpStateComputationAndNotificationSvc;
import com.siemens.cto.aem.service.state.GroupStateService;
import com.siemens.cto.aem.service.state.StateNotificationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link GrpStateComputationAndNotificationSvcImpl}.
 *
 * Created by JC043760 on 1/19/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = {GrpStateComputationAndNotificationSvcImplTest.Config.class})
public class GrpStateComputationAndNotificationSvcImplTest {

    @Autowired
    @Qualifier("groupStateComputationAndNotificationService")
    private GrpStateComputationAndNotificationSvc svc;

    @Before
    public void setup() {
        reset(Config.stateNotificationServiceMock);
        reset(Config.groupStateApi);
    }

    @Test
    public void testComputeAndNotifyUsingJvmState() throws Exception {
        svc.computeAndNotify(new Identifier<Jvm>(1L), JvmState.JVM_STOPPED);
        verify(Config.stateNotificationServiceMock).notifyStateUpdated(any(CurrentState.class));
        verify(Config.groupStateApi).groupStatePersist(any(SetGroupStateRequest.class));
    }

    @Test
    public void testComputeAndNotifyUsingWebServerState() throws Exception {
        svc.computeAndNotify(new Identifier<WebServer>(1L), WebServerReachableState.WS_UNREACHABLE);
        verify(Config.stateNotificationServiceMock).notifyStateUpdated(any(CurrentState.class));
        verify(Config.groupStateApi).groupStatePersist(any(SetGroupStateRequest.class));
    }

    @ComponentScan("com.siemens.cto.aem.service.spring.component.impl")
    static class Config {

        @Mock
        static StateNotificationService stateNotificationServiceMock;

        @Mock
        static GroupStateService.API groupStateApi;

        public Config() {
            MockitoAnnotations.initMocks(this);
        }

        @Bean
        JvmCrudService getJvmCrudService() {
            final JvmCrudService jvmCrudServiceMock = mock(JvmCrudService.class);

            final JpaGroup group = new JpaGroup();
            group.setId(1L);
            final JpaGroup [] jpaGroupArray = {group};
            final JpaJvm jpaJvm = new JpaJvm();
            jpaJvm.setId(1L);
            final JpaJvm otherJpaJvm = new JpaJvm();
            otherJpaJvm.setId(2L);
            final JpaJvm [] jpaJvmArray = {jpaJvm, otherJpaJvm};
            jpaJvm.setGroups(Arrays.asList(jpaGroupArray));

            when(jvmCrudServiceMock.getJvm(eq(new Identifier<Jvm>(1L)))).thenReturn(jpaJvm);
            when(jvmCrudServiceMock.findJvmsBelongingTo(eq(new Identifier<Group>(1L)))).thenReturn(Arrays.asList(jpaJvmArray));

            // For testComputeAndNotifyUsingWebServerState
            final JpaJvm jpaJvm1 = new JpaJvm();
            jpaJvm1.setId(1L);
            final JpaJvm [] anotherJpaJvmArray = {jpaJvm1};
            when(jvmCrudServiceMock.findJvmsBelongingTo(new Identifier<Group>(1L)))
                    .thenReturn(Arrays.asList(anotherJpaJvmArray));

            return jvmCrudServiceMock;
        }

        @Bean
        WebServerCrudService getWebServerCrudService() {
            final WebServerCrudService webServerCrudServiceMock = mock(WebServerCrudService.class);
            final List<Group> groupList = new ArrayList<>();
            groupList.add(new Group(new Identifier<Group>(1L), "zGroup"));
            final WebServer webServer = new WebServer(new Identifier<WebServer>(1L), groupList, "zWebServer");
            final WebServer [] webServerArray = {webServer};
            when(webServerCrudServiceMock.findWebServersBelongingTo(new Identifier<Group>(1L))).
                    thenReturn(Arrays.asList(webServerArray));

            // For testComputeAndNotifyUsingWebServerState
            final WebServer webServer1 = new WebServer(new Identifier<WebServer>(1L), groupList, "webServer1");
            final WebServer webServer2 = new WebServer(new Identifier<WebServer>(2L), groupList, "webServer2");
            final WebServer [] anotherWebServerArray = {webServer1, webServer2};

            when(webServerCrudServiceMock.getWebServer(eq(new Identifier<WebServer>(1L)))).thenReturn(webServer1);
            when(webServerCrudServiceMock.findWebServersBelongingTo(eq(new Identifier<Group>(1L)))).
                    thenReturn(Arrays.asList(anotherWebServerArray));

            return webServerCrudServiceMock;
        }

        @Bean(name = "jvmStateCrudService")
        @SuppressWarnings("unchecked")
        StateCrudService<Jvm, JvmState> getJvmStateCrudService() {
            final StateCrudService<Jvm, JvmState> stateCrudServiceMock = mock(StateCrudService.class);
            final JpaCurrentState jpaCurrentState = new JpaCurrentState();
            jpaCurrentState.setState(JvmState.JVM_STOP.name());
            when(stateCrudServiceMock.getState(any(Identifier.class))).thenReturn(jpaCurrentState);
            return stateCrudServiceMock;
        }

        @Bean(name = "webServerStateCrudService")
        @SuppressWarnings("unchecked")
        StateCrudService<WebServer, WebServerReachableState> getWebServerStateCrudService() {
            final StateCrudService<WebServer, WebServerReachableState> stateCrudServiceMock = mock(StateCrudService.class);
            final JpaCurrentState jpaCurrentState = new JpaCurrentState();
            jpaCurrentState.setState(WebServerReachableState.WS_UNREACHABLE.name());
            when(stateCrudServiceMock.getState(any(Identifier.class))).thenReturn(jpaCurrentState);
            return stateCrudServiceMock;
        }

        @Bean
        StateNotificationService getStateNotificationService() {
            return stateNotificationServiceMock;
        }

        @Bean
        GroupStateService.API getGroupStateServiceApi() {
            return groupStateApi;
        }

    }
}
