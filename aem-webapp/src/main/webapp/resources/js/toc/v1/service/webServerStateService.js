var webServerStateService = function() {

    var constructWebServerIdParameters = function(ids) {
        if (ids === undefined) {
            return "";
        } else {
            return "?" + $.param({ webServerId : ids});
        }
    };

    return {
        getCurrentStates : function(ids) {
            return serviceFoundation.promisedGet("v1.0/webservers/states/current" + constructWebServerIdParameters(ids),
                                                 "json");
        }
    };
}();