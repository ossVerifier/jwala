package com.siemens.cto.aem.persistence.jpa.service.jvm.impl;

import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.common.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.request.jvm.CreateJvmRequest;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.persistence.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvmConfigTemplate;
import com.siemens.cto.aem.persistence.jpa.service.impl.JvmCrudServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@EnableTransactionManagement
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = {JvmCrudServiceImplTest.Config.class
        })
public class JvmCrudServiceImplTest {

    public static final String SERVER_XML = "server.xml";
    @Autowired
    private JvmCrudServiceImpl jvmCrudService;

    private User user;
    private Jvm jvm;

    @Before
    public void setup() throws Exception {
        user = new User("testUser");
        user.addToThread();

        String testJvmName = "testJvmName";
        CreateJvmRequest createCommand = new CreateJvmRequest(testJvmName, "testHostName", 100, 101, 102, 103, 104, new Path("./stp.png"), "");
        Event<CreateJvmRequest> createJvmEvent = new Event<>(createCommand, AuditEvent.now(user));
        JpaJvm jpaJvm = jvmCrudService.createJvm(createJvmEvent);
        jvm = new Jvm(Identifier.<Jvm>id(jpaJvm.getId()), jpaJvm.getName(), new HashSet<Group>());
    }

    @After
    public void tearDown() {
        User.getThreadLocalUser().invalidate();
    }

    @Test
    public void testUploadJvmTemplateXml() throws FileNotFoundException {
        final String expectedTemplateName = SERVER_XML;
        File testTemplate = new File("./src/test/resources/HttpdSslConfTemplate.tpl");
        UploadJvmTemplateRequest uploadCommand = new UploadJvmTemplateRequest(jvm, expectedTemplateName, new FileInputStream(testTemplate)) {
            @Override
            public String getConfFileName() {
                return SERVER_XML;
            }
        };
        Event<UploadJvmTemplateRequest> uploadEvent = new Event<>(uploadCommand, AuditEvent.now(user));
        JpaJvmConfigTemplate result = jvmCrudService.uploadJvmTemplateXml(uploadEvent);
        assertEquals(expectedTemplateName, result.getTemplateName());

        // test get resource template names
        List<String> resultList = jvmCrudService.getResourceTemplateNames(jvm.getJvmName());
        assertFalse(resultList.isEmpty());
        assertEquals(1, resultList.size());
        assertEquals(SERVER_XML, resultList.get(0));

        // test get resource template
        String resultText = jvmCrudService.getResourceTemplate(jvm.getJvmName(), SERVER_XML);
        assertFalse(resultText.isEmpty());

        // test update template
        jvmCrudService.updateResourceTemplate(jvm.getJvmName(), SERVER_XML, "<server>updated content</server>");
        String resultUpdate = jvmCrudService.getResourceTemplate(jvm.getJvmName(), SERVER_XML);
        assertTrue(resultUpdate.contains("updated content"));
    }

    @Test
    public void testGetJvmTemplate() {
        String result = jvmCrudService.getJvmTemplate(SERVER_XML, jvm.getId());
        assertNotNull(result);
    }

    @Configuration
    @Import(TestJpaConfiguration.class)
    static class Config {

        @Bean
        public JvmCrudServiceImpl getJvmCrudServiceImpl() {
            return new JvmCrudServiceImpl();
        }
    }
}
