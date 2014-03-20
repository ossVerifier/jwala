var /* statusService */groupService = {

	getCurrentStatus : function() {
		return serviceFoundation.get('services/rest/v1/statuses/current',
				'json');
	},
	getStatusMessages : function() {
		return serviceFoundation.get('services/rest/v1/db/status', 'json');
	},
	insertNewStatusMessage : function(message, shouldFail) {
		return serviceFoundation.post('services/rest/v1/db/status?fail=' + shouldFail, 'json', message);
	}
};
