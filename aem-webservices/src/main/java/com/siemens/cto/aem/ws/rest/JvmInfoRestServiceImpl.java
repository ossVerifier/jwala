package com.siemens.cto.aem.ws.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;

import com.siemens.cto.aem.service.JvmInfoService;
import com.siemens.cto.aem.service.exception.RecordNotAddedException;
import com.siemens.cto.aem.service.exception.RecordNotDeletedException;
import com.siemens.cto.aem.service.exception.RecordNotFoundException;
import com.siemens.cto.aem.service.exception.RecordNotUpdatedException;
import com.siemens.cto.aem.service.model.GroupInfo;
import com.siemens.cto.aem.service.model.JvmInfo;
import com.siemens.cto.aem.ws.rest.parameter.JvmInfoBean;
import com.siemens.cto.aem.ws.rest.v1.response.ResponseBuilder;

import static com.siemens.cto.aem.ws.rest.v1.response.ApplicationResponseStatus.INVALID_POST_DATA;
import static com.siemens.cto.aem.ws.rest.v1.response.ApplicationResponseStatus.RECORD_NOT_ADDED;
import static com.siemens.cto.aem.ws.rest.v1.response.ApplicationResponseStatus.RECORD_NOT_DELETED;
import static com.siemens.cto.aem.ws.rest.v1.response.ApplicationResponseStatus.RECORD_NOT_FOUND;
import static com.siemens.cto.aem.ws.rest.v1.response.ApplicationResponseStatus.RECORD_NOT_UPDATED;

@Deprecated
public class JvmInfoRestServiceImpl implements JvmInfoRestService {

    private final JvmInfoService jvmInfoService;

    public JvmInfoRestServiceImpl(final JvmInfoService jvmInfoService) {
        this.jvmInfoService = jvmInfoService;
    }

    @Override
    public Response getJvmInfoById(final Long id) {
        try {
            final JvmInfo jvmInfo = jvmInfoService.getJvmInfoById(id);

            return ResponseBuilder.ok(jvmInfo);
        } catch (final RecordNotFoundException e) {
            return ResponseBuilder.notOk(Response.Status.NOT_FOUND,
                                         RECORD_NOT_FOUND,
                                         e);
        }
    }

    @Override
    public Response getAllJvmInfo() {
        final List<JvmInfo> jvmList = jvmInfoService.getAllJvmInfo();

        return ResponseBuilder.ok(jvmList);
    }

    @Override
    public Response addJvmInfo(final JvmInfoBean jvmInfoBean) {
        try {
            final List<String> invalidParameterList = new ArrayList<String>();
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
                return ResponseBuilder.notOk(Response.Status.BAD_REQUEST,
                                             INVALID_POST_DATA,
                                             new IllegalArgumentException("Invalid parameters: " + invalidParameterList.toString()));
            }

            jvmInfoService.addJvmInfo(jvmInfoBean.getJvmName(),
                                      jvmInfoBean.getHostName(),
                                      new GroupInfo(jvmInfoBean.getGroupName()));

            return ResponseBuilder.created();

        } catch (final RecordNotAddedException e) {
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                                         RECORD_NOT_ADDED,
                                         e);
        }
    }

    @Override
    public Response updateJvmInfo(final Long id,
                                  final String jvmName,
                                  final String hostName) {
        try {
            jvmInfoService.updateJvmInfo(id, jvmName, hostName);
            return ResponseBuilder.ok();
        } catch (final RecordNotFoundException e) {
            return ResponseBuilder.notOk(Response.Status.NOT_FOUND,
                                         RECORD_NOT_FOUND,
                                         e);
        } catch (final RecordNotUpdatedException e) {
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                                         RECORD_NOT_UPDATED,
                                         e);
        }
    }

    @Override
    public Response updateJvmInfo(final Long id,
                                  final String jvmName,
                                  final String hostName,
                                  final String groupName) {
        try {
            jvmInfoService.updateJvmInfo(id, jvmName, hostName, groupName);
            return ResponseBuilder.ok();
        } catch (final RecordNotFoundException e) {
            return ResponseBuilder.notOk(Response.Status.NOT_FOUND,
                                         RECORD_NOT_FOUND,
                                         e);
        } catch (final RuntimeException e) {
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                                         RECORD_NOT_UPDATED,
                                         e);
        }
    }

    @Override
    public Response deleteJvm(final Long id) {
        try {
            jvmInfoService.deleteJvm(id);

            return ResponseBuilder.ok();
        } catch (final RecordNotFoundException e) {
            return ResponseBuilder.notOk(Response.Status.NOT_FOUND,
                                         RECORD_NOT_FOUND,
                                         e);

        } catch (final RecordNotDeletedException e) {
            return ResponseBuilder.notOk(Response.Status.INTERNAL_SERVER_ERROR,
                                         RECORD_NOT_DELETED,
                                         e);
        }
    }

    @Override
    public Response getJvmInfoByName(final String name) {
        try {
            final JvmInfo jvmInfo = jvmInfoService.getJvmInfoByName(name);

            return ResponseBuilder.ok(jvmInfo);
        } catch (final RecordNotFoundException e) {
            return ResponseBuilder.notOk(Response.Status.NOT_FOUND,
                                         RECORD_NOT_FOUND,
                                         e);
        }
    }
}
