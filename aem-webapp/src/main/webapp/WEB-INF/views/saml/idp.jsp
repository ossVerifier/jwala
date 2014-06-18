<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1" isELIgnored="false" %>

<%@ page import="java.util.Enumeration" %>    
<%@ page import="java.util.HashMap" %>    
<%@ page import="java.util.Iterator" %>    
<%@ page import="javax.servlet.http.Cookie" %>

<%@ page import="com.siemens.cto.security.saml.SamlPostBindingHelper" %>
<%@ page import="com.siemens.cto.security.saml.BadRequestException" %>
<%@ page import="com.siemens.cto.security.saml.SamlTokenGenerationException" %>

<%
  // prevent page caching
  response.setHeader("Cache-Control","no-cache"); 
  response.setHeader("Pragma","no-cache");
  response.setDateHeader ("Expires", -1); 
  
  try {      
    // Check argument and determine what type of target we are dealing with
    SamlPostBindingHelper samlPostBindingHelper = new SamlPostBindingHelper();
    String redirectUrl = samlPostBindingHelper.getStringParameter(request,SamlPostBindingHelper.QUERY_PARAM_NAME_TARGETAPP, SamlPostBindingHelper.ATTRIBUTE_NAME_TARGETAPP);
    if (redirectUrl == null) {
        throw new BadRequestException("Missing argument " + SamlPostBindingHelper.PARAM_NAME_TARGETAPP);
    }

    String launchType = samlPostBindingHelper.getStringParameter(request,SamlPostBindingHelper.QUERY_PARAM_NAME_LAUNCHTYPE, SamlPostBindingHelper.ATTRIBUTE_NAME_LAUNCHTYPE);
    if (launchType != null) {
        launchType = launchType.toLowerCase();    
    }
    else {
        launchType = "";
    }
    
    String paramType = samlPostBindingHelper.getStringParameter(request,SamlPostBindingHelper.QUERY_PARAM_NAME_PARAMETERTYPE, SamlPostBindingHelper.ATTRIBUTE_NAME_PARAMETERTYPE);
    if (paramType != null) {
        paramType = paramType.toLowerCase();
    }
    else {
        paramType="none"; 
    }
    // create the SAML token	
	HashMap<String, String> getArgs = new HashMap<String, String>();
	String base64SamlToken = samlPostBindingHelper.handleRequest(request, getArgs, redirectUrl);
	
	// Check if using SAML HTTP Posting Binding or Desktop Launch
	if ( redirectUrl.startsWith(SamlPostBindingHelper.HTTP_PREFIX) || redirectUrl.startsWith(SamlPostBindingHelper.HTTPS_PREFIX) ){
		// add arguments to redirect
		Iterator<String> iter = getArgs.keySet().iterator();
		if (paramType.equals("get") ) {
		    boolean first=true;
		    for ( String name : getArgs.keySet()){
			    if ( first ){
			        redirectUrl += "?" + name + "=" + getArgs.get(name);
			        first = false;
		 	    }
		        else {
			        redirectUrl += "&" + name + "=" + getArgs.get(name);
        	   }
		    }
		}
		if (paramType.equals("post")) {
		    request.setAttribute("postSamlParams", true);
		    request.setAttribute("postParams", getArgs);
		}
		if (request.getAttribute("interopParent") != null)
		{	
			if (redirectUrl.indexOf("?") > -1)
			{
			  redirectUrl = redirectUrl + "&interopParent=SLPA";
			}
			else
			{
		      redirectUrl = redirectUrl + "?interopParent=SLPA";
			}
		}
		if (request.getAttribute("tibcoParams") != null)
		{	
			if (redirectUrl.indexOf("?") > -1)
			{
			  redirectUrl = redirectUrl + "&" + request.getAttribute("tibcoParams");
			}
			else
			{
		      redirectUrl = redirectUrl + "?" + request.getAttribute("tibcoParams");
			}
		}
		request.setAttribute("redirectUrl", redirectUrl);
		request.setAttribute("samlToken", base64SamlToken);
%>
		<jsp:include page="/post" />
<%
	}
	else if ( redirectUrl.indexOf(':')==1 || redirectUrl.startsWith(SamlPostBindingHelper.FILE_PREFIX) ) {
		String command = null;
		if ( redirectUrl.indexOf(':')==1 ) {
	    	// Use ActiveX control to launch Windows client using "c:\Path\Program.exe args" notation
		    command = redirectUrl.replace('\\','/');
		}
		else if ( redirectUrl.startsWith(SamlPostBindingHelper.FILE_PREFIX) ) {
		    // Use ActiveX control to launch Windows client using "file://c:/Path/Program.exe args" notation
			// TODO: need to test
		    command = redirectUrl.substring(SamlPostBindingHelper.FILE_PREFIX.length());
		}
		else {
			throw new BadRequestException("Malformed argument " + SamlPostBindingHelper.PARAM_NAME_TARGETAPP + "=" + redirectUrl);
		}
	
		// add arguments to redirect
		Iterator<String> iter = getArgs.keySet().iterator();
		String commandArgs = "";
		for ( String name : getArgs.keySet()){
	        commandArgs += " -" + name + " " + getArgs.get(name);
	    }
		
		// add the arguments to the command line if needed
		if ( command!=null ){
		    commandArgs =  " -samltoken " + base64SamlToken + " -samltokenlength " + base64SamlToken.length() + commandArgs; 
		}
		
		request.setAttribute("command", command);
		request.setAttribute("commandArgs", commandArgs);
%>
		<jsp:include page="/saml/launch.jsp" />
<%

		// Default behavior is to kill the session; only if explicitly set, maintain the session
		String sessionParam = samlPostBindingHelper.getStringParameter(request,SamlPostBindingHelper.QUERY_PARAM_NAME_SESSION, SamlPostBindingHelper.ATTRIBUTE_NAME_SESSION);
		if (sessionParam == null) {
		    // kill the session
		    HttpSession httpsession = request.getSession(false);
        if (httpsession != null) {
            httpsession.invalidate();
		    }
		    // kill the authentication token
		    Cookie c = new Cookie("LtpaToken2", "RESETBYSOARIAN");
		    c.setPath("/");
		    c.setMaxAge(0);
		    response.addCookie(c);
		}
	}
	else {
        throw new BadRequestException("Malformed " + SamlPostBindingHelper.PARAM_NAME_TARGETAPP + "=" + redirectUrl + ". Please use http://, https://, file:// or X:\\ format");
	}
  }
  catch (BadRequestException bre){
	    System.out.println("ERROR: idp.jsp - " + bre.toString());
	    throw bre;
  }
  catch (SamlTokenGenerationException stge){
	    System.out.println("ERROR: idp.jsp - " + stge.toString());
	    throw stge;
  }
  catch (Exception e){
      System.out.println("ERROR: idp.jsp - Major problem: " + e.toString() + "; check error logs for information");
      e.printStackTrace();
      throw e;
  }
%>
