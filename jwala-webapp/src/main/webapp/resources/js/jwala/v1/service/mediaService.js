var mediaService = {
      getMedia: function(responseCallback) {
            return serviceFoundation.get("v1.0/media?all", "json", responseCallback);
      },
      insertNewMedia: function(name, successCallback, errorCallback) {
      	    return serviceFoundation.post("v1.0/media",
      	                                  "json",
      	                                  name,
      	                                  successCallback,
      	                                  errorCallback);
      },
      updateMedia: function(updateName, successCallback, errorCallback) {
              jvm = this.serializedJvmFormToJson(updateName, true);
              return serviceFoundation.put("v1.0/media/",
                                           "json",
                                           updateName,
                                           successCallback,
                                           errorCallback );
      },
      deleteMedia: function(id, caughtCallback) {
              return serviceFoundation.del("v1.0/media/" + id, "json", caughtCallback);
      }
    };