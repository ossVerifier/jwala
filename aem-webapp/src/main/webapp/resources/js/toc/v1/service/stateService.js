var stateService = function() {

    var clientId = Date.now();

    var addTimeoutParameter = function(timeout, params) {
        if (timeout !== undefined) {
            params.timeout = timeout;
        }
    };

    var addClientIdParameter = function(clientId, params) {
        params.clientId = clientId;
    };

    var sendToDataSinkThunk = function(dataSink) {
        return function(data) {
            dataSink.consume(data.applicationResponseContent);
            return data.applicationResponseContent;
        }
    };

    var recurseThunk = function(timeout, dataSink) {
        return function() {
            return stateService.pollForUpdates(timeout, dataSink);
        }
    };

    var createPollingParameters = function(timeout, clientId) {
        var params = {};
        addTimeoutParameter(timeout, params);
        addClientIdParameter(clientId, params);
        return "?" + $.param(params);
    };

    return {
        pollForUpdates : function(timeout, dataSink) {
            if (dataSink.shouldContinue()) {
                return serviceFoundation.promisedGet("v1.0/states" + createPollingParameters(timeout, clientId),"json")
                                        .then(sendToDataSinkThunk(dataSink))
                                        .then(recurseThunk(timeout, dataSink))
                                        .caught(function(e) { console.log("State error occurred"); return Promise.delay(30000).then(recurseThunk(timeout, dataSink));});

            }
        },
        createDataSink : function(consumeFunc) {
            var shouldKeepGoing = true;
            return {
                shouldContinue: function() { return shouldKeepGoing;},
                stop: function() { shouldKeepGoing = false;},
                consume: consumeFunc
            };
        }
    };
}();