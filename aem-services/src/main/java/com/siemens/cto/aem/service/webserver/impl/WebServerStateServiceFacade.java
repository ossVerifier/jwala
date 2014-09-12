package com.siemens.cto.aem.service.webserver.impl;

import java.io.PrintWriter;
import java.text.MessageFormat;

import org.joda.time.DateTime;

import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.command.SetStateCommand;
import com.siemens.cto.aem.domain.model.state.command.WebServerSetStateCommand;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.dao.webserver.WebServerDao;
import com.siemens.cto.aem.service.fault.AemExceptionMapping;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.state.impl.AbstractStateServiceFacade;

public class WebServerStateServiceFacade extends AbstractStateServiceFacade<WebServer, WebServerReachableState> {

    private final WebServerDao webServerDao;
    
    public WebServerStateServiceFacade(final StateService<WebServer, WebServerReachableState> theService, final WebServerDao theWebServerDao) {
        super(theService,
              StateType.WEB_SERVER);
        webServerDao = theWebServerDao;
    }

    @Override
    protected SetStateCommand<WebServer, WebServerReachableState> createCommand(final CurrentState<WebServer, WebServerReachableState> aNewCurrentState) {
        return new WebServerSetStateCommand(aNewCurrentState);
    }
    
    @Override 
    protected void printCustomizedStateMessageFromException(PrintWriter printer, Identifier<WebServer> anId, WebServerReachableState aNewState, DateTime anAsOf, String aMessage, Throwable penultimateRootCause) {
        
        AemFaultType faultCode = null;
        
        faultCode = AemExceptionMapping.MapGenericFaultTypesForRemoteConnections(penultimateRootCause);
        
        if(faultCode != null) { 
            String pattern = ApplicationProperties.get("error." + faultCode.getMessageCode());
            if(pattern != null) {
                
                // collect WebServer Information
                try {
                    String wsNoWs = "noWsWithId="+anId.getId();
                    String wsName = wsNoWs;
                    String wsHost = wsNoWs;
                    String wsStatusPath = wsNoWs;
                    WebServer webServer = webServerDao.getWebServer(anId);
                    if(webServer != null) {
                        wsName = webServer.getName();
                        wsHost = webServer.getHost();
                        wsStatusPath = webServer.getStatusPath().getPath();
                    }
                    String message = MessageFormat.format(pattern,
                            wsName,
                            wsHost,
                            wsStatusPath
                    );
                    printer.println(message);
                    return;
                    
                } catch(Throwable e) { 
                    e.printStackTrace(printer);
                }
            }
        } 

        // Fallback
        super.printCustomizedStateMessageFromException(printer, anId, aNewState, anAsOf, aMessage, penultimateRootCause);
    }

}
