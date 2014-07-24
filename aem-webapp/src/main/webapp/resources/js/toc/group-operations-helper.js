var groupOperationsHelper = function(){

    // TODO: Check when this gets called
    var mergeWebServerStateData = function(webServerStateById, webServers) {
        return webServers.map(function(webServer) {
                                                    return mergeSingleWebServerState(webServerStateById[webServer.id.id], webServer);
                                                  });
    };

    var mergeJvmStateData = function(jvmStateById, jvms) {
        return jvms.map(function(jvm) { return mergeSingleJvmState(jvmStateById[jvm.id.id], jvm);});
    };

    // TODO: Check when this gets called
    var mergeSingleWebServerState = function(webServerState, webServer) {
        if (webServerState !== undefined) {
            webServer.state = webServerState;
        } else {
            // TODO: decide if maybe the rendering component should render missing web server state data as UNKNOWN instead
            webServer.state = {state: "UNKNOWN",
                               asOf: Date.now(),
                               id: { id: webServer.id}};
        }
        return webServer;
    };

    var mergeSingleJvmState = function(jvmState, jvm) {
        if (jvmState !== undefined) {
            jvm.state = jvmState;
        } else {
            // TODO: decide if maybe the rendering component should render missing jvm state data as UNKNOWN instead
            jvm.state = { jvmState: "UNKNOWN",
                          asOf: Date.now(),
                          jvmId: { id: jvm.id}};
        }
        return jvm;
    };

    var keyById = function(result, data, idKey) {
        data.forEach(function(d) { result[d[idKey].id] = d;});
    };

    var combineWebServerStatesById = function(existingWebServerStates, newWebServerStates) {
        var result = {};
        var idKey = "id";

        keyById(result, existingWebServerStates, idKey);
        keyById(result, newWebServerStates, idKey);

        return result;
    };

    var combineJvmStatesById = function(existingJvmStates, newJvmStates) {
        var result = {};
        var idKey = "id";

        keyById(result, existingJvmStates, idKey);
        keyById(result, newJvmStates, idKey);

        return result;
    };

    var processWebServers = function(existingWebServers, newWebServers, webServerStatesById) {
        var combinedResult = {};
        var idKey = "id";

        keyById(combinedResult, existingWebServers, idKey);
        keyById(combinedResult, newWebServers, idKey);

        var result = extractValuesOnly(combinedResult);

        return mergeWebServerStateData(webServerStatesById, result);
    };

    var processJvms = function(existingJvms, newJvms, jvmStatesById) {
        var combinedResult = {};
        var idKey = "id";

        keyById(combinedResult, existingJvms, idKey);
        keyById(combinedResult, newJvms, idKey);

        var result = extractValuesOnly(combinedResult);

        return mergeJvmStateData(jvmStatesById, result);
    };

    var extractValuesOnly = function(valuesById) {
        var result = [];

        for (theId in valuesById) {
            if (valuesById.hasOwnProperty(theId)) {
                result.push(valuesById[theId]);
            }
        }

        return result;
    };

    var flatten = function(data) {
        var flattenHelper = function(helperData, helperResult) {
            for (var i=0; i<helperData.length; i++) {
                if (helperData[i].constructor === Array) {
                    flattenHelper(helperData[i], helperResult);
                } else {
                    helperResult.push(helperData[i]);
                }
            }
            return helperResult;
        };

        return flattenHelper(data, []);
    };

    var testMRenderVar = 0;

    return {

        processWebServerData: function(existingWebServers, newWebServers, existingWebServerStates, newWebServerStates) {
            var combinedWebServerStatesById = combineWebServerStatesById(existingWebServerStates,
                                                                         newWebServerStates);

            var processedWebServers = processWebServers(existingWebServers,
                                                        newWebServers,
                                                        combinedWebServerStatesById);

            var processedWebServerStates = extractValuesOnly(combinedWebServerStatesById);

            return {
                webServers: processedWebServers,
                webServerStates: processedWebServerStates
            };
        },

        processJvmData: function(existingJvms, newJvms, existingJvmStates, newJvmStates) {

            var combinedJvmStatesById = combineJvmStatesById(existingJvmStates,
                                                             newJvmStates);

            var processedJvms = processJvms(existingJvms,
                                            newJvms,
                                            combinedJvmStatesById);

            var processedJvmStates = extractValuesOnly(combinedJvmStatesById);

            return {
                jvms: processedJvms,
                jvmStates: processedJvmStates
            };
        },
        extractJvmDataFromGroups: function(jvmDataInGroups) {
            var result = jvmDataInGroups.map(function(group) { return group.jvms;});
            return flatten(result);
        },
        markGroupExpanded: function(groups, groupId, isExpanded) {
            var groupsById = {};
            keyById(groupsById, groups, "id");
            groupsById[groupId].isExpanded = isExpanded;
            return { groups: extractValuesOnly(groupsById)};
        },
        markJvmExpanded: function(jvms, jvmId, isExpanded) {
            var jvmsById = {};
            keyById(jvmsById, jvms, "id");
            jvmsById[jvmId].isExpanded = isExpanded;
            return { jvms: extractValuesOnly(jvmsById)};
        },

        getWebServerStatesByGroupIdAndWebServerId: function(webServers) {
            var result = [];
            webServers.forEach(function(webServer) {
                // NOTE: Unlike JVMs, Web Server Data is populated when the row is opened therefore this
                // code might not be appropriate because it pushes all the web server data regardless
                // if the group is opened or not. TODO: Check on this!
                webServer.groups.forEach(
                    function(group) {
                        result.push({groupId: group.id, webServerId: webServer.id, state: webServer.state})
                    });
            });
            return result;
        },

        getJvmStatesByGroupIdAndJvmId: function(jvms) {
            var result = [];
            jvms.forEach(function(jvm) {
                jvm.groups.forEach(function(group) { result.push( { groupId: group.id,
                                                                    jvmId: jvm.id,
                                                                    state: jvm.state
                                                                  })});
            });
            return result;
        },

        updateWebServersInDataTables: function(groupId, webServerId, state) {
            $("table[id*='web-server'][id$='" + groupId + "']").filter(function(index, elem) { return $.fn.DataTable.fnIsDataTable(elem);})
                                                        .each(function(index, elem) { $(elem).dataTable().fnDraw();});

        },

        updateDataTables: function(groupId, jvmId, state) {
            $("table[id*='jvm'][id$='" + groupId + "']").filter(function(index, elem) { return $.fn.DataTable.fnIsDataTable(elem);})
                                                        .each(function(index, elem) { $(elem).dataTable().fnDraw();});

        },

        keyWebServersById: function(webServers) {
            var result = {};
            keyById(result, webServers, "id");
            return result;
        },

        keyJvmsById: function(jvms) {
            var result = {};
            keyById(result, jvms, "id");
            return result;
        }
    }
}();