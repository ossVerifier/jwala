package com.siemens.cto.aem.service.jvm.impl;

import java.io.PrintWriter;
import java.text.MessageFormat;

import org.joda.time.DateTime;

import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.command.JvmSetStateCommand;
import com.siemens.cto.aem.domain.model.state.command.SetStateCommand;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;
import com.siemens.cto.aem.service.fault.AemExceptionMapping;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.state.impl.AbstractStateServiceFacade;

public class JvmStateServiceFacade extends AbstractStateServiceFacade<Jvm, JvmState> {

    private final JvmPersistenceService jvmPersistenceService;
    
    public JvmStateServiceFacade(
            final StateService<Jvm, JvmState> theService,
            final JvmPersistenceService theJvmPersistenceService) {
        super(theService,
              StateType.JVM);
        this.jvmPersistenceService = theJvmPersistenceService;
    }

    @Override
    protected SetStateCommand<Jvm, JvmState> createCommand(final CurrentState<Jvm, JvmState> aNewCurrentState) {
        return new JvmSetStateCommand(aNewCurrentState);
    }
    
    @Override 
    protected void printCustomizedStateMessageFromException(PrintWriter printer, Identifier<Jvm> anId, JvmState aNewState, DateTime anAsOf, String aMessage, Throwable penultimateRootCause) {
        
        AemFaultType faultCode = null;
        
        faultCode = AemExceptionMapping.MapGenericFaultTypesForRemoteConnections(penultimateRootCause);
        
        if(faultCode != null) { 
            String pattern = ApplicationProperties.get("error." + faultCode.getMessageCode());
            if(pattern != null) {
                
                // collect Jvm Information
                try {
                    String jvmNoJvm= "noJvmWithId="+anId.getId();
                    String jvmName = jvmNoJvm;
                    String jvmHost = jvmNoJvm;
                    String jvmStatusPath = jvmNoJvm;
                    Jvm jvm = jvmPersistenceService.getJvm(anId);
                    if(jvm != null) {
                        jvmName = jvm.getJvmName();
                        jvmHost = jvm.getHostName();
                        jvmStatusPath = jvm.getStatusPath().getPath();
                    }
                    String message = MessageFormat.format(pattern,
                            jvmName,
                            jvmHost,
                            jvmStatusPath
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
