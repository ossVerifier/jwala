
var resourceService = {
    getResourceTypes: function(responseCallback) {
        return serviceFoundation.get("v1.0/resources/types", "json", responseCallback);
    },
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
    getXmlSnippet: function(resourceName, groupName, responseCallback) {
        return serviceFoundation.get("v1.0/resources/" + resourceName + "/preview;groupName=" + groupName, "json", responseCallback);
    },
    getTemplate: function(resourceTypeName, responseCallback) {
        return serviceFoundation.get("v1.0/resources/types/" + resourceTypeName + "/template", "json", responseCallback);
    },
    createResource: function(targetName, formData) {
        return serviceFoundation.promisedPost("v1.0/resources/template/" + targetName, "json", formData, null, true);
    },
    deleteAllResource: function(resourceName) {
        return serviceFoundation.del("v1.0/resources/template/" + resourceName);
    },
    getResourceAttrData: function() {
        return serviceFoundation.promisedGet("v1.0/resources/data/");
    },
    getResourceTopology: function() {
        return serviceFoundation.promisedGet("v1.0/resources/topology/");
    }
};