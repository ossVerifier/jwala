/*
Application:
  .group {.id, .name, .jvms }
  .id {.id}
  .name
  .warPath
  .webAppContext
*/
var webAppService = {
    baseUrl: "v1.0/applications",
    prepareUploadForm: function(id, uploadForm) {
      uploadForm.action=this.baseUrl + "/" + id + "/war";
      uploadForm.method="POST";
    },
    serializedWebAppFormToJson: function(serializedArray, forUpdate) {
        var json = {};
        $.each(serializedArray, function() {

            var excludeProp = false;
            if (forUpdate !== true && this.name === "webappId") {
                excludeProp = true;
            }

            if (excludeProp !== true) {
                if (this.name === "secure") {
                    json[this.name] = this.value === "on" ? true : false;
                } else if (this.name === "loadBalance") {
                    json["loadBalanceAcrossServers"] = this.value === "acrossServers" ? true : false;
                } else {
                    json[this.name] = this.value;
                }
            }
        });

        return JSON.stringify(json);
    },
	  deleteWar : function(id, caughtCallback) {
        return serviceFoundation.del("v1.0/applications/" + id + "/war", "json", caughtCallback);
    },
	 insertNewWebApp : function(webAppFromArray, successCallback, errorCallback) {
		return serviceFoundation.post("v1.0/applications",
		                              "json",
		                              this.serializedWebAppFormToJson(webAppFromArray, false),
		                                                successCallback,
		                                                errorCallback);
	},
	updateWebApp : function(webAppFromArray, successCallback, errorCallback) {
		return serviceFoundation.put("v1.0/applications/",
		                             "json",
				                     this.serializedWebAppFormToJson(webAppFromArray, true),
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
	},
	getWebAppsByGroup : function(groupId, responseCallback) {
        return serviceFoundation.get("v1.0/applications?group.id=" + groupId, "json", responseCallback);
    },
    getWebAppsByJvm : function(jvmId, responseCallback) {
        return serviceFoundation.get("v1.0/applications/jvm/" + jvmId, "json", responseCallback);
    },
    getResources : function(appName, responseCallback) {
        return serviceFoundation.get("v1.0/applications/" + encodeURI(appName) + "/resources/name", "json", responseCallback);
    }
};