    package com.siemens.cto.aem.ws.rest;

import com.siemens.cto.aem.service.JvmInfo;
import com.siemens.cto.aem.service.JvmInfoService;
import com.siemens.cto.aem.service.RecordNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.core.Response;
import java.util.List;

public class JvmInfoRestServiceImpl implements JvmInfoRestService {

    private final JvmInfoService jvmInfoService;

    public JvmInfoRestServiceImpl(JvmInfoService jvmInfoService) {
        this.jvmInfoService = jvmInfoService;
    }

    @Override
    public Response getJvmInfoById(Long id) {
        try {
            final JvmInfo jvmInfo = jvmInfoService.getJvmInfoById(id);

            ApplicationResponseContentBuilder<JvmInfo> appContentBuilder =
                    new ApplicationResponseContentBuilder<JvmInfo>(jvmInfo);
            final ApplicationResponse applicationResponse =
                    new ApplicationResponse(ApplicationResponseStatus.SUCCESS.getCode(),
                                            ApplicationResponseStatus.SUCCESS.name(),
                                            appContentBuilder.build());

            return Response.status(Response.Status.OK).entity(applicationResponse).build();
        } catch (RecordNotFoundException e) {
            final ApplicationResponse applicationResponse =
                    new ApplicationResponse(ApplicationResponseStatus.RECORD_NOT_FOUND.getCode(),
                                            e.getMessage(),
                                            null);
            return Response.status(Response.Status.OK).entity(applicationResponse).build();
        }
    }

    @Override
    public Response getAllJvmInfo() {
        List<JvmInfo> jvmList = jvmInfoService.getAllJvmInfo();

        ApplicationResponseContentBuilder<List<JvmInfo>> appContentBuilder =
                new ApplicationResponseContentBuilder<List<JvmInfo>>(jvmList);
        final ApplicationResponse applicationResponse =
                new ApplicationResponse(ApplicationResponseStatus.SUCCESS.getCode(),
                    ApplicationResponseStatus.SUCCESS.name(),
                    appContentBuilder.build());

        return Response.status(Response.Status.OK).entity(applicationResponse).build();
    }

    @Override
    public Response addJvmInfo(String jvmName,
                               String hostName) {
        jvmInfoService.addJvmInfo(jvmName, hostName);
        return Response.status(Response.Status.OK).
                entity(new ApplicationResponse(ApplicationResponseStatus.SUCCESS.getCode(),
                                               ApplicationResponseStatus.SUCCESS.name(),
                                               null)).build();
    }

    @Override
    public Response updateJvmInfo(Long id,
                                  String jvmName,
                                  String hostName) {
        jvmInfoService.updateJvmInfo(id, jvmName, hostName);
        return Response.status(Response.Status.OK)
                .entity(new ApplicationResponse(ApplicationResponseStatus.SUCCESS.getCode(),
                                                ApplicationResponseStatus.SUCCESS.name(),
                                                null)).build();
    }

    @Override
    public Response deleteJvm(Long id) {
        jvmInfoService.deleteJvm(id);
        return Response.status(Response.Status.OK)
                .entity(new ApplicationResponse(ApplicationResponseStatus.SUCCESS.getCode(),
                                                ApplicationResponseStatus.SUCCESS.name(),
                                                null)).build();
    }

}
