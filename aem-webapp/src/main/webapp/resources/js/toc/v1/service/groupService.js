var groupService = {

    getGroups: function(responseCallback) {
        return serviceFoundation.get("v1.0/groups?all", "json", responseCallback);
    },
	insertNewGroup: function(name, successCallback, errorCallback) {
	    return serviceFoundation.post("v1.0/groups",
	                                  "json",
	                                  name,
	                                  successCallback,
	                                  errorCallback);
	},
	updateGroup: function(group, successCallback, errorCallback) {
        return serviceFoundation.put("v1.0/groups/",
                                     "json",
                                     serviceFoundation.serializedFormToJsonNoId(group),
                                     successCallback,
                                     errorCallback);
    },
	deleteGroup: function(id, caughtCallback) {
	    return serviceFoundation.del("v1.0/groups/" + id, "json", caughtCallback);
	},
	getGroup: function(id, responseCallback) {
	    return serviceFoundation.get("v1.0/groups/" + id, "json", responseCallback);
	},
	getChildrenOtherGroupConnectionDetails: function(id) {
        return serviceFoundation.promisedGet("v1.0/groups/" + id + "/children/otherGroup/connectionDetails",
                                             "json",
                                             true);
	}
}