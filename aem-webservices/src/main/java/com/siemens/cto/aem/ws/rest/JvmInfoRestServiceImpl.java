package com.siemens.cto.aem.ws.rest;

import com.siemens.cto.aem.persistence.domain.Jvm;
import com.siemens.cto.aem.service.JvmInfoService;
import com.siemens.cto.aem.ws.rest.vo.JvmInfoVo;
import com.siemens.cto.aem.ws.rest.vo.JvmInfoVoListWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.FormParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class JvmInfoRestServiceImpl implements JvmInfoRestService {

    @Autowired
    private JvmInfoService jvmInfoService;

    public JvmInfoRestServiceImpl() {}

    public JvmInfoRestServiceImpl(JvmInfoService jvmInfoService) {
        this.jvmInfoService = jvmInfoService;
    }

    @Override
    public Response getJvmInfoById(@QueryParam("id") Long id) {
        final JvmInfoVo jvmInfoVo = toJvmInfoVo(jvmInfoService.getJvmInfoById(id));
        return Response.ok(jvmInfoVo).build();
    }

    @Override
    public Response getAllJvmInfo() {
        final JvmInfoVoListWrapper jvmInfoVoListWrapper = new JvmInfoVoListWrapper();
        final List<JvmInfoVo> jvmInfoVoList = new ArrayList<JvmInfoVo>();
        List<Jvm> jvmList = jvmInfoService.getAllJvmInfo();
        for (Jvm jvm : jvmList) {
            jvmInfoVoList.add(toJvmInfoVo(jvm));
        }
        jvmInfoVoListWrapper.setJvmInfoList(jvmInfoVoList);
        return Response.ok(jvmInfoVoListWrapper).build();
    }

    @Override
    public Response addJvmInfo(@FormParam("jvmName") String jvmName,
                               @FormParam("hostName") String hostName) {
        jvmInfoService.addJvmInfo(jvmName, hostName);
        return Response.ok().build();
    }

    @Override
    public Response updateJvmInfo(@FormParam("jvmId") Long jvmId,
                                  @FormParam("jvmName") String jvmName,
                                  @FormParam("hostName") String hostName) {
        jvmInfoService.updateJvmInfo(jvmId, jvmName, hostName);
        return Response.ok().build();
    }

    @Override
    public Response deleteJvm(@QueryParam("id") Long id) {
        jvmInfoService.deleteJvm(id);
        return Response.ok().build();
    }

    /**
     * Converts the domain/jpa entity to a the value object model equivalent.
     * @param {@link Jvm}
     * @return @return {@link com.siemens.cto.aem.ws.rest.vo.JvmInfoVo}
     */
    private static final JvmInfoVo toJvmInfoVo(Jvm jvm) {
        final JvmInfoVo jvmInfoVo = new JvmInfoVo();
        jvmInfoVo.setId(jvm.getId());
        jvmInfoVo.setName(jvm.getName());
        return jvmInfoVo;
    }

}
