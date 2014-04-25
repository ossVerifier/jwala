var webAppService = {
    serializedWebAppsFormToJson: function(serializedArray, forUpdate) {
        var json = {};
        $.each(serializedArray, function() {

            var excludeProp = false;
            if (forUpdate !== true && this.name === "id") {
                excludeProp = true;
            }

            if (excludeProp !== true) {
                json[this.name] = this.value;
            }

        });
        return "[" + JSON.stringify(json) + "]";
    },
	 insertNewWebServer : function(webappName, groupId, warPath, version, webAppContext, successCallback, errorCallback) {
		return serviceFoundation.post("v1.0/applications",
		                              "json",
		                              JSON.stringify([{ webappName: webappName,
		                                                groupId: groupId,
		                                                warPath:warPath,
		                                                version:version,
		                                                webAppContext:webAppContext}]),
		                                                successCallback,
		                                                errorCallback);
	},
	updateWebServer : function(webserverFormArray, successCallback, errorCallback) {
		return serviceFoundation.put("v1.0/applications/",
		                             "json",
				                     this.serializedWebServerFormToJson(webserverFormArray),
				                     successCallback,
				                     errorCallback);
	},
	deleteWebServer : function(id, caughtCallback) {
        return serviceFoundation.del("v1.0/applications/" + id, "json", caughtCallback);
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
		return serviceFoundation.get("v1.0/applications/" + id, "json", responseCallback);
	},
	/*
	 * Get list of defined web servers - no parameters needed Console test code:
	 * jQuery.getScript('/aem/public-resources/js/toc/v1/service/webServerService.js');
	 * var svc = webServerService; svc.getWebServers().then(function(e) {
	 * 
	 * if(e.readyState > 1) { alert(e.applicationResponseContent); } },null);
	 */
	getWebServers : function(responseCallback) {
		return serviceFoundation.get("v1.0/applications?all", "json", responseCallback);
	}
};