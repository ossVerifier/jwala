package com.siemens.cto.aem.ws.rest;

import com.siemens.cto.aem.service.JvmInfoService;
import com.siemens.cto.aem.service.exception.RecordNotAddedException;
import com.siemens.cto.aem.service.exception.RecordNotDeletedException;
import com.siemens.cto.aem.service.exception.RecordNotFoundException;
import com.siemens.cto.aem.service.exception.RecordNotUpdatedException;
import com.siemens.cto.aem.service.model.GroupInfo;
import com.siemens.cto.aem.service.model.JvmInfo;
import com.siemens.cto.aem.ws.rest.parameter.JvmInfoBean;
import org.apache.commons.lang.StringUtils;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static com.siemens.cto.aem.ws.rest.ApplicationResponseFactory.createApplicationResponse;
import static com.siemens.cto.aem.ws.rest.ApplicationResponseFactory.createInvalidPostDataApplicationResponse;
import static com.siemens.cto.aem.ws.rest.ApplicationResponseStatus.*;

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

            return Response.status(Response.Status.OK)
                           .entity(createApplicationResponse(appContentBuilder.build()))
                           .build();
        } catch (RecordNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity(createApplicationResponse(RECORD_NOT_FOUND, e))
                           .build();
        }
    }

    @Override
    public Response getAllJvmInfo() {
        List<JvmInfo> jvmList = jvmInfoService.getAllJvmInfo();

        ApplicationResponseContentBuilder<List<JvmInfo>> appContentBuilder =
                new ApplicationResponseContentBuilder<List<JvmInfo>>(jvmList);

        return Response.status(Response.Status.OK)
                       .entity(createApplicationResponse(appContentBuilder.build()))
                       .build();
    }

    @Override
    public Response addJvmInfo(JvmInfoBean jvmInfoBean) {
        try {
            List<String> invalidParameterList = new ArrayList<String>();
            if (StringUtils.isEmpty(jvmInfoBean.getJvmName())) {
                invalidParameterList.add("JVM Name");
            }

            if (StringUtils.isEmpty(jvmInfoBean.getHostName())) {
                invalidParameterList.add("Host Name");
            }

            if (StringUtils.isEmpty(jvmInfoBean.getGroupName())) {
                invalidParameterList.add("Group Name");
            }

            if (invalidParameterList.size() > 0) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(createInvalidPostDataApplicationResponse(invalidParameterList))
                        .build();
            }

            jvmInfoService.addJvmInfo(jvmInfoBean.getJvmName(),
                                      jvmInfoBean.getHostName(),
                                      new GroupInfo(jvmInfoBean.getGroupName()));

            return Response.status(Response.Status.CREATED)
                    .entity(createApplicationResponse(null))
                    .build();

        } catch (RecordNotAddedException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(createApplicationResponse(RECORD_NOT_ADDED, e))
                           .build();
        }
    }

    @Override
    public Response updateJvmInfo(Long id,
                                  String jvmName,
                                  String hostName) {
        try {
            jvmInfoService.updateJvmInfo(id, jvmName, hostName);
            return Response.status(Response.Status.OK)
                            .entity(createApplicationResponse(null))
                            .build();
        } catch (RecordNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(createApplicationResponse(RECORD_NOT_FOUND, e))
                    .build();
        } catch (RecordNotUpdatedException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(createApplicationResponse(RECORD_NOT_UPDATED, e))
                            .build();
        }
    }

    @Override
    public Response updateJvmInfo(Long id,
                                  String jvmName,
                                  String hostName,
                                  String groupName) {
        try {
            jvmInfoService.updateJvmInfo(id, jvmName, hostName, groupName);
            return Response.status(Response.Status.OK)
                    .entity(createApplicationResponse(null))
                    .build();
        } catch (RecordNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(createApplicationResponse(RECORD_NOT_FOUND, e))
                    .build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createApplicationResponse(RECORD_NOT_UPDATED, e))
                    .build();
        }
    }

    @Override
    public Response deleteJvm(Long id) {
        try {
            jvmInfoService.deleteJvm(id);

            return Response.status(Response.Status.OK)
                           .entity(createApplicationResponse(null))
                           .build();
        } catch (RecordNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity(createApplicationResponse(RECORD_NOT_FOUND, e))
                           .build();
        } catch (RecordNotDeletedException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                           .entity(createApplicationResponse(RECORD_NOT_DELETED, e))
                           .build();
        }
    }

    @Override
    public Response getJvmInfoByName(String name) {
        try {
            final JvmInfo jvmInfo = jvmInfoService.getJvmInfoByName(name);

            ApplicationResponseContentBuilder<JvmInfo> appContentBuilder =
                    new ApplicationResponseContentBuilder<JvmInfo>(jvmInfo);

            return Response.status(Response.Status.OK)
                    .entity(createApplicationResponse(appContentBuilder.build()))
                    .build();
        } catch (RecordNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(createApplicationResponse(RECORD_NOT_FOUND, e))
                    .build();
        }
    }

}
