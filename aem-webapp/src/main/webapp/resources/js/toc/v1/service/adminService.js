var adminService = {

    encryptServerSide: function(toEncrypt, successCallback, errorCallback) {
        return serviceFoundation.post("v1.0/admin/properties/encrypt", "json", toEncrypt, successCallback, errorCallback, true, "text/plain");
    },
    
    reloadProperties: function(successCallback) {
    	return  serviceFoundation.get("v1.0/admin/properties/reload", "json", successCallback);
    },

    viewProperties: function(successCallback) {
    	return  serviceFoundation.get("v1.0/admin/properties/view", "json", successCallback);
    }
    
}