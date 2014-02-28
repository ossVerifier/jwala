package com.siemens.cto.aem.service;

import com.siemens.cto.aem.service.model.GroupInfo;
import com.siemens.cto.aem.service.model.JvmInfo;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

public class JvmInfoServiceImplIT extends TestCase {

    private ApplicationContext context;
    private JvmInfoService jvmInfoService;
    private static final String JVM_01 = "JulietteVictoryMama_ZeroOne";

    @Override
    protected void setUp() throws Exception {
        context = new ClassPathXmlApplicationContext("META-INF/service-context.xml");
        jvmInfoService = (JvmInfoService) context.getBean("jvmInfoService");
  }

    /**
     * This test performs a "crude" CRUD integration test - get it ? :)
     * This test is inherently "flawed" since it might
     * fail if the database is dirty.
     *
     * TODO Module/pre-test init that makes sure that the database is clean.
     */
    public void testJvmCrud() {
        jvmInfoService.addJvmInfo("Test", "", new GroupInfo("Test Group"));
        List<JvmInfo> jvmInfoList = jvmInfoService.getAllJvmInfo();
        assertTrue(jvmInfoList.size() > 0);
        assertFalse(jvmInfoList.get(0).getName().equalsIgnoreCase(JVM_01));
        jvmInfoService.updateJvmInfo(jvmInfoList.get(0).getId(), JVM_01, "");
        JvmInfo jvmInfo = jvmInfoService.getJvmInfoById(jvmInfoList.get(0).getId());
        assertTrue(jvmInfo.getName().equalsIgnoreCase(JVM_01));
        assertEquals("Test Group", jvmInfoService.getAllJvmInfo().get(0).getGroupInfo().getName());
        jvmInfoService.deleteJvm(jvmInfo.getId());
        assertEquals(0, jvmInfoService.getAllJvmInfo().size());
        // TODO: Delete group!
    }

}