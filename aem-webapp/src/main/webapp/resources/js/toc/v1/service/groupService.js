var groupService = {

    getGroups: function(responseCallback, queryString) {
        queryString = queryString === undefined ? "" : queryString;
        return serviceFoundation.get("v1.0/groups?" + queryString, "json", responseCallback);
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
	getChildrenOtherGroupConnectionDetails: function(id, groupChildType) {
	    var queryParam = "";
	    if (groupChildType === "jvm") {
            queryParam = "?groupChildType=JVM";
	    } else if (groupChildType === "webServer") {
	        queryParam = "?groupChildType=WEB_SERVER";
	    }
        return serviceFoundation.promisedGet("v1.0/groups/" + id + "/children/otherGroup/connectionDetails" + queryParam,
                                             "json",
                                             true);
	}
}