package com.siemens.cto.aem.ws.rest;

import com.siemens.cto.aem.ws.rest.model.JvmInfo;
import com.siemens.cto.aem.ws.rest.model.JvmInfoListWrapper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class JvmInfoService {

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

    @GET
    @Path("/jvms")
    @Produces("application/xml")
    public Response getJvmInfoList() {
        return Response.ok(getDummyJvmInfoList()).build();
    }

}
