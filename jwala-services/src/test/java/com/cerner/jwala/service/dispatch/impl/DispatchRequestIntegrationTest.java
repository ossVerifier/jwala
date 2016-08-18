package com.cerner.jwala.service.dispatch.impl;

/*@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { DispatchRequestIntegrationTest.CommonConfiguration.class })*/
public class DispatchRequestIntegrationTest {

/*    private static final Identifier<Jvm> JVM1_IDENTIFIER = new Identifier<>((long) 1);
    private static final Identifier<Jvm> JVM2_IDENTIFIER = new Identifier<>((long) 2);
    private static final Identifier<WebServer> WS1_IDENTIFIER = new Identifier<>((long) 11);
    private static final Identifier<WebServer> WS2_IDENTIFIER = new Identifier<>((long) 12);
    private static final Identifier<Group> GROUP1_IDENTIFIER = new Identifier<>((long) 11);
    private BlockingQueue<Message<?>> blockingQueue;

    @Autowired
    private CommandDispatchGateway gateway;

    @Autowired
    @Qualifier("jvmAggregated")
    private DirectChannel jvmCommandCompletionChannel;

    @Autowired
    @Qualifier("webServerAggregated")
    private DirectChannel wsCommandCompletionChannel;

    private Jvm mockJvm1;
    private Jvm mockJvm2;
    private WebServer mockWs1;
    private WebServer mockWs2;

    // Using static variables in unit tests that are initialized in the class level is not good practice.
    // TODO: Refactor this in the near future.
    private static List<Jvm> jvmList = new ArrayList<>();
    private static List<WebServer> wsList = new ArrayList<>();

    private Group theGroup;

    @Before
    public void setup() {
        mockJvm1 = mock(Jvm.class);
        when(mockJvm1.getId()).thenReturn(JVM1_IDENTIFIER);
        mockJvm2 = mock(Jvm.class);
        when(mockJvm2.getId()).thenReturn(JVM2_IDENTIFIER);
        final Set<Jvm> jvmSet = new HashSet<>();
        jvmSet.add(mockJvm1);
        jvmSet.add(mockJvm2);
        theGroup = new Group(GROUP1_IDENTIFIER, "group1", jvmSet);
        blockingQueue = new ArrayBlockingQueue<>(1);
    }

    class TestMessageHandler implements MessageHandler {
        @Override
        public void handleMessage(Message<?> message) throws MessagingException {
            blockingQueue.add(message);
        }
    }

    @Configuration
    @ImportResource("classpath*:META-INF/spring/integration.xml")
    static class CommonConfiguration {

        @Bean 
        public static PropertySourcesPlaceholderConfigurer configurer() { 
             PropertySourcesPlaceholderConfigurer ppc = new PropertySourcesPlaceholderConfigurer();
             ppc.setLocation(new ClassPathResource("META-INF/spring/toc-defaults.properties"));
             ppc.setLocalOverride(true);
             return ppc;
        }

        @Bean(name = "jvmControlService")
        public JvmControlService jvmControlService() {
            JvmControlService mockJvmControlService = mock(JvmControlService.class);
            return mockJvmControlService;
        }

        @Bean(name = "groupJvmControlService")
        public GroupJvmControlService groupJvmControlService() {
            GroupJvmControlService mockGroupJvmControlService = mock(GroupJvmControlService.class);
            return mockGroupJvmControlService;
        }

        @SuppressWarnings("unchecked")
        @Bean(name = "webServerPersistenceService")
        public WebServerPersistenceService getWebServerPersistenceService() {
            WebServerPersistenceService mockWebServerPersistenceService = mock(WebServerPersistenceService.class);
            when(mockWebServerPersistenceService.findWebServersBelongingTo(any(Identifier.class))).thenReturn(wsList);
            return mockWebServerPersistenceService;
        }

        @SuppressWarnings("unchecked")
        @Bean(name = "jvmPersistenceService")
        public JvmPersistenceService getJvmPersistenceService() {
            JvmPersistenceService mockJvmPersistenceService = mock(JvmPersistenceService.class);
            return mockJvmPersistenceService;
        }
        
        @Bean(name="webServerControlService")
        public WebServerControlService getWebServerControlService() {
            CommandOutput execData = new CommandOutput(new ExecReturnCode(0), "Successful.", "");
            WebServerControlService mockWebServerControlService = mock(WebServerControlService.class);
            return mockWebServerControlService;
        }

        @Bean(name="groupWebServerControlService")
        public GroupWebServerControlService getGroupWebServerControlService() {
            GroupWebServerControlService mockGroupWebServerControlService = mock(GroupWebServerControlService.class);
            return mockGroupWebServerControlService;
        }
    }*/
}
