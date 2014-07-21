var groupControlService = function() {

    var control = function(groupId, operation) {
        return serviceFoundation.post("v1.0/groups/" + groupId + "/commands",
                                      "json",
                                      JSON.stringify({ controlOperation : operation}));
    };

    return {
        startGroupJvms : function(groupId) {
            return control(groupId, "start");
        },
        stopGroupJvms : function(groupId) {
            return control(groupId, "stop");
        }
    };

}();