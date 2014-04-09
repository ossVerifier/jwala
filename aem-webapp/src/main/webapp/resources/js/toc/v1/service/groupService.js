var groupService = {

    getGroups: function() {
        // TODO: Change to v1.0/groups?all when the underlying rest service is working
        return serviceFoundation.get("v1.0/groups?offset=0&limit=10000");
    },
	insertNewGroup: function(name) {
	    return serviceFoundation.post("v1.0/groups", "json", name);
	},
	updateGroup: function(group) {
        return serviceFoundation.put("v1.0/groups/",
                                     "json",
                                     serviceFoundation.serializedFormToJsonNoId(group));
    },
	deleteGroup: function(id) {
	    return serviceFoundation.del("v1.0/groups/" + id, "json");
	},
	getGroup: function(id) {
	    return serviceFoundation.get("v1.0/groups/" + id);
	}
}