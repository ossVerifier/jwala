package com.siemens.cto.aem.common.domain.model.balancermanager;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;

public class DrainStatusTest {

    @Test
    public void testDrainStatus(){
        final String appName = "myAppName";
        DrainStatus.WebServerDrainStatus.JvmDrainStatus jvm1DrainStatus = new DrainStatus.WebServerDrainStatus.JvmDrainStatus("jvm1", "drained", appName);
        DrainStatus.WebServerDrainStatus.JvmDrainStatus jvm2DrainStatus = new DrainStatus.WebServerDrainStatus.JvmDrainStatus("jvm2", "drained", appName);
        DrainStatus.WebServerDrainStatus.JvmDrainStatus jvm3DrainStatus = new DrainStatus.WebServerDrainStatus.JvmDrainStatus("jvm3", "active", appName);
        List<DrainStatus.WebServerDrainStatus.JvmDrainStatus> jvmDrainStatusList = new ArrayList<>();
        jvmDrainStatusList.add(jvm1DrainStatus);
        jvmDrainStatusList.add(jvm2DrainStatus);
        jvmDrainStatusList.add(jvm3DrainStatus);
        DrainStatus.WebServerDrainStatus webServer1DrainStatus = new DrainStatus.WebServerDrainStatus("webServer1", jvmDrainStatusList);
        DrainStatus.WebServerDrainStatus webServer2DrainStatus = new DrainStatus.WebServerDrainStatus("webServer2", jvmDrainStatusList);
        List<DrainStatus.WebServerDrainStatus> webServerDrainStatusList = new ArrayList<>();
        webServerDrainStatusList.add(webServer1DrainStatus);
        webServerDrainStatusList.add(webServer2DrainStatus);
        DrainStatus drainStatus = new DrainStatus("group1", webServerDrainStatusList);
        assertEquals(getExpectedString(), drainStatus.toString());
    }

    private String getExpectedString(){
        return "DrainStatus{groupName='group1', webServerDrainStatusList=[WebServerDrainStatus{webServerName='webServer1', jvmDrainStatusList=[JvmDrainStatus{jvmName='jvm1', drainStatus='drained', appName='myAppName'}, JvmDrainStatus{jvmName='jvm2', drainStatus='drained', appName='myAppName'}, JvmDrainStatus{jvmName='jvm3', drainStatus='active', appName='myAppName'}]}, WebServerDrainStatus{webServerName='webServer2', jvmDrainStatusList=[JvmDrainStatus{jvmName='jvm1', drainStatus='drained', appName='myAppName'}, JvmDrainStatus{jvmName='jvm2', drainStatus='drained', appName='myAppName'}, JvmDrainStatus{jvmName='jvm3', drainStatus='active', appName='myAppName'}]}]}";
    }
}
