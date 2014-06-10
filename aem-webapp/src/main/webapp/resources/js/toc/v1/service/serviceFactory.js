var ServiceFactory = {
    getJvmService: function () {
        return jvmService;
    },
    getGroupService: function () {
        return groupService;
    },
    getWebServerService: function () {
        return webServerService;
    },
    getWebAppService: function () {
        return webAppService;
    },
    getUserService: function () {
        return userService;
    },
    getJvmStateService: function() {
        return jvmStateService;
    }
};
