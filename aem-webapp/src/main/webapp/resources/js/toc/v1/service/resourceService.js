var resourceService = {
    getResourceTypes: function(responseCallback) {
        return serviceFoundation.get("v1.0/resources/types", "json", responseCallback);
    },
    getResources: function(groupName, responseCallback) {
        return serviceFoundation.get("v1.0/resources;groupName=" + groupName, "json", responseCallback);
    },
    insertNewResource: function(groupName, resourceTypeName, resourceName, attributes, successCallback, errorCallback) {
        return serviceFoundation.post("v1.0/resources",
                                      "json",
                                      JSON.stringify({groupName:groupName,
                                                      resourceTypeName:resourceTypeName,
                                                      resourceName:resourceName,
                                                      attributes:attributes}),
                                                      successCallback,
                                                      errorCallback);
    },
    updateRsource: function(resource) {
        // console.log("Update resource...");
    },
    deleteResource: function(resource) {
        // console.log("Delete resource...");
    }
};