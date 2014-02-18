package com.siemens.cto.aem.ws.rest;

import com.siemens.cto.aem.ws.rest.model.JvmInfo;
import com.siemens.cto.aem.ws.rest.model.JvmInfoListWrapper;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class JvmInfoServiceImpl implements JvmInfoService  {

    private JvmInfoListWrapper getDummyJvmInfoList() {
        List<JvmInfo> jvmInfoList = new ArrayList<JvmInfo>();

        jvmInfoList.add(new JvmInfo());
        jvmInfoList.get(0).setJvmId(1);
        jvmInfoList.get(0).setHost("CTO_HC_SRN012_1");
        jvmInfoList.get(0).setName("SRN012");

        jvmInfoList.add(new JvmInfo());
        jvmInfoList.get(1).setJvmId(1);
        jvmInfoList.get(1).setHost("CTO_HC_SRN012_1");
        jvmInfoList.get(1).setName("SRN012");

        JvmInfoListWrapper jvmInfoListWrapper = new JvmInfoListWrapper();
        jvmInfoListWrapper.setJvmInfoList(jvmInfoList);

        return jvmInfoListWrapper;
    }

    @Override
    public Response getJvmInfoList() {
        return Response.ok(getDummyJvmInfoList()).build();
    }
}
