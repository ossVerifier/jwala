var webServerService = {
    serializedWebServerFormToJson: function(serializedArray, forUpdate) {
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
                } else if (this.name === "id") {
                    json["jvmId"] = this.value;
                } else {
                    json[this.name] = this.value;
                }
            }

        });
        json["groupIds"] = groupIdArray;
        return "[" + JSON.stringify(json) + "]";
    },
	insertNewWebServer : function(webserverName, groupIds, hostName, portNumber) {
		return serviceFoundation.post("v1.0/webservers",
		                              "json",
		                              JSON.stringify([{ webserverName: webserverName,
		                                                groupIds: groupIds,
		                                                hostName:hostName,
		                                                portNumber:portNumber}]) );
	},
	updateWebServer : function(webserverFormArray) {
		return serviceFoundation.put("v1.0/webservers/",
		                             "json",
				                     this.serializedWebServerFormToJson(webserverFormArray));
	},
	deleteWebServer : function(id) {
        return serviceFoundation.del("v1.0/webservers/" + id, "json");
    },
	/*
	 * Get details of one defined web server - pass identifier integer Console
	 * test code:
	 * jQuery.getScript('/aem/public-resources/js/toc/v1/service/webServerService.js')
	 * var svc = webServerService; svc.getWebServer(1).then(null, function(e) {
	 *
	 * if(e.readyState == 4) { alert(e.responseText); } else
	 * alert(e.responseText); });
	 */
	getWebServer : function(id) {
		return serviceFoundation.get("v1.0/webservers/" + id);
	},
	/*
	 * Get list of defined web servers - no parameters needed Console test code:
	 * jQuery.getScript('/aem/public-resources/js/toc/v1/service/webServerService.js');
	 * var svc = webServerService; svc.getWebServers().then(function(e) {
	 * 
	 * if(e.readyState > 1) { alert(e.applicationResponseContent); } },null);
	 */
	getWebServers : function() {
		return serviceFoundation.get("v1.0/webservers");
	}
};