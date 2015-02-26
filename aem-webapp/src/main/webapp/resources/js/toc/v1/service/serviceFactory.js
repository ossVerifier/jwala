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
    getStateService: function() {
        return stateService;
    },
    getAdminService: function() {
    	return adminService;
    }
};
