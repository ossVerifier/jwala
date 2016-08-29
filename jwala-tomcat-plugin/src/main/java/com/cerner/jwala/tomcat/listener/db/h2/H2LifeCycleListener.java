package com.cerner.jwala.tomcat.listener.db.h2;

import com.cerner.jwala.tomcat.listener.db.DbService;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A life cycle listener that starts/stops h2 db server
 *
 * Created by JC043760 on 8/28/2016
 */
public class H2LifeCycleListener implements LifecycleListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(H2LifeCycleListener.class);
    private DbService dbService;

    private String tcpServerParam;
    private String webServerParam;

    @Override
    public void lifecycleEvent(final LifecycleEvent event) {
        if (dbService == null) {
            dbService = new H2ServiceImpl(tcpServerParam, webServerParam);
        }

        final LifecycleState lifecycleState = event.getLifecycle().getState();
        if (LifecycleState.STARTING_PREP.equals(lifecycleState) && !dbService.isRunning()) {
            LOGGER.info("Initializing H2 on Tomcat lifecyle: {}", lifecycleState);
            dbService.startServer();
        } else if (LifecycleState.DESTROYING.equals(lifecycleState) && dbService.isRunning()) {
            LOGGER.info("Destroying H2 on Tomcat lifecyle: {}", lifecycleState);
            dbService.stopServer();
        }
    }

    public void setTcpServerParam(final String tcpServerParam) {
        this.tcpServerParam = tcpServerParam;
    }

    public void setWebServerParam(final String webServerParam) {
        this.webServerParam = webServerParam;
    }
}
