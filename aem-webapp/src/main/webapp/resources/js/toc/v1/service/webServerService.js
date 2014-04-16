/*
 
		jQuery.getScript('/aem/public-resources/js/toc/v1/service/webServerService.js');
		alert('wait for script to load'); 
		var svc = webServerService;
		svc.getWebServers().then(function(e) { 
        window.t = e;
        if(e.readyState == 4)
        {
            alert(e.applicationResponseContent);
        }}, function(e) { 
        window.t = e;
        if(e.readyState == 4 ) {
            alert(e.responseText);
        }
    });

 */
var webServerService = {

	/* 		jQuery.getScript('/aem/public-resources/js/toc/v1/service/webServerService.js');
			alert('waiting for script to load'); 
			var svc = webServerService;
			svc.insertNewWebServer('Apache Local on 8080',1, 'localhost',8080).then(function(e) { 
	        window.t = e;
	        if(e.readyState == 4)
	        {
	            alert(e.applicationResponseContent);
	        }}, function(e) { 
	        window.t = e;
	        if(e.readyState == 4 ) {
	            alert(e.responseText);
	        }
	    });
	 */
	insertNewWebServer : function(webserverName, groupIds, hostName, portNumber) {
		return serviceFoundation
				.post("v1.0/webservers", "json", JSON.stringify([{ webserverName: webserverName, groupIds: groupIds, hostName:hostName, portNumber:portNumber}]) );
	},
	
	/* 		jQuery.getScript('/aem/public-resources/js/toc/v1/service/webServerService.js');
	alert('waiting for script to load'); 
	var svc = webServerService;
	svc.updateWebServer([
		{name:'webserverId',value:'1'},
		{name:'groupId',value:'1'},
		{name:'webserverName',value:'TOC test 2'},
		{name:'hostName',value:'localhost'},
		{name:'portNumber',value:81},
	]).then(function(e) { 
    window.t = e;
    if(e.readyState == 4)
    {
        alert(e.applicationResponseContent);
    }}, function(e) { 
    window.t = e;
    if(e.readyState == 4 ) {
        alert(e.responseText);
    }
});
*/	
	updateWebServer : function(webserverFormArray) {
		return serviceFoundation.put("v1.0/webservers/", "json",
				serviceFoundation.serializedFormToJsonNoId(webserverFormArray));
	},
	
	/*
	jQuery.getScript('/aem/public-resources/js/toc/v1/service/webServerService.js')
	 var svc = webServerService; svc.deleteWebServer(1).then(function(e) { 
   window.t = e;
   if(e.readyState == 4)
   {
       alert(e.applicationResponseContent);
   }}, function(e) {
	  
	 if(e.readyState == 4) { alert(e.responseText); } else
	 alert(e.responseText); }); 
	 */
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
