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
	getWebAppsByGroup : function(groupId, responseCallback, loadingVisible) {
        return serviceFoundation.get("v1.0/applications?group.id=" + groupId, "json", responseCallback, loadingVisible);
    },
    getWebAppsByJvm : function(jvmId, responseCallback) {
        return serviceFoundation.get("v1.0/applications/jvm/" + jvmId, "json", responseCallback);
    },
    getResources : function(appName, responseCallback) {
        return serviceFoundation.get("v1.0/applications/" + encodeURI(appName) + "/resources/name", "json", responseCallback);
    },
    getResourceTemplate : function(appName, groupName, jvmName, tokensReplaced, resourceTemplateName, responseCallback) {
        return serviceFoundation.get("v1.0/applications/" + encodeURI(appName) + "/resources/template/" +
                                     encodeURI(resourceTemplateName) + ";groupName=" + encodeURI(groupName) +
                                     ";jvmName=" + encodeURI(jvmName) + "?tokensReplaced=" + tokensReplaced, "json", responseCallback);
    },
    updateResourceTemplate: function(appName, resourceTemplateName, resourceTemplateContent, jvmName, groupName) {
        return serviceFoundation.promisedPut("v1.0/applications/" + encodeURI(appName) + "/resources/template/" + encodeURI(resourceTemplateName) + ";groupName=" + encodeURI(groupName) + ";jvmName=" + encodeURI(jvmName),
                                     "json",
                                     resourceTemplateContent,
                                     false,
                                     "text/plain; charset=utf-8");
    },
    deployWebAppsConf: function(webAppName, groupName, jvmName, resourceTemplateName) {
        return serviceFoundation.promisedPut("v1.0/applications/" + encodeURI(webAppName) + "/conf/" +
                                             encodeURI(resourceTemplateName) + ";groupName=" + encodeURI(groupName) +
                                             ";jvmName=" + encodeURI(jvmName),
                                             "json",
                                             null,
                                             false);
     },
     uploadTemplateForm: function(webAppName, jvmName, templateName, templateFile, successCallback, errorCallback) {
         return serviceFoundation.post("v1.0/applications/" + encodeURI(webAppName) + "/resources/uploadTemplate;templateName=" + encodeURI(templateName) + ";jvmName="+encodeURI(jvmName),
                                         "json",
                                         templateFile,
                                         successCallback,
                                         errorCallback,
                                         false,
                                         "multipart/form-data",
                                         true);
     },
     previewResourceFile: function(appName, groupName, jvmName, template, successCallback, errorCallback) {
        return serviceFoundation.put("v1.0/applications/" + encodeURI(appName) + "/resources/preview;groupName=" +
                                     encodeURI(groupName) + ";jvmName=" + encodeURI(jvmName),
                                     "json",
                                     template,
                                     successCallback,
                                     errorCallback,
                                     false,
                                     "text/plain; charset=utf-8");
    }

};
