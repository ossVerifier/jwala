var webServerControlService = function() {

    var control = function(webServerId, operation) {
        return serviceFoundation.post("v1.0/webservers/" + webServerId + "/commands",
                                      "json",
                                      JSON.stringify({ controlOperation : operation}),
                                      undefined,
                                      undefined,
                                      false);
    };

    return {
        startWebServer : function(webServerId) {
            return control(webServerId, "start");
        },
        stopWebServer : function(webServerId) {
            return control(webServerId, "stop");
        }
    };

}();