var jvmControlService = function() {

    var control = function(jvmId, operation) {
        return serviceFoundation.post("v1.0/jvms/" + jvmId + "/commands",
                                      "json",
                                      JSON.stringify({ controlOperation : operation}),
                                      undefined,
                                      undefined,
                                      false);
    };

    return {
        startJvm : function(jvmId) {
            return control(jvmId, "start");
        },
        stopJvm : function(jvmId) {
            return control(jvmId, "stop");
        }
    };

}();