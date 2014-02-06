package com.siemens.cto.aem.web.controller;

import com.siemens.cto.aem.web.model.JvmInfo;
import junit.framework.TestCase;
import java.util.List;

public class JvmInfoControllerTest extends TestCase {

    private final JvmInfoController jvmInfoCtrl = new JvmInfoController();

    public void testGetJvmInfoList() {
        List<JvmInfo> jvmInfoList = jvmInfoCtrl.getJvmInfoList();
        assertNotNull(jvmInfoList);
        assertEquals(3, jvmInfoList.size());
    }

}
