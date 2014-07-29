var jvmControlService = function() {

    var control = function(jvmId, operation, thenCallback, caughtCallback) {
        return serviceFoundation.post("v1.0/jvms/" + jvmId + "/commands",
                                      "json",
                                      JSON.stringify({ controlOperation : operation}),
                                      thenCallback,
                                      caughtCallback,
                                      false);
    };

    return {
        startJvm : function(jvmId, thenCallback, caughtCallback) {
            return control(jvmId, "start", thenCallback, caughtCallback);
        },
        stopJvm : function(jvmId, thenCallback, caughtCallback) {
            return control(jvmId, "stop", thenCallback, caughtCallback);
        }
    };

}();