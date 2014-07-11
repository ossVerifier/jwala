var userService = {
    logout : function() {
        return serviceFoundation.post("v1.0/user/logout", "json", "", function(){
            window.location = tocVars.contextPath;
        });
    }
};