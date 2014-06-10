var jvmStateService = function() {

    var constructJvmIdParameter = function(jvmId) {
        return "jvmId=" + jvmId;
    };

    var constructJvmIdParameters = function(ids) {
        if (ids === undefined) {
            return "";
        } else {
            return "?" + $.map(ids, constructJvmIdParameter).join("&");
        }
    };

    var constructTimeoutParameter = function(timeout) {
        if (timeout === undefined) {
            return "";
        } else {
            return "?timeout=" + timeout;
        }
    };

    var sendToDataSinkThunk = function(dataSink) {
        return function(data) {
            dataSink.consume(data.applicationResponseContent);
            return data.applicationResponseContent;
        }
    };

    var recurseThunk = function(timeout, dataSink) {
        return function() {
            return jvmStateService.pollForUpdates(timeout, dataSink);
        }
    };

    return {
        pollForUpdates : function(timeout, dataSink) {
            if (dataSink.shouldContinue()) {
                return serviceFoundation.promisedGet("v1.0/jvms/states" + constructTimeoutParameter(timeout),"json")
                                        .then(sendToDataSinkThunk(dataSink))
                                        .then(recurseThunk(timeout, dataSink))
                                        .caught(function(e) { console.log("State error occurred"); return Promise.delay(30000).then(recurseThunk(timeout, dataSink));});

            }
        },
        getCurrentStates : function(ids) {
            return serviceFoundation.promisedGet("v1.0/jvms/states/current" + constructJvmIdParameters(ids),
                                                 "json");
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