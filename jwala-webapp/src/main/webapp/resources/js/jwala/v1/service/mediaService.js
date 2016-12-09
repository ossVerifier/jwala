var mediaService = {
    getAllMedia: function(responseCallback) {
        return serviceFoundation.get("v1.0/media", "json", responseCallback);
    },
    getMedia: function(id, responseCallback) {
        return serviceFoundation.get("v1.0/media/" + id, "json", responseCallback);
    },
    createMedia: function(serializedArray) {
        var jsonData = {};
        serializedArray.forEach(function(item){
            jsonData[item.name] = item.value;
        });
        return serviceFoundation.promisedPost("v1.0/media", "json", JSON.stringify(jsonData));
    },
    updateMedia: function(updateName, successCallback, errorCallback) {
        jvm = this.serializedJvmFormToJson(updateName, true);
        return serviceFoundation.put("v1.0/media/", "json", updateName, successCallback, errorCallback );
    },
    deleteMedia: function(name, caughtCallback) {
        return serviceFoundation.promisedDel("v1.0/media/" + encodeURIComponent(name), "json");
    },
    getMediaTypes: function() {
        return serviceFoundation.promisedGet("v1.0/media/types");
    }
};
