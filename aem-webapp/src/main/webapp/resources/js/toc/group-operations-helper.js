var groupOperationsHelper = function(){

    var mergeJvmStateData = function(jvmStateById, jvms) {
        return jvms.map(function(jvm) { return mergeSingleJvmState(jvmStateById[jvm.id.id], jvm);});
    };

    var mergeSingleJvmState = function(jvmState, jvm) {
        if (jvmState !== undefined) {
            jvm.state = jvmState;
        } else {
//            TODO decide if maybe the rendering component should render missing jvm state data as UNKNOWN instead
            jvm.state = { jvmState: "UNKNOWN",
                          asOf: Date.now(),
                          jvmId: { id: jvm.id}};
        }
        return jvm;
    };

    var keyById = function(result, data, idKey) {
        data.forEach(function(d) { result[d[idKey].id] = d;});
    };

    var combineJvmStatesById = function(existingJvmStates, newJvmStates) {
        var result = {};
        var idKey = "jvmId";

        keyById(result, existingJvmStates, idKey);
        keyById(result, newJvmStates, idKey);

        return result;
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
        updateDataTables: function(groupId, jvmId, state) {
            $("table[id*='jvm'][id$='" + groupId + "']").filter(function(index, elem) { return $.fn.DataTable.fnIsDataTable(elem);})
                                                        .each(function(index, elem) { $(elem).dataTable().fnDraw();});

        },
        keyJvmsById: function(jvms) {
            var result = {};
            keyById(result, jvms, "id");
            return result;
        }
    }
}();