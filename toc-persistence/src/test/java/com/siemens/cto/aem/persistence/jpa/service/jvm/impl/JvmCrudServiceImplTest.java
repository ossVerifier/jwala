package com.siemens.cto.aem.persistence.jpa.service.jvm.impl;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.request.jvm.CreateJvmRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmTemplateRequest;
import com.siemens.cto.aem.persistence.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.JpaJvmConfigTemplate;
import com.siemens.cto.aem.persistence.jpa.service.impl.JvmCrudServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
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
import org.springframework.transaction.annotation.Propagation;
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
        CreateJvmRequest createJvmRequest = new CreateJvmRequest(testJvmName, "testHostName", 100, 101, 102, 103, 104, new Path("./stp.png"), "");
        JpaJvm jpaJvm = jvmCrudService.createJvm(createJvmRequest);
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
        UploadJvmTemplateRequest uploadJvmTemplateRequest = new UploadJvmTemplateRequest(jvm, expectedTemplateName,
                new FileInputStream(testTemplate), StringUtils.EMPTY) {
            @Override
            public String getConfFileName() {
                return SERVER_XML;
            }
        };
        JpaJvmConfigTemplate result = jvmCrudService.uploadJvmTemplateXml(uploadJvmTemplateRequest);
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

    @Test
    public void testGetJvmByExactName() {
        CreateJvmRequest createJvmRequest = new CreateJvmRequest("jvm-1", "testHost", 9101, 9102, 9103, -1, 9104, new Path("./"), "");
        CreateJvmRequest createJvmWithSimilarNameRequest = new CreateJvmRequest("jvm-11", "testHost", 9111, 9112, 9113, -1, 9114, new Path("./"), "");
        JpaJvm jvmOne = jvmCrudService.createJvm(createJvmRequest);
        JpaJvm jvmOneOne = jvmCrudService.createJvm(createJvmWithSimilarNameRequest);

        Jvm foundJvm = jvmCrudService.findJvmByExactName("jvm-1");
        assertEquals(jvmOne.getName(), foundJvm.getJvmName());

        foundJvm = jvmCrudService.findJvmByExactName("jvm-11");
        assertEquals(jvmOneOne.getName(), foundJvm.getJvmName());
    }

    @Test
    public void testUpdateState() throws InterruptedException {
        final CreateJvmRequest createJvmRequest = new CreateJvmRequest("jvmName", "hostName", 0, 0, 0, 0, 0,
                new Path("./stp.png"), StringUtils.EMPTY);
        final JpaJvm newJpaJvm = jvmCrudService.createJvm(createJvmRequest);
        final Identifier<Jvm> jpaJvmId = new Identifier<>(newJpaJvm.getId());
        assertEquals(1, jvmCrudService.updateState(jpaJvmId, JvmState.JVM_STOPPED));
    }

    @Test
    public void testUpdateErrorStatus() {
        final CreateJvmRequest createJvmRequest = new CreateJvmRequest("jvmName", "hostName", 0, 0, 0, 0, 0,
                new Path("./stp.png"), StringUtils.EMPTY);
        final JpaJvm newJpaJvm = jvmCrudService.createJvm(createJvmRequest);
        final Identifier<Jvm> jpaJvmId = new Identifier<>(newJpaJvm.getId());
        assertEquals(1, jvmCrudService.updateErrorStatus(jpaJvmId, "error!"));
    }

    @Test
    public void testUpdateStateAndErrSts() {
        final CreateJvmRequest createJvmRequest = new CreateJvmRequest("jvmName", "hostName", 0, 0, 0, 0, 0,
                new Path("./stp.png"), StringUtils.EMPTY);
        final JpaJvm newJpaJvm = jvmCrudService.createJvm(createJvmRequest);
        final Identifier<Jvm> jpaJvmId = new Identifier<>(newJpaJvm.getId());
        assertEquals(1, jvmCrudService.updateState(jpaJvmId, JvmState.JVM_FAILED, "error!"));
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
