package com.siemens.cto.aem.domain.model.fault;

import com.siemens.cto.aem.common.exception.MessageResponseStatus;

public enum AemFaultType implements MessageResponseStatus {

	GROUP_NOT_FOUND("AEM1", "GroupNotFound"), 
	INVALID_GROUP_NAME("AEM2","InvalidGroupName"), 
	INVALID_IDENTIFIER("AEM3", "InvalidIdentifier"), 
	DUPLICATE_GROUP_NAME("AEM4", "DuplicateGroupName"), 
	INVALID_JVM_NAME("AEM5","InvalidJvmName"), 
	INVALID_HOST_NAME("AEM6", "InvalidHostName"), 
	GROUP_NOT_SPECIFIED("AEM7", "GroupNotSpecified"), 
	JVM_NOT_FOUND("AEM8", "JvmNotFound"), 
	JVM_NOT_SPECIFIED("AEM9", "JvmNotSpecified"), 
	WEBSERVER_NOT_FOUND("AEM10","WebServerNotFound"), 
	INVALID_WEBSERVER_NAME("AEM11","InvalidWebServerName"), 
	INVALID_WEBSERVER_HOST("AEM12", "InvalidWebServerHostName"),
	INVALID_WEBSERVER_PORT("AEM13", "InvalidWebServerPortNUmber"), 
	WEBSERVER_NOT_SPECIFIED("AEM14", "WebServerNotSpecified")
	;

	private final String faultCode;
	private final String faultMessage;

	private AemFaultType(final String theFaultCode, final String theFaultMessage) {
		faultCode = theFaultCode;
		faultMessage = theFaultMessage;
	}

	@Override
	public String getMessageCode() {
		return faultCode;
	}

	@Override
	public String getMessage() {
		return faultMessage;
	}
}
