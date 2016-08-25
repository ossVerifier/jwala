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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

public class H2LifecycleService {

    private static final Logger LOG = Logger.getLogger(H2LifecycleService.class);
    public static H2LifecycleService INSTANCE = new H2LifecycleService();
    public static final String PROPERTIES_FILE_NAME = "vars.properties";
    private static String[] defaultArguments = new String[]{
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

    public H2LifecycleService() {
    }

    public static class SysEnvSubstitutor {
        public static final String replace(final String source) {
            StrSubstitutor strSubstitutor = new StrSubstitutor(
                    new StrLookup() {
                        @Override
                        public String lookup(final String key) {
                            String value;
                            value = System.getenv(key);
                            if (value == null) {
                                final String sysEnvNotFoundMessage = key + " not found as a environment property, trying as system property";
                                LOG.debug(sysEnvNotFoundMessage);
                                System.out.println(sysEnvNotFoundMessage);
                            }
                            if (value == null) {
                                value = System.getProperty(key);
                            }
                            if (value == null) {
                                final String sysPropertyNotFoundMessage = "problem getting " + key + " as either a environment variable or a system property";
                                LOG.debug(sysPropertyNotFoundMessage);
                                System.out.println(sysPropertyNotFoundMessage);
                            }
                            System.out.println("value: " + value);
                            return value;
                        }
                    });
            return strSubstitutor.replace(source);
        }
    }

    public void loadArgumentsAndSystemPropertiesFromH2PropertiesFile() {
        Properties properties = new Properties();
        try {
            String h2Properties = System.getProperty("PROPERTIES_ROOT_PATH") + File.separator.toString() + PROPERTIES_FILE_NAME;
            print("H2Properties at " + h2Properties);
            InputStream stream = new FileInputStream(new File(h2Properties));
            properties.load(stream);
            Enumeration<Object> props = properties.keys();
            ArrayList<String> args = new ArrayList<String>();
            while (props.hasMoreElements()) {
                String key = (String) props.nextElement();
                String value = properties.getProperty(key);
                if (key != null && key.startsWith("h2")) {
                    if ("h2.web".equals(key)) {
                        this.startWebServer = true;
                    } else if ("h2.tcp".equals(key)) {
                        this.startTcpServer = true;
                    }
                    if ("h2.pg".equals(key)) {
                        this.startPgServer = true;
                    }
                    args.add(key.replace("h2.","-"));
                    if (value.indexOf('$') != -1) {
                        value = SysEnvSubstitutor.replace(value);
                    }
                    args.add(value);
                    print("H2 Argument found:  " + key + " = " + value);
                } else {
                    System.setProperty(key, value);
                    print("Argument found:  " + key + " = " + value);
                }
            }
            this.arguments = args.toArray(new String[args.size()]);
        } catch (IOException ioe) {
            LOG.error(ioe.getMessage());
            System.err.println(ioe.getMessage());
            throw new RuntimeException(ioe.getMessage());
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
                if (INSTANCE.h2DatabaseServer_Tcp == null && this.startTcpServer) {
                    try {
                        print("Starting H2 database TCP");
                        this.h2DatabaseServer_Tcp = Server.createTcpServer(arguments);
                        this.h2DatabaseServer_Tcp.start();
                        print("H2 database TCP started on " + this.h2DatabaseServer_Tcp.getService().getURL());
                    } catch (SQLException sqle) {
                        LOG.error(sqle.getMessage());
                        System.err.println(sqle.getMessage());
                        throw new RuntimeException(sqle.getMessage());
                    }
                }
                if (INSTANCE.h2DatabaseServer_web == null && this.startWebServer) {
                    print("Starting H2 web console");
                    try {
                        this.h2DatabaseServer_web = Server.createWebServer(arguments);
                        this.h2DatabaseServer_web.start();
                        print("H2 database web console avaiable on " + this.h2DatabaseServer_web.getService().getURL());
                    } catch (SQLException sqle) {
                        LOG.debug(sqle.getMessage());
                        System.err.println(sqle.getMessage());
                        throw new RuntimeException(sqle.getMessage());
                    }
                }
                if (INSTANCE.h2DatabaseServer_Pg == null && this.startPgServer) {
                    print("H2 database web console avaiable on " + this.h2DatabaseServer_web.getService().getURL());
                    try {
                        this.h2DatabaseServer_Pg = Server.createPgServer(arguments);
                        this.h2DatabaseServer_Pg.start();
                        print("H2 database PG server started on " + this.h2DatabaseServer_Pg.getService().getURL());
                    } catch (SQLException sqle) {
                        LOG.debug(sqle.getMessage());
                        System.err.println(sqle.getMessage());
                        throw new RuntimeException(sqle.getMessage());
                    }
                }
            } finally {
                gate.unlock();
            }
        } else {
            print("H2 Database already fully started");
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
        } finally {
            gate.unlock();
        }
        arguments = null;
    }

    public String[] getArguments() {
        return arguments;
    }

    public void print(final String content){
        System.out.println(content);
        LOG.debug(content);
    }
}
