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
                } else {
                    json[this.name] = this.value;
                }
            }

        });
        json["groupIds"] = groupIdArray;
        return JSON.stringify(json);
    },
    insertNewJvm: function(jvm) {
        jvm = this.serializedJvmFormToJson(jvm, false);
        return serviceFoundation.post("v1.0/jvms",
                                      "json",
                                      jvm);
    },
    updateJvm: function(jvm) {
        jvm = this.serializedJvmFormToJson(jvm, true);
        return serviceFoundation.put("v1.0/jvms/",
                                     "json",
                                     jvm);
    }
};