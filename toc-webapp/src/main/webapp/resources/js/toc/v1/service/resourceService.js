
var resourceService = {
    getResources: function(groupName, responseCallback) {
        if (groupName === undefined) { return; }
        return serviceFoundation.get("v1.0/resources;groupName=" + groupName.replace(/%20/g, " "), "json", responseCallback);
    },
    insertNewResource: function(groupName, resourceTypeName, resourceName, attributes, successCallback, errorCallback) {
        return serviceFoundation.post("v1.0/resources",
                                      "json",
                                      JSON.stringify({groupName:groupName,
                                                      resourceTypeName:resourceTypeName,
                                                      name:resourceName,
                                                      attributes:attributes}),
                                                      successCallback,
                                                      errorCallback);
    },
    updateResourceName: function(groupName, resourceTypeName, resourceName, newResourceName, successCallback, errorCallback) {
        return serviceFoundation.put("v1.0/resources/" + resourceName.replace(/%20/g, " ") + ";groupName=" + groupName.replace(/%20/g, " "),
                                     "json",
                                     JSON.stringify({groupName:groupName,
                                                     resourceTypeName:resourceTypeName,
                                                     name:newResourceName,
                                                     attributes:null}),
                                     successCallback,
                                     errorCallback);
    },
    deleteResources: function(groupName, resourceNames, successCallback, errorCallback) {
        var resourceNamesMatrix = "";
        resourceNames.forEach(function(name){
            resourceNamesMatrix = resourceNamesMatrix + "resourceName=" + name.replace(/%20/g, " ") + ";";
        });
        return serviceFoundation.del("v1.0/resources;groupName=" + groupName + ";" + resourceNamesMatrix, "json", errorCallback).then(successCallback);
    },
    updateResourceAttributes: function(resourceName, groupName, resource, successCallback, errorCallback) {
        return serviceFoundation.put("v1.0/resources/" + resourceName.replace(/%20/g, " ") +  ";groupName=" + groupName.replace(/%20/g, " "),
                                     "json",
                                     JSON.stringify(resource),
                                     successCallback,
                                     errorCallback);
    },

    createResource: function(groupName, webServerName, jvmName, webAppName, formData) {
        var matrixParam = "";
        if (groupName) {
            matrixParam += ";group=" + groupName;
        }
        if (webServerName) {
            matrixParam += ";webServer=" + webServerName;
        }
        if (jvmName) {
            matrixParam += ";jvm=" + jvmName;
        }
        if (webAppName) {
            matrixParam += ";webApp=" + webAppName;
        }
        return serviceFoundation.promisedPost("v1.0/resources/data" + matrixParam, "json", formData, null, true);
    },
    deleteAllResource: function(resourceName) {
        return serviceFoundation.del("v1.0/resources/template/" + resourceName);
    },
    getResourceAttrData: function() {
        return serviceFoundation.promisedGet("v1.0/resources/data/");
    },
    getResourceTopology: function() {
        return serviceFoundation.promisedGet("v1.0/resources/topology/");
    },
    // TODO: All things regarding resources should be in here therefore we have to put resource related methods for JVM and web server here as well in the future.
    // NOTE: Also make sure to rewrite the REST service calls related to resources (for JVM and web servers) to be in the resource service, not in the group service.
    getAppResources : function(groupName, appName, responseCallback) {
        return serviceFoundation.get("v1.0/resources/" + encodeURI(groupName) + "/" + encodeURI(appName) + "/name", "json", responseCallback);
    },
    getResourceTemplate: function(groupName, appName, templateName) {
        return serviceFoundation.promisedGet("v1.0/resources/" + groupName + "/" + appName + "/" + templateName);
    },
    deleteResource: function(resourceName, groupName, webServerName, jvmName, webAppName) {
        var matrixParam = "";
        if (groupName) {
            matrixParam += ";group=" + groupName;
        }
        if (webServerName) {
            matrixParam += ";webServer=" + webServerName;
        }
        if (jvmName) {
            matrixParam += ";jvm=" + jvmName;
        }
        if (webAppName) {
            matrixParam += ";webApp=" + webAppName;
        }
        return serviceFoundation.del("v1.0/resources/template/" + resourceName + matrixParam);
    }
};