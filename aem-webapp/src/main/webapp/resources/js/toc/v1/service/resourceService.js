var resourceService = {
    getResourceTypes: function(responseCallback) {
        return serviceFoundation.get("v1.0/resources/types", "json", responseCallback);
    }
};