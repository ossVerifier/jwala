package com.siemens.cto.aem.service.dispatch;

import org.springframework.integration.annotation.Payload;

import com.siemens.cto.aem.domain.model.exec.ExecCommand;

public interface ExecutorGatewayBean {

    public void execute(@Payload ExecCommand exec);
}
