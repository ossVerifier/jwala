package com.siemens.cto.aem.service.jvm.heartbeat;

import java.io.PrintWriter;
import java.text.MessageFormat;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.siemens.cto.aem.service.jvm.state.jms.listener.JvmStateMessageListener;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.state.impl.AbstractStateServiceFacade;

public class JvmStateServiceFacade extends AbstractStateServiceFacade<Jvm, JvmState> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmStateMessageListener.class);

    private final JvmPersistenceService jvmPersistenceService;
    
    public JvmStateServiceFacade(
            final StateService<Jvm, JvmState> theService,
            final JvmPersistenceService theJvmPersistenceService) {
        super(theService,
              StateType.JVM);
        this.jvmPersistenceService = theJvmPersistenceService;
    }

    @Override
    protected SetStateCommand<Jvm, JvmState> createCommand(final CurrentState<Jvm, JvmState> currentState, final CurrentState<Jvm, JvmState> aNewCurrentState) {

        // startup/shutdown protection - check for starting/stopping        
        CurrentState<Jvm, JvmState> newState = aNewCurrentState;
        
        boolean discard = false;

        if(currentState != null) {
            // if starting, ignore stopped. If stopping, ignore started.
            // TODO: we rely on started to exit starting. However, we have no way to leave STARTING to STOPPED in case of failure.
            switch(currentState.getState()) {
                case START_REQUESTED:
                    switch(newState.getState()) {
                        case STOPPED: 
                            discard = true;
                            break;
                        default: break;
                    } break;
                case STOP_REQUESTED:
                    switch(newState.getState()) {
                        case STARTED: 
                            discard = true;
                            break;
                        default: break;
                    } break;
                default:
                    discard = false;        
                    break;
            }
        }

        if(discard) { 
            LOGGER.debug("Discarding reverse heartbeat; Jvm starting/stopping: {}", aNewCurrentState);                
            return null;
        } 

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
                        jvmStatusPath = jvm.getStatusPath().getUriPath();
                    }
                    String message = MessageFormat.format(pattern,
                            jvmName,
                            jvmHost,
                            jvmStatusPath
                    );
                    printer.println(message);
                    return;
                    
                } catch(Exception e) { 
                    LOGGER.error("Could not get JVM information from database", e);
                }
            }
        } 

        // Fallback
        super.printCustomizedStateMessageFromException(printer, anId, aNewState, anAsOf, aMessage, penultimateRootCause);
    }
}
