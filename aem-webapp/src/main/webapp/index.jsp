
<%@ page import="java.util.List"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="com.siemens.cto.aem.common.Version"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<HTML>
<HEAD>
<TITLE>Apache Enterprise Manager</TITLE>
</HEAD>

<%
    request.setAttribute("serverName", request.getServerName());
    request.setAttribute("localPortNumber", request.getLocalPort());
    request.setAttribute("osName", System.getProperty("os.name"));
    request.setAttribute("osVersion", System.getProperty("os.version"));

    request.setAttribute("title", Version.getTitle());
    request.setAttribute("version", Version.getVersion());
    request.setAttribute("buildTime", Version.getBuildTime());
%>

<BODY>
    Apache Enterprise Manager
    <br>
    <br>Host Name: ${serverName}
    <br>Port Number: ${localPortNumber}
    <br>
    <br>OS Name: ${osName}
    <br>OS Version: ${osVersion}
    <br>
    <br>Application Title: ${title}
    <br>Application Version: ${version}
    <br>Application Build Time: ${buildTime}
</BODY>
</HTML>
