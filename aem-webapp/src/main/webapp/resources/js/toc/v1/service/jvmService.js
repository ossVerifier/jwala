    var jvmService = {
    serializedJvmFormToJson: function(serializedArray, forUpdate) {
        var json = {};
        var groupIdArray = [];
        $.each(serializedArray, function() {

            var excludeProp = false;
            if (forUpdate !== true && this.name === "id") {
                excludeProp = true;
            }

            if (excludeProp !== true) {
                if (this.name.indexOf("groupSelector[]") > -1) {
                    var id = {};
                    id["groupId"] = this.value;
                    groupIdArray.push(id);
                    // groupIdArray.push(this.value);
                } else if (this.name === "id") {
                    json["jvmId"] = this.value;
                } else {
                    json[this.name] = this.value;
                }
            }

        });
        json["groupIds"] = groupIdArray;
        return JSON.stringify(json);
    },
    insertNewJvm: function(jvmName,
                           groupIds,
                           hostName,
                           statusPath,
                           httpPort,
                           httpsPort,
                           redirectPort,
                           shutdownPort,
                           ajpPort,
                           successCallback,
                           errorCallback) {
        return serviceFoundation.post("v1.0/jvms",
                                      "json",
                                      JSON.stringify({jvmName: jvmName,
                                                      groupIds: groupIds,
                                                      hostName:hostName,
                                                      statusPath:statusPath,
                                                      httpPort: httpPort,
                                                      httpsPort: httpsPort,
                                                      redirectPort: redirectPort,
                                                      shutdownPort: shutdownPort,
                                                      ajpPort: ajpPort}),
                                                      successCallback,
                                                      errorCallback);
    },
    updateJvm: function(jvm, successCallback, errorCallback) {
        jvm = this.serializedJvmFormToJson(jvm, true);
        return serviceFoundation.put("v1.0/jvms/",
                                     "json",
                                     jvm,
                                     successCallback,
                                     errorCallback );
    },
    deleteJvm: function(id, caughtCallback) {
        return serviceFoundation.del("v1.0/jvms/" + id, "json", caughtCallback);
    },
    getJvm : function(id, responseCallback) {
        return serviceFoundation.get("v1.0/jvms/" + id, "json", responseCallback);
    },
    getJvms : function(responseCallback) {
        return serviceFoundation.get("v1.0/jvms?all", "json", responseCallback);
    }

};