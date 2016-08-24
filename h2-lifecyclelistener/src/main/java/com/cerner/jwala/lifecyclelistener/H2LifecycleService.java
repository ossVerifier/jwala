package com.cerner.jwala.lifecyclelistener;

import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.log4j.Logger;
import org.h2.tools.Server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class H2LifecycleService {

    private static final Logger LOG = Logger.getLogger(H2LifecycleService.class);
    public static H2LifecycleService INSTANCE = new H2LifecycleService();

    private static String[] defaultArguments = new String[] {
            "-tcp",
            "-web",
    };
    private String[] arguments = null;
    private Server h2DatabaseServer_Tcp = null;
    private boolean startTcpServer = false;
    private Server h2DatabaseServer_web = null;
    private boolean startWebServer = false;
    private Server h2DatabaseServer_Pg = null;
    private boolean startPgServer;
    private ReentrantLock gate = new ReentrantLock();
    private H2LifecycleService() {    }

    private static class SysEnvSubstitutor {
        public static final String replace(final String source) {
            StrSubstitutor strSubstitutor = new StrSubstitutor(
                    new StrLookup() {
                        @Override
                        public String lookup(final String key) {
                            String value = null;
                            try {
                                value = System.getenv(key);
                            }
                            catch (NullPointerException npe) {
                                LOG.debug(key + " not found as a environment property, trying as system property");
                            }
                            if (value == null) {
                                value = System.getProperty(key);
                            }
                            if (value == null) {
                                LOG.debug("problem getting '" + key + "' as either a environment variable or a system property");
                            }
                            return value;
                        }
                    });
            return strSubstitutor.replace(source);
        }
    }

    private void loadArgumentsAndSystemPropertiesFromH2PropertiesFile() {
        Properties properties = new Properties();
        try {
            String h2Properties = System.getProperty("PROPERTIES_ROOT_PATH") + File.separator.toString() + "h2.properties";
            LOG.debug("H2Properties at " + h2Properties);
            InputStream stream = new FileInputStream( new File(h2Properties));
            properties.load(stream);
            Enumeration<Object> props = properties.keys();
            ArrayList<String> args = new ArrayList<String>();
            while (props.hasMoreElements()) {
                String key = (String)props.nextElement();
                String value = properties.getProperty(key);
                if (key != null && key.startsWith("-")) {
                    // is an argument
                    if ("-web".equals(key)) {
                        this.startWebServer = true;
                    }
                    else if ("-tcp".equals(key)) {
                        this.startTcpServer = true;
                    }
                    if ("-pg".equals(key)) {
                        this.startPgServer = true;
                    }
                    args.add(key);
                    if (!key.equals(value)) {
                        if (value.indexOf('$') != -1) {
                            value = SysEnvSubstitutor.replace(value);
                        }
                        args.add(value);
                        LOG.debug("Argument found:  " + key + " = " + value);
                    }
                    else {
                        LOG.debug("Argument found:  " + key);
                    }
                }
                else {
                    System.setProperty(key, value);
                    LOG.debug("System property found: " + key + " = " + value);
                }
            }
            this.arguments = args.toArray(new String[args.size()]);
        }
        catch(IOException ioe) {
            return;
        }
    }
    public void startH2DatabaseServer() {

        if (this.h2DatabaseServer_Tcp == null || this.h2DatabaseServer_web == null || this.h2DatabaseServer_Pg == null) {
            try {
                gate.lock();
                this.loadArgumentsAndSystemPropertiesFromH2PropertiesFile();
                if (arguments == null || arguments.length == 0) {
                    this.arguments = defaultArguments;
                }
                if (INSTANCE.h2DatabaseServer_Tcp == null  && this.startTcpServer) {
                    try {
                        LOG.debug("Starting H2 database TCP");
                        this.h2DatabaseServer_Tcp = Server.createTcpServer(arguments);
                        this.h2DatabaseServer_Tcp.start();
                        LOG.debug("H2 database TCP started on " + this.h2DatabaseServer_Tcp.getService().getURL());
                    } catch (SQLException sqle) {
                        LOG.debug(sqle.getMessage());
                        System.err.println(sqle.getMessage());
                        sqle.printStackTrace();
                    }
                }
                if (INSTANCE.h2DatabaseServer_web == null && this.startWebServer) {
                    LOG.debug("Starting H2 web console");
                    try {
                        this.h2DatabaseServer_web = Server.createWebServer(arguments);
                        this.h2DatabaseServer_web.start();
                        LOG.debug("H2 database web console avaiable on " + this.h2DatabaseServer_web.getService().getURL());
                    } catch (SQLException sqle) {
                        LOG.debug(sqle.getMessage());
                        System.err.println(sqle.getMessage());
                        sqle.printStackTrace();
                    }
                }
                if (INSTANCE.h2DatabaseServer_Pg == null && this.startPgServer) {
                    LOG.debug("Starting H2 web Postgres interface");
                    try {
                        this.h2DatabaseServer_Pg = Server.createPgServer(arguments);
                        this.h2DatabaseServer_Pg.start();
                        LOG.debug("H2 database PG server started on " + this.h2DatabaseServer_Pg.getService().getURL());
                    } catch (SQLException sqle) {
                        LOG.debug(sqle.getMessage());
                        System.err.println(sqle.getMessage());
                        sqle.printStackTrace();
                    }
                }
            }
            finally {
                gate.unlock();
            }
        }
        else {
            LOG.debug("H2 Database already fully started");
        }
    }

    public void stopH2DatabaseServer() {
        try {
            gate.lock();
            if (INSTANCE.h2DatabaseServer_web != null) {
                h2DatabaseServer_web.stop();
            }
            if (INSTANCE.h2DatabaseServer_Pg != null) {
                h2DatabaseServer_Pg.stop();
            }
            if (INSTANCE.h2DatabaseServer_Tcp != null) {
                h2DatabaseServer_Tcp.stop();
            }
        }
        finally {
            gate.unlock();
        }
        arguments = null;
    }
}
