var ServiceFactory = {

	getJvmService : function() {
		return new /* com.siemens. */jvmService();
	},
	getGroupService : function() {
		return new /* com.siemens. */groupService();
	}
};
