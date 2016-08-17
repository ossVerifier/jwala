var webServerService = {
    serializedWebServerFormToJson: function(serializedArray, forUpdate) {
        var json = {};
        var groupIdArray = [];
        $.each(serializedArray, function() {

            var excludeProp = false;
            if (forUpdate !== true && this.name === "id") {
                excludeProp = true;
            }

            if (excludeProp !== true) {
                if (this.name.indexOf("groupSelector[]") > -1) {
                    var id = {};
                    id["groupId"] = this.value;
                    groupIdArray.push(id);
                } else if (this.name === "id") {
                    json["jvmId"] = this.value;
                } else {
                    json[this.name] = this.value;
                }
            }

        });
        json["groupIds"] = groupIdArray;
        return "[" + JSON.stringify(json) + "]";
    },
	insertNewWebServer : function(webserverName, groupIds, hostName, portNumber, httpsPort, statusPath,
	                        svrRoot, docRoot, successCallback, errorCallback) {
		return serviceFoundation.post("v1.0/webservers",
		                              "json",
		                              JSON.stringify([{ webserverName: webserverName,
		                                                groupIds: groupIds,
		                                                hostName:hostName,
		                                                portNumber:portNumber,
		                                                httpsPort:httpsPort,
                                                        statusPath:statusPath,
                                                        svrRoot:svrRoot,
                                                        docRoot:docRoot}]),
		                                                successCallback,
		                                                errorCallback);
	},
	updateWebServer : function(webserverFormArray, successCallback, errorCallback) {
		return serviceFoundation.put("v1.0/webservers/",
		                             "json",
				                     this.serializedWebServerFormToJson(webserverFormArray),
				                     successCallback,
				                     errorCallback);
	},
	deleteWebServer : function(id, caughtCallback) {
        return serviceFoundation.del("v1.0/webservers/" + id, "json", caughtCallback);
    },
	/*
	 * Get details of one defined web server - pass identifier integer Console
	 * test code:
	 * jQuery.getScript('/aem/public-resources/js/toc/v1/service/webServerService.js')
	 * var svc = webServerService; svc.getWebServer(1).then(null, function(e) {
	 *
	 * if(e.readyState == 4) { alert(e.responseText); } else
	 * alert(e.responseText); });
	 */
	getWebServer : function(id, responseCallback) {
		return serviceFoundation.get("v1.0/webservers/" + id, "json", responseCallback);
	},
	/*
	 * Get list of defined web servers - no parameters needed Console test code:
	 * jQuery.getScript('/aem/public-resources/js/toc/v1/service/webServerService.js');
	 * var svc = webServerService; svc.getWebServers().then(function(e) {
	 *
	 * if(e.readyState > 1) { alert(e.applicationResponseContent); } },null);
	 */
	getWebServers : function(responseCallback) {
		return serviceFoundation.get("v1.0/webservers?all", "json", responseCallback);
	},

    /**
     * Get a list of web servers of a group
     */
    getWebServerByGroupId : function(groupId, responseCallback, loadingVisible) {
        var REST_URL = "v1.0/webservers?groupId=" + groupId;
        if (responseCallback) {
            return serviceFoundation.get(REST_URL, "json", responseCallback, loadingVisible);
        }
        return serviceFoundation.promisedGet(REST_URL, "json", loadingVisible);
    },

    /**
     * Generate HTTPD Conf then deploy to a web server.
     */
    deployHttpdConf: function(webserverName, resourceTemplateName, successCallback, errorCallback) {
        if (successCallback === undefined) {
            return serviceFoundation.promisedPut("v1.0/webservers/" + webserverName + "/conf/" + resourceTemplateName,
                                                 "json",
                                                 null,
                                                 false);
        }
        return serviceFoundation.put("v1.0/webservers/" + webserverName + "/conf",
                                     "json",
                                     null,
                                     successCallback,
                                     errorCallback,
                                     false);
    },
    /**
     * delete the web server service, generate the httpd.conf, and then reinstall the service
     */
    deployServiceAndHttpdConf: function(webserverName, successCallback, errorCallback) {
        if (successCallback === undefined) {
            return serviceFoundation.promisedPut("v1.0/webservers/" + webserverName + "/conf/deploy",
                                                 "json",
                                                 null,
                                                 false);
        }
        return serviceFoundation.put("v1.0/webservers/" + webserverName + "/conf/deploy",
                                     "json",
                                     null,
                                     successCallback,
                                     errorCallback,
                                     false);
    },
    drainWebServer: function(groupName, webserverName, errorCallback) {
            return serviceFoundation.post("v1.0/balancermanager/" + encodeURIComponent(groupName) + "/" + encodeURIComponent(webserverName) + "/",
            "json",
            null,
            null,
            errorCallback,
            true,
            "text/plain",
            false
            );
    },
    drainJvm: function(jvmName, errorCallback) {
            return serviceFoundation.post("v1.0/balancermanager/jvm/" + encodeURIComponent(jvmName),
            "json",
            null,
            null,
            errorCallback,
            true,
            "text/plain",
            false
            );
    },
    getResources : function(webServerName, responseCallback) {
        return serviceFoundation.get("v1.0/webservers/" + encodeURIComponent(webServerName) + "/resources/name", "json", responseCallback);
    },
    updateResourceTemplate: function(webServerName, resourceTemplateName, resourceTemplateContent) {
        return serviceFoundation.promisedPut("v1.0/webservers/" + encodeURIComponent(webServerName) + "/resources/template/" + encodeURIComponent(resourceTemplateName),
                                             "json",
                                             resourceTemplateContent,
                                             false,
                                             "text/plain; charset=utf-8");
    },
     previewResourceFile: function(webServerName, groupName, template, successCallback, errorCallback) {
        return serviceFoundation.put("v1.0/webservers/" + encodeURIComponent(webServerName) + "/resources/preview;groupName=" + encodeURIComponent(groupName),
                                     "json",
                                     template,
                                     successCallback,
                                     errorCallback,
                                     false,
                                     "text/plain; charset=utf-8");
    }
};
