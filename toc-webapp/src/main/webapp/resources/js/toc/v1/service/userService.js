var userService = {

    login : function(serializedFormData, successCallback, errorCallback) {
        return serviceFoundation.post("v1.0/user/login",
                                      "json",
                                      serializedFormData,
                                      successCallback,
                                      errorCallback,
                                      false,
                                      "application/x-www-form-urlencoded");
    },
    logout : function() {
        return serviceFoundation.post("v1.0/user/logout", "json", "", function(){
            window.location = tocVars.contextPath;
        });
    }
};