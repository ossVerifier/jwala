var resourceService = {
    getResourceTypes: function(responseCallback) {
        return serviceFoundation.get("v1.0/resources/types", "json", responseCallback);
    },
    getResources: function(groupName, responseCallback) {
        responseCallback(groupName, {"message":"SUCCESS","applicationResponseContent":[],"msgCode":"0"});
    },
    saveResource: function(groupName, resourceName) {
        // console.log("Saving " + groupName + "  " + resourceName);
    },
    updateRsource: function(resource) {
        // console.log("Update resource...");
    },
    deleteResource: function(resource) {
        // console.log("Delete resource...");
    }
};