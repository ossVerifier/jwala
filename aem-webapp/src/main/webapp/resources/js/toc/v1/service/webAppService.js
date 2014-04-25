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
	 insertNewWebApp : function(webappName, groupId, warPath, webAppContext, successCallback, errorCallback) {
		return serviceFoundation.post("v1.0/applications",
		                              "json",
		                              JSON.stringify([{ webappName: webappName,
		                                                groupId: groupId,
		                                                warPath:warPath,
		                                                webAppContext:webAppContext}]),
		                                                successCallback,
		                                                errorCallback);
	},
	updateWebApp : function(webserverFormArray, successCallback, errorCallback) {
		return serviceFoundation.put("v1.0/applications/",
		                             "json",
				                     this.serializedWebAppFormToJson(webserverFormArray),
				                     successCallback,
				                     errorCallback);
	},
	deleteWebApp : function(id, caughtCallback) {
        return serviceFoundation.del("v1.0/applications/" + id, "json", caughtCallback);
    },
	getWebApp : function(id, responseCallback) {
		return serviceFoundation.get("v1.0/applications/" + id, "json", responseCallback);
	},
	getWebApps : function(responseCallback) {
		return serviceFoundation.get("v1.0/applications?all", "json", responseCallback);
	}
};