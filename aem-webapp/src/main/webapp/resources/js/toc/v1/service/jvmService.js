var /*healthService*/jvmService = {
	getHealth : function() {
		return serviceFoundation.get('services/rest/v1/health', 'json');
	},
	getDefaultHealth : function(unknownText, unknownSymbol) {
		return {
			message : {
				messageText : unknownText,
				messageID : unknownSymbol
			},
			machineInfo : {
				upTime : {
					timeInMinutes : unknownSymbol
				},
				jvmInstanceName : unknownText,
				jvmPid : unknownSymbol,
				machineName : unknownText,
				httpListenPort : unknownSymbol
			},
			asOfDate : unknownText,
			timings : {
				serverTime : {
					unit : unknownText,
					elapsedTime : unknownSymbol
				},
				dbTime : {
					unit : unknownText,
					elapsedTime : unknownSymbol
				},
				jmsTime : {
					unit : unknownText,
					elapsedTime : unknownSymbol
				}
			}
		}
	}
};