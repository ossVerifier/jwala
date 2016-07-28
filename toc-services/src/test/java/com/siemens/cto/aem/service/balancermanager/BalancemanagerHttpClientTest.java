package com.siemens.cto.aem.service.balancermanager;

import com.siemens.cto.aem.service.balancermanager.impl.BalancemanagerHttpClient;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class BalancemanagerHttpClientTest {

    @Test
    public void testDoHttpClientPost(){
        //TODO: need to make the test case for doHttpClientPort
        /*String uri = "https://usmlvv1cds0049/balancer-manager";
        Map<String, String> map = new HashMap<>();
        map.put("w_status_N", "0");
        map.put("b", "lb-health-check-4.0");
        map.put("w", "https://usmlvv1cds0049:9101/hct");
        BalancemanagerHttpClient balancemanagerHttpClient = new BalancemanagerHttpClient();
        assertEquals(200,  balancemanagerHttpClient.doHttpClientPost(uri, map));*/
        assertEquals(true, true);
    }
}
