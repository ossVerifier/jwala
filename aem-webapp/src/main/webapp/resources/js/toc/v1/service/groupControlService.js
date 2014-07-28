var groupControlService = function() {

    var control = function(groupId, operation) {
        return serviceFoundation.post("v1.0/groups/" + groupId + "/commands",
                                      "json",
                                      JSON.stringify({ controlOperation : operation}));
    };
    
    var controlJvms = function(groupId, operation) {
        return serviceFoundation.post("v1.0/groups/" + groupId + "/jvms/commands",
                                      "json",
                                      JSON.stringify({ controlOperation : operation}));
    };
    
    var controlWebServers = function(groupId, operation) {
        return serviceFoundation.post("v1.0/groups/" + groupId + "/webservers/commands",
                                      "json",
                                      JSON.stringify({ controlOperation : operation}));
    };

    return {
        startGroup : function(groupId) {
            return control(groupId, "start");
        },
        stopGroup : function(groupId) {
            return control(groupId, "stop");
        },
        startJvms : function(groupId) {
            return controlJvms(groupId, "start");
        },
        stopJvms : function(groupId) {
            return controlJvms(groupId, "stop");
        },
        startWebServers : function(groupId) {
            return controlWebServers(groupId, "start");
        },
        stopWebServers : function(groupId) {
            return controlWebServers(groupId, "stop");
        }
    };

}();