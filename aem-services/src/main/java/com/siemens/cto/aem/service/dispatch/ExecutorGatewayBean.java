package com.siemens.cto.aem.service.dispatch;

import com.siemens.cto.aem.domain.command.exec.ExecCommand;
import org.springframework.integration.annotation.Payload;

public interface ExecutorGatewayBean {

    public void execute(@Payload ExecCommand exec);
}
