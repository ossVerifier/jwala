var historyService = {

    read: function(groupName, serverName) {
        if (serverName === undefined) {
            return serviceFoundation.promisedGet("v1.0/history/" + groupName + "?numOfRec=" + tocVars["historyReadMaxRecCount"],
                                                 "json", true);
        }
        return serviceFoundation.promisedGet("v1.0/history/" + groupName + "/" + serverName + "?numOfRec=" + tocVars["historyReadMaxRecCount"],
                                             "json", true);
    }

}