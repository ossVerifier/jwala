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
	deleteGroup: function(name, caughtCallback) {
	    return serviceFoundation.del("v1.0/groups/" + encodeURI(name) + "?byName=true", "json", caughtCallback);
	},
	getGroup: function(idOrName, responseCallback, byName) {
	    var queryString = "";
	    if (byName !== undefined) {
            queryString = "byName=" + byName;
	    }
	    return serviceFoundation.get("v1.0/groups/" + idOrName + "?" + queryString, "json", responseCallback);
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
	},
	getGroupByName: function(name, responseCallbacks) {

	}
}