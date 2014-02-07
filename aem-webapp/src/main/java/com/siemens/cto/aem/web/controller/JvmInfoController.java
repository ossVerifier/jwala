package com.siemens.cto.aem.web.controller;

import com.siemens.cto.aem.web.model.JvmInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Controller
public class JvmInfoController {

    private static final long getRandomInt(int start, int end, Random random){
        int range = end - start + 1;
        int randomNo =  ((int) (range * random.nextDouble())) + start;
        return randomNo;
    }

    /**
     * Generate dummy {@link com.siemens.cto.aem.web.model.JvmInfo} data.
     * @return a list of {@link com.siemens.cto.aem.web.model.JvmInfo}
     */
    private final List<JvmInfo> getDummyJvmInfoList() {

        final List<JvmInfo> jvmInfoList = new ArrayList<JvmInfo>();
        final int start = 1;
        final int end = 1000;

        JvmInfo.Builder jvmInfoBuilder = new JvmInfo.Builder();

        jvmInfoBuilder.setName("CTO_HC_SRN012_1").
                setHost("SRN012").
                setHttpPort("8080").
                setAvailableHeap("1.2 gb").
                setTotalHeap("3 gb").
                setHttpSessionCount(String.valueOf(getRandomInt(start, end, new Random()))).
                setHttpRequestCount(String.valueOf(getRandomInt(start, end, new Random()))).
                setGroup("Group 1");

        jvmInfoList.add(jvmInfoBuilder.build());

        jvmInfoBuilder.setName("CTO_HC_SRN012_2").
                setHost("SRN013").
                setHttpPort("8080").
                setAvailableHeap("2 gb").
                setTotalHeap("3 gb").
                setHttpSessionCount(String.valueOf(getRandomInt(start, end, new Random()))).
                setHttpRequestCount(String.valueOf(getRandomInt(start, end, new Random()))).
                setGroup("Group 2");

        jvmInfoList.add(jvmInfoBuilder.build());

        jvmInfoBuilder.setName("CTO_HC_SRN012_3").
                setHost("SRN014").
                setHttpPort("8080").
                setAvailableHeap("1.6 gb").
                setTotalHeap("2 gb").
                setHttpSessionCount(String.valueOf(getRandomInt(start, end, new Random()))).
                setHttpRequestCount(String.valueOf(getRandomInt(start, end, new Random()))).
                setGroup("Group 3");

        jvmInfoList.add(jvmInfoBuilder.build());

        return jvmInfoList;
    }

    @RequestMapping(value = "/jvminfo", method = RequestMethod.GET)
    @ResponseBody
    public List<JvmInfo> getJvmInfoList() {
        return getDummyJvmInfoList();
    }

}
