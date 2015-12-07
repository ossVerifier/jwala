var historyService = {

    read: function(groupName) {
        return serviceFoundation.promisedGet("v1.0/history/" + groupName, "json", true);
    }

}