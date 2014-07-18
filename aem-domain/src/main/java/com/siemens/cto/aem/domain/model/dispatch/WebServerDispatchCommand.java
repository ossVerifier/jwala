package com.siemens.cto.aem.domain.model.dispatch;

import com.siemens.cto.aem.domain.model.webserver.WebServer;

public class WebServerDispatchCommand extends DispatchCommand {

    private static final long serialVersionUID = 1L;
    private final GroupWebServerDispatchCommand groupWebServerDispatchCommand;
    private final WebServer webServer;

    public WebServerDispatchCommand(WebServer theWebServer, GroupWebServerDispatchCommand theGroupWebServerDispatchCommand) {
        webServer = theWebServer;
        groupWebServerDispatchCommand = theGroupWebServerDispatchCommand;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public GroupWebServerDispatchCommand getGroupWebServerDispatchCommand() {
        return groupWebServerDispatchCommand;
    }

    public WebServer getWebServer() {
        return webServer;
    }

    @Override
    public String toString() {
        return "WebServerDispatchCommand [groupWebServerDispatchCommand=" + groupWebServerDispatchCommand
                + ", webServer=" + webServer + "]";
    }
    
}
