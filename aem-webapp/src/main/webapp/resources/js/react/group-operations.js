
/** @jsx React.DOM */
var GroupOperations = React.createClass({
    getInitialState: function() {
        selectedGroup = null;

        // What does the code below do ?
        this.allJvmData = { jvms: [],
                            jvmStates: []};
        return {
            // Rationalize/unify all the groups/jvms/webapps/groupTableData/etc. stuff so it's coherent
            groupFormData: {},
            groupTableData: [],
            groups: [],
            groupStates: [],
            webServers: [],
            webServerStates: [],
            jvms: [],
            jvmStates: []
        };
    },
    render: function() {
        var btnDivClassName = this.props.className + "-btn-div";
        return  <div className={this.props.className}>
                    <table style={{width:"1084px"}}>
                        <tr>
                            <td>
                                <div>
                                    <GroupOperationsDataTable data={this.state.groupTableData}
                                                              selectItemCallback={this.selectItemCallback}
                                                              groups={this.state.groups}
                                                              groupsById={groupOperationsHelper.keyGroupsById(this.state.groups)}
                                                              webServers={this.state.webServers}
                                                              jvms={this.state.jvms}
                                                              updateWebServerDataCallback={this.updateWebServerDataCallback}
                                                              stateService={this.props.stateService}/>
                                </div>
                            </td>
                        </tr>
                   </table>
               </div>
    },
    retrieveData: function() {
        var self = this;
        this.props.service.getGroups(function(response){
                                        self.setState({groupTableData:response.applicationResponseContent});
                                        self.updateJvmData(self.state.groupTableData);
                                        self.setState({ groups: response.applicationResponseContent});
                                     });
    },
    updateJvmData: function(jvmDataInGroups) {
        this.setState(groupOperationsHelper.processJvmData(this.state.jvms,
                                                           groupOperationsHelper.extractJvmDataFromGroups(jvmDataInGroups),
                                                           this.state.jvmStates,
                                                           []));
    },

    updateStateData: function(newStates) {
        var groups = [];
        var webServers = [];
        var jvms = [];

        for (var i = 0; i < newStates.length; i++) {
            if (newStates[i].type === "JVM") {
                jvms.push(newStates[i]);
            } else if (newStates[i].type === "WEB_SERVER") {
                webServers.push(newStates[i]);
            } else if (newStates[i].type === "GROUP") {
                groups.push(newStates[i]);
            }
        }

        this.updateGroupsStateData(groups);
        this.updateWebServerStateData(webServers);
        this.updateJvmStateData(jvms);
    },
    updateGroupsStateData: function(newGroupStates) {
        var groupsToUpdate = groupOperationsHelper.getGroupStatesById(this.state.groups);

        groupsToUpdate.forEach(
        function(group) {
            var groupStatusWidget = GroupOperations.groupStatusWidgetMap["grp" + group.groupId.id];
            if (newGroupStates !== undefined) {
                for (var i = 0; i < newGroupStates.length; i++) {
                    if (newGroupStates[i].id.id === group.groupId.id) {
                        groupStatusWidget.setStatus(newGroupStates[i].stateString,
                                                    newGroupStates[i].asOf,
                                                    newGroupStates[i].message);
                    }
                }
            }
        });
    },
    updateWebServerStateData: function(newWebServerStates) {
        var webServersToUpdate = groupOperationsHelper.getWebServerStatesByGroupIdAndWebServerId(this.state.webServers);
        webServersToUpdate.forEach(
        function(webServer) {
            var webServerStatusWidget = GroupOperations.webServerStatusWidgetMap["grp" + webServer.groupId.id + "webServer" + webServer.webServerId.id];
            if (webServerStatusWidget !== undefined) {
                for (var i = 0; i < newWebServerStates.length; i++) {
                    if (newWebServerStates[i].id.id === webServer.webServerId.id) {
                        webServerStatusWidget.setStatus(newWebServerStates[i].stateString,
                                                        newWebServerStates[i].asOf,
                                                        newWebServerStates[i].message);
                    }
                }
            }
        });
    },
    updateJvmStateData: function(newJvmStates) {
        var self = this;
        var jvmsToUpdate = groupOperationsHelper.getJvmStatesByGroupIdAndJvmId(this.state.jvms);
        jvmsToUpdate.forEach(function(jvm) {
            var jvmStatusWidget = GroupOperations.jvmStatusWidgetMap["grp" + jvm.groupId.id + "jvm" + jvm.jvmId.id];
            if (jvmStatusWidget !== undefined) {
                for (var i = 0; i < newJvmStates.length; i++) {
                    if (newJvmStates[i].id.id === jvm.jvmId.id) {
                        jvmStatusWidget.setStatus(newJvmStates[i].stateString,
                                                  newJvmStates[i].asOf,
                                                  newJvmStates[i].message);
                    }
                }
            }
        });
    },

    pollStates: function() {
        var self = this;
        this.dataSink = this.props.stateService.createDataSink(function(data) {
                                                                                    self.updateStateData(data);
                                                                              });
        this.props.stateService.pollForUpdates(this.props.statePollTimeout, this.dataSink);
    },
    fetchCurrentGroupStates: function() {
        var self = this;
        this.props.stateService.getCurrentGroupStates().then(function(data) { self.updateGroupsStateData(data.applicationResponseContent);})
                                                       .caught(function(e) {console.log(e);});
    },
    markGroupExpanded: function(groupId, isExpanded) {
        this.setState(groupOperationsHelper.markGroupExpanded(this.state.groups,
                                                              groupId,
                                                              isExpanded));
    },
    markJvmExpanded: function(jvmId, isExpanded) {
        this.setState(groupOperationsHelper.markJvmExpanded(this.state.jvms,
                                                            jvmId,
                                                            isExpanded));
    },
    componentDidMount: function() {
        this.retrieveData();
        this.pollStates();
        this.fetchCurrentGroupStates();
    },
    componentWillUnmount: function() {
        this.dataSink.stop();
    },
    updateWebServerDataCallback: function(webServerData) {
        this.setState(groupOperationsHelper.processWebServerData([],
                                                                 webServerData,
                                                                 this.state.webServerStates,
                                                                 []));
        this.updateWebServerStateData([]);
    },
    statics: {
        // Used in place of ref since ref will now work without a React wrapper (in the form a data table)
        groupStatusWidgetMap: {},
        webServerStatusWidgetMap: {},
        jvmStatusWidgetMap: {}
    }
});

var GroupOperationsDataTable = React.createClass({
   shouldComponentUpdate: function(nextProps, nextState) {

       // TODO: Set status here
       this.groupsById = groupOperationsHelper.keyGroupsById(nextProps.groups);

       this.hasNoData = (this.props.data.length === 0);

       return this.hasNoData;
    },
    render: function() {
        var groupTableDef = [{sTitle:"", mData: "jvms", tocType:"control", colWidth:"14px"},
                             {sTitle:"Group ID", mData:"id.id", bVisible:false},
                             {sTitle:"Group Name", mData:"name", colWidth:"651px"},
                              [{id:"startGroup",
                                sTitle:"Start Group",
                                mData:null,
                                tocType:"button",
                                btnLabel:"Start Group",
                                btnCallback:this.startGroup,
                                className:"inline-block",
                                buttonClassName:"ui-button-height",
                                extraDataToPassOnCallback:"name",
                               },
                               {tocType:"space"},
                               {id:"stopGroup",
                                sTitle:"Stop Group",
                                mData:null,
                                tocType:"button",
                                btnLabel:"Stop Group",
                                btnCallback:this.stopGroup,
                                className:"inline-block",
                                buttonClassName:"ui-button-height",
                                extraDataToPassOnCallback:"name",
                              }],

                              {sTitle:"State",
                               mData:null,
                               tocType:"custom",
                               tocRenderCfgFn: this.renderGroupStateRowData.bind(this, "grp"),
                               colWidth:"115px"}];

        var webServerOfGrpChildTableDef = [{sTitle:"Web Server ID", mData:"id.id", bVisible:false},
                                           {mData:null, colWidth:"10px"},
                                           {sTitle:"Name", mData:"name", colWidth:"340px", maxDisplayTextLen:45},
                                           {sTitle:"Host", mData:"host", colWidth:"140px", maxDisplayTextLen:20},
                                           {sTitle:"HTTP", mData:"port", colWidth:"41px"},
                                           {sTitle:"HTTPS", mData:"httpsPort", colWidth:"48px"},
                                           {sTitle:"Group",
                                            mData:"groups",
                                            tocType:"array",
                                            displayProperty:"name",
                                            maxDisplayTextLen:20,
                                            colWidth:"129px"},
                                           [{sTitle:"Load Balancer",
                                             mData:null,
                                             tocType:"link",
                                             linkLabel:"LB",
                                             hRefCallback:this.buildHRefLoadBalancerConfig},
                                            {tocType:"space"},
                                            {tocType:"space"},
                                            {sTitle:"",
                                             mData:null,
                                             tocType:"link",
                                             linkLabel:"httpd.conf",
                                             onClickCallback:this.onClickHttpdConf},
                                            {tocType:"space"},
                                            {tocType:"space"},
                                            {id:"startWebServer",
                                             sTitle:"Start",
                                             mData:null,
                                             tocType:"button",
                                             btnLabel:"",
                                             btnCallback:this.webServerStart,
                                             className:"inline-block",
                                             customSpanClassName:"ui-icon ui-icon-play",
                                             buttonClassName:"ui-button-height",
                                             extraDataToPassOnCallback:["name","groups"],
                                             onClickMessage:"Starting..."},
                                            {tocType:"space"},
                                            {id:"stopWebServer",
                                             sTitle:"Stop",
                                             mData:null,
                                             tocType:"button",
                                             btnLabel:"",
                                             btnCallback:this.webServerStop,
                                             className:"inline-block",
                                             customSpanClassName:"ui-icon ui-icon-stop",
                                             buttonClassName:"ui-button-height",
                                             extraDataToPassOnCallback:["name","groups"],
                                             onClickMessage:"Stopping..."}],
                                            {sTitle:"State",
                                             mData:null,
                                             tocType:"custom",
                                             tocRenderCfgFn: this.renderWebServerStateRowData.bind(this, "grp", "webServer"),
                                             colWidth:"105px"}];

        var webServerOfGrpChildTableDetails = {tableIdPrefix:"ws-child-table_",
                                               className:"simple-data-table",
                                               dataCallback:this.getWebServersOfGrp,
                                               title:"Web Servers",
                                               isCollapsible:true,
                                               headerComponents:[
                                                    {id:"startWebServers",
                                                     sTitle:"Start Web Servers",
                                                     mData:null,
                                                     tocType:"button",
                                                     btnLabel:"Start Web Servers",
                                                     btnCallback:this.startGroupWebServers,
                                                     className:"inline-block",
                                                     buttonClassName:"ui-button-height",
                                                    },
                                                    {id:"space1", tocType:"space"},
                                                    {id:"stopWebServers",
                                                     sTitle:"Stop Web Servers",
                                                     mData:null,
                                                     tocType:"button",
                                                     btnLabel:"Stop Web Servers",
                                                     btnCallback:this.stopGroupWebServers,
                                                     className:"inline-block",
                                                     buttonClassName:"ui-button-height",
                                                    },
                                                    {tocType:"label", className:"inline-block header-component-label", text:""}
                                               ],
                                               initialSortColumn: [[2, "asc"]]};

        webServerOfGrpChildTableDetails["tableDef"] = webServerOfGrpChildTableDef;

        var webAppOfGrpChildTableDetails = {tableIdPrefix:"web-app-child-table_",
                                            className:"simple-data-table",
                                            dataCallback:this.getApplicationsOfGrp,
                                            title:"Applications",
                                            isCollapsible:true,
                                            initialSortColumn: [[1, "asc"]]};

        var webAppOfGrpChildTableDef = [{sTitle:"Web App ID", mData:"id.id", bVisible:false},
                                        {mData:null, colWidth:"10px"},
                                        {sTitle:"Name", mData:"name"},
                                        {sTitle:"War Path", mData:"warPath", tocType:"custom", tocRenderCfgFn: this.renderWebAppRowData},
                                        {sTitle:"Context", mData:"webAppContext"}];

        webAppOfGrpChildTableDetails["tableDef"] = webAppOfGrpChildTableDef;

        var webAppOfJvmChildTableDetails = {tableIdPrefix:"web-app-child-table_jvm-child-table_", /* TODO: We may need to append the group and jvm id once this table is enabled in the next release. */
                                            className:"simple-data-table",
                                            dataCallback:this.getApplicationsOfJvm,
                                            defaultSorting: {col:5, sort:"asc"},
                                            initialSortColumn: [[1, "asc"]]};

        var webAppOfJvmChildTableDef = [{sTitle:"Web App ID", mData:"id.id", bVisible:false},
                                        {sTitle:"Web App in JVM", mData:"name"},
                                        {sTitle:"War Path", mData:"warPath"},
                                        {sTitle:"Context", mData:"webAppContext"},
                                        {sTitle:"Group", mData:"group.name"},
                                        {sTitle:"Class Name", mData:"className", bVisible:false}];

        webAppOfJvmChildTableDetails["tableDef"] = webAppOfJvmChildTableDef;

        var jvmChildTableDetails = {tableIdPrefix:"jvm-child-table_",
                                    className:"simple-data-table",
                                    /* childTableDetails:webAppOfJvmChildTaapbleDetails, !!! Disable for the Aug 11, 2014 Demo */
                                    title:"JVMs",
                                    isCollapsible:true,
                                    headerComponents:[
                                         {id:"startJvms",
                                          sTitle:"Start JVMs",
                                          mData:null,
                                          tocType:"button",
                                          btnLabel:"Start JVMs",
                                          btnCallback:this.startGroupJvms,
                                          className:"inline-block",
                                          buttonClassName:"ui-button-height",
                                         },
                                         {id:"space1", tocType:"space"},
                                         {id:"stopJvms",
                                           sTitle:"Stop JVMs",
                                           mData:null,
                                           tocType:"button",
                                           btnLabel:"Stop JVMs",
                                           btnCallback:this.stopGroupJvms,
                                           className:"inline-block",
                                           buttonClassName:"ui-button-height",
                                          },
                                         {tocType:"label", className:"inline-block header-component-label", text:""}
                                    ],
                                    initialSortColumn: [[2, "asc"]]};

        var jvmChildTableDef = [/* {sTitle:"", mData:null, tocType:"control"}, !!! Disable for the Aug 11, 2014 Demo */
                                {mData:null, colWidth:"10px"},
                                {sTitle:"JVM ID", mData:"id.id", bVisible:false},
                                {sTitle:"Name", mData:"jvmName", colWidth:"340px", maxDisplayTextLen:48},
                                {sTitle:"Host", mData:"hostName", colWidth:"140", maxDisplayTextLen:17},
                                {sTitle:"HTTP", mData:"httpPort", colWidth:"41px"},
                                {sTitle:"HTTPS", mData:"httpsPort", colWidth:"48px"},
                                {sTitle:"Group",
                                 mData:"groups",
                                 tocType:"array",
                                 displayProperty:"name",
                                 maxDisplayTextLen:20,
                                 colWidth:"138px"},
                                [{id:"tomcatManager",
                                  sTitle:"Manager",
                                  mData:null,
                                  tocType:"button",
                                  btnLabel:"",
                                  btnCallback:this.onClickMgr,
                                  className:"inline-block",
                                  customSpanClassName:"ui-icon ui-icon-mgr",
                                  buttonClassName:"ui-button-height",
                                  extraDataToPassOnCallback:["hostName","httpPort", "httpsPort"]},
                                  {id:"spacediag", tocType:"space"},
                                  {id:"diagnose",
                                      sTitle:"Check State",
                                      mData:null,
                                      tocType:"button",
                                      btnLabel:"",
                                      btnCallback:this.jvmDiagnose,
                                      className:"inline-block",
                                      customSpanClassName:"ui-icon ui-icon-wrench",
                                      buttonClassName:"ui-button-height",
                                      extraDataToPassOnCallback:["jvmName","groups"]},
                                 {tocType:"space"},
                                 {id:"healthCheck",
                                  sTitle:"Health Check",
                                  mData:null,
                                  tocType:"button",
                                  btnLabel:"",
                                  btnCallback:this.onClickHealthCheck,
                                  className:"inline-block",
                                  customSpanClassName:"ui-icon ui-icon-health-check",
                                  buttonClassName:"ui-button-height",
                                  extraDataToPassOnCallback:["hostName","httpPort", "httpsPort"]},
                                  {tocType:"space"},
                                 {id:"threadDump",
                                  sTitle:"Thread Dump",
                                  mData:null,
                                  tocType:"button",
                                  btnLabel:"",
                                  btnCallback:this.onClickThreadDump,
                                  className:"inline-block",
                                  customSpanClassName:"ui-icon ui-icon-thread-dump",
                                  buttonClassName:"ui-button-height"},
                                  {tocType:"space"},
                                 {id:"heapDump",
                                  sTitle:"Heap Dump",
                                  mData:null,
                                  tocType:"button",
                                  btnLabel:"",
                                  btnCallback:this.jvmHeapDump,
                                  className:"inline-block",
                                  customSpanClassName:"ui-icon ui-icon-heap-dump",
                                  buttonClassName:"ui-button-height",
                                  extraDataToPassOnCallback:"hostName"},
                                 {tocType:"space"},
                                 {id:"startJvm",
                                  sTitle:"Start",
                                  mData:null,
                                  tocType:"button",
                                  btnLabel:"",
                                  btnCallback:this.jvmStart,
                                  className:"inline-block",
                                  customSpanClassName:"ui-icon ui-icon-play",
                                  buttonClassName:"ui-button-height",
                                  extraDataToPassOnCallback:["jvmName","groups"],
                                  onClickMessage:"Starting..."},
                                 {tocType:"space"},
                                 {id:"stopJvm",
                                  sTitle:"Stop",
                                  mData:null,
                                  tocType:"button",
                                  btnLabel:"",
                                  btnCallback:this.jvmStop,
                                  className:"inline-block",
                                  customSpanClassName:"ui-icon ui-icon-stop",
                                  buttonClassName:"ui-button-height",
                                  extraDataToPassOnCallback:["jvmName","groups"],
                                  onClickMessage:"Stopping..."}],
                                {sTitle:"State",
                                 mData:null,
                                 tocType:"custom",
                                 tocRenderCfgFn: this.renderJvmStateRowData.bind(this, "grp", "jvm"),
                                 colWidth:"105px"}];

        jvmChildTableDetails["tableDef"] = jvmChildTableDef;

        var childTableDetailsArray = [webServerOfGrpChildTableDetails,
                                      jvmChildTableDetails,
                                      webAppOfGrpChildTableDetails];

        return <TocDataTable tableId="group-operations-table"
                             className="dataTable hierarchical"
                             tableDef={groupTableDef}
                             data={this.props.data}
                             rowSubComponentContainerClassName="row-sub-component-container"
                             childTableDetails={childTableDetailsArray}
                             selectItemCallback={this.props.selectItemCallback}
                             initialSortColumn={[[2, "asc"]]}/>
   },
   renderGroupStateRowData: function(type, dataTable, data, aoColumnDefs, itemIndex, parentId) {
      var self= this;
      aoColumnDefs[itemIndex].fnCreatedCell = function (nTd, sData, oData, iRow, iCol) {
           var key = type + oData.id.id;
           return React.render(<StatusWidget key={key} defaultStatus=""
                                    errorMsgDlgTitle={oData.name + " State Error Messages"} />, nTd, function() {
                      GroupOperations.groupStatusWidgetMap[key] = this;

                      // Fetch and set initial state
                      var statusWidget = this;
                      self.props.stateService.getCurrentGroupStates(oData.id.id)
                                             .then(function(data) {
                                                      statusWidget.setStatus(data.applicationResponseContent[0].stateString,
                                                                             data.applicationResponseContent[0].asOf,
                                                                             data.applicationResponseContent[0].message);
                                                   });

                  });
      }.bind(this);
   },
   renderWebServerStateRowData: function(parentPrefix, type, dataTable, data, aoColumnDefs, itemIndex, parentId) {
       var self= this;
       aoColumnDefs[itemIndex].fnCreatedCell = function (nTd, sData, oData, iRow, iCol) {
            var key = parentPrefix + parentId + type + oData.id.id;
            return React.render(<StatusWidget key={key} defaultStatus=""
                                     errorMsgDlgTitle={oData.name + " State Error Messages"} />, nTd, function() {
                       GroupOperations.webServerStatusWidgetMap[key] = this;

                       // Fetch and set initial state
                       var statusWidget = this;
                       self.props.stateService.getCurrentWebServerStates(oData.id.id)
                                              .then(function(data) {
                                                       statusWidget.setStatus(data.applicationResponseContent[0].stateString,
                                                                              data.applicationResponseContent[0].asOf,
                                                                              data.applicationResponseContent[0].message);
                                                    });

                   });
       }.bind(this);
   },
   renderJvmStateRowData: function(parentPrefix, type, dataTable, data, aoColumnDefs, itemIndex, parentId) {
        var self= this;
        aoColumnDefs[itemIndex].fnCreatedCell = function (nTd, sData, oData, iRow, iCol) {
             var key = parentPrefix + parentId + type + oData.id.id;
             return React.render(<StatusWidget key={key} defaultStatus=""
                                      errorMsgDlgTitle={oData.jvmName + " State Error Messages"} />, nTd, function() {
                        GroupOperations.jvmStatusWidgetMap[key] = this;

                        // Fetch and set initial state
                        var statusWidget = this;
                        self.props.stateService.getCurrentJvmStates(oData.id.id)
                                               .then(function(data) {
                                                        statusWidget.setStatus(data.applicationResponseContent[0].stateString,
                                                                               data.applicationResponseContent[0].asOf,
                                                                               data.applicationResponseContent[0].message);
                                                     });

                    });
        }.bind(this);
   },
   renderWebAppRowData: function(dataTable, data, aoColumnDefs, itemIndex) {
          dataTable.expandCollapseEnabled = true;
          aoColumnDefs[itemIndex].mDataProp = null;
          aoColumnDefs[itemIndex].sClass = "";
          aoColumnDefs[itemIndex].bSortable = false;

          aoColumnDefs[itemIndex].mRender = function (data, type, full) {

                  return React.renderComponentToStaticMarkup(
                      <WARUpload war={data} readOnly={true} full={full} row={0} />
                    );
                }.bind(this);
   },
   getWebServersOfGrp: function(idObj, responseCallback) {
        var self = this;

        webServerService.getWebServerByGroupId(idObj.parentId, function(response) {
            // This is when the row is initially opened.
            // Unlike JVMs, web server data is retrieved when the row is opened.

            if (response.applicationResponseContent !== undefined && response.applicationResponseContent !== null) {
                response.applicationResponseContent.forEach(function(o) {
                    o["parentItemId"] = idObj.parentId;
                });
            }

            responseCallback(response);

            // This will set the state which triggers DOM rendering thus the state will be updated
            // TODO: Find out if the code below is still necessary since removing it seems to have no effect whatsoever.
            self.props.updateWebServerDataCallback(response.applicationResponseContent);
        });
   },
   getApplicationsOfGrp: function(idObj, responseCallback) {
        // TODO: Verify if we need to display the applications on a group. If we need to, I think this needs fixing. For starters, we need to include the group id in the application response.
        webAppService.getWebAppsByGroup(idObj.parentId, responseCallback);
   },
   getApplicationsOfJvm: function(idObj, responseCallback) {

            webAppService.getWebAppsByJvm(idObj.parentId, function(data) {

                var webApps = data.applicationResponseContent;
                for (var i = 0; i < webApps.length; i++) {
                    if (idObj.rootId !== webApps[i].group.id.id) {
                        webApps[i]["className"] = "highlight";
                    } else {
                        webApps[i]["className"] = ""; // This is needed to prevent datatable from complaining
                                                      // for a missing "className" data since "className" is a defined
                                                      // filed in mData (please research for JQuery DataTable)
                    }
                }

                responseCallback(data);

            });

   },
   deploy: function(id) {
        alert("Deploy applications for group_" + id + "...");
   },
    enableButtonThunk: function(buttonSelector, iconClass) {
        return function() {
            $(buttonSelector).prop('disabled', false);
            if ($(buttonSelector + " span").hasClass("ui-icon")) {
                $(buttonSelector).attr("class",
                "ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only ui-button-height");
                $(buttonSelector).find("span").attr("class", "ui-icon " + iconClass);
            } else {
                $(buttonSelector).removeClass("ui-state-disabled");
            }
        };
    },
    disableButtonThunk: function(buttonSelector) {
        return function() {
            $(buttonSelector).prop('disabled', true);
            if ($(buttonSelector + " span").hasClass("ui-icon")) {
                $(buttonSelector).attr("class", "busy-button");
                $(buttonSelector).find("span").removeClass();
            } else {
                $(buttonSelector).addClass("ui-state-disabled");
            }
        };
    },
   enableHeapDumpButtonThunk: function(buttonSelector) {
       return function() {
            $(buttonSelector).prop('disabled', false);
            $(buttonSelector).attr("class",
                                   "ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only ui-button-height");
            $(buttonSelector).find("span").attr("class", "ui-icon ui-icon-heap-dump");
       };
   },
   disableHeapDumpButtonThunk: function(buttonSelector) {
       return function() {
           $(buttonSelector).prop('disabled', true);
           $(buttonSelector).attr("class", "busy-button");
           $(buttonSelector).find("span").removeClass();
       };
   },
   disableEnable: function(buttonSelector, func, iconClass) {
       var disable = this.disableButtonThunk(buttonSelector);
       var enable = this.enableButtonThunk(buttonSelector, iconClass);
       Promise.method(disable)().then(func).lastly(enable);
   },
   enableLinkThunk: function(linkSelector) {
        return function () {
            $(linkSelector).removeClass("disabled");
        };
    },
    disableLinkThunk: function(linkSelector) {
        return function () {
            $(linkSelector).addClass("disabled");
        };
    },
    disableEnableHeapDumpButton: function(selector, requestTask, requestCallbackTask, errHandler) {
        var disable = this.disableHeapDumpButtonThunk(selector);
        var enable = this.enableHeapDumpButtonThunk(selector);
        Promise.method(disable)().then(requestTask).then(requestCallbackTask).caught(errHandler).lastly(enable);
    },
   confirmStartStopGroupDialogBox: function(id, buttonSelector, msg, callbackOnConfirm) {
        var dialogId = "group-stop-confirm-dialog-" + id;
        $(buttonSelector).parent().append("<div id='" + dialogId +"' style='text-align:left'>" + msg + "</div>");
        $(buttonSelector).parent().find("#" + dialogId).dialog({
            title: "Confirmation",
            width: "auto",
            modal: true,
            buttons: {
                "Yes": function() {
                    callbackOnConfirm(id, buttonSelector);
                    $(this).dialog("close");
                },
                "No": function() {
                    $(this).dialog("close");
                }
            },
            open: function() {
                // Set focus to "No button"
                $(this).closest('.ui-dialog').find('.ui-dialog-buttonpane button:eq(1)').focus();
            }
        });
   },
    /**
     * Verifies and confirms to the user whether to continue the operation or not.
     * @param id the id (e.g. group id)
     * @param name the name (e.g. group name)
     * @param buttonSelector the jquery button selector
     * @param operation control operation namely "Start" and "Stop"
     * @param operationCallback operation to execute (e.g. startGroupCallback)
     * @param groupChildType a group's children to verify membership in other groups
     *                       (jvm - all JVMs, webServer - all web servers, undefined = jvms and web servers)
     */
    verifyAndConfirmControlOperation: function(id, buttonSelector, name, operation, operationCallback, groupChildType) {
        var self = this;
        groupService.getChildrenOtherGroupConnectionDetails(id, groupChildType).then(function(data) {
            if (data.applicationResponseContent instanceof Array && data.applicationResponseContent.length > 0) {
                var membershipDetails =
                    groupOperationsHelper.createMembershipDetailsHtmlRepresentation(data.applicationResponseContent);

                self.confirmStartStopGroupDialogBox(id,
                                               buttonSelector,
                                               membershipDetails
                                                    + "<br/><b>Are you sure you want to " + operation
                                                    + " <span style='color:#2a70d0'>" + name + "</span> ?</b>",
                                               operationCallback);
            } else {
                operationCallback(id, buttonSelector);
            }
        });
    },
    startGroupCallback: function(id, buttonSelector) {
        this.disableEnable(buttonSelector, function() {return groupControlService.startGroup(id)}, "ui-icon-play");
    },
    startGroup: function(id, buttonSelector, name) {
        this.verifyAndConfirmControlOperation(id, buttonSelector, name, "start", this.startGroupCallback);
    },
    stopGroupCallback: function(id, buttonSelector) {
        this.disableEnable(buttonSelector, function() {return groupControlService.stopGroup(id)}, "ui-icon-stop");
    },
    stopGroup: function(id, buttonSelector, name) {
        this.verifyAndConfirmControlOperation(id, buttonSelector, name, "stop", this.stopGroupCallback);
    },

    startGroupJvms: function(event) {
        var self = this;
        var callback = function(id, buttonSelector) {
                            self.disableEnable(event.data.buttonSelector,
                                               function() { return groupControlService.startJvms(event.data.id)},
                                               "ui-icon-play");
                       }

        this.verifyAndConfirmControlOperation(event.data.id,
                                              event.data.buttonSelector,
                                              event.data.name,
                                              "start all JVMs under",
                                              callback,
                                              "jvm");
    },

    stopGroupJvms: function(event) {
        var self = this;
        var callback = function(id, buttonSelector) {
                            self.disableEnable(event.data.buttonSelector,
                                               function() { return groupControlService.stopJvms(event.data.id)},
                                               "ui-icon-stop");
                       }

        this.verifyAndConfirmControlOperation(event.data.id,
                                              event.data.buttonSelector,
                                              event.data.name,
                                              "stop all JVMs under",
                                              callback,
                                              "jvm");
    },
    startGroupWebServers: function(event) {
        var self = this;
        var callback = function(id, buttonSelector) {
                            self.disableEnable(event.data.buttonSelector,
                                               function() { return groupControlService.startWebServers(event.data.id)},
                                               "ui-icon-play");
                       }

        this.verifyAndConfirmControlOperation(event.data.id,
                                              event.data.buttonSelector,
                                              event.data.name,
                                              "start all Web Servers under",
                                              callback,
                                              "webServer");
    },
    stopGroupWebServers: function(event) {
        var self = this;
        var callback = function(id, buttonSelector) {
                            self.disableEnable(event.data.buttonSelector,
                                               function() { return groupControlService.stopWebServers(event.data.id)},
                                               "ui-icon-stop");
                       }

        this.verifyAndConfirmControlOperation(event.data.id,
                                              event.data.buttonSelector,
                                              event.data.name,
                                              "stop all Web Servers under",
                                              callback,
                                              "webServer");
    },

   onClickHttpdConf: function(data) {
       var id = data.id.id;
       var url = "webServerCommand?webServerId=" + id + "&operation=viewHttpdConf";
       window.open(url)
   },
   jvmHeapDump: function(id, selector, host) {
       var requestHeapDump = function() {return jvmControlService.heapDump(id);};
       var heapDumpRequestCallback = function(response){
                                        var msg;
                                        if (response.applicationResponseContent.execData.standardError === "") {
                                            msg = response.applicationResponseContent.execData.standardOutput;
                                            msg = msg.replace("Dumping heap to", "Heap dump saved to " + host + " in ");
                                            msg = msg.replace("Heap dump file created", "");
                                        } else {
                                            msg = response.applicationResponseContent.execData.standardError;
                                        }
                                        $.alert(msg, "Heap Dump", false);
                                        $(selector).attr('title', "Last heap dump status: " + msg);
                                     };
       var heapDumpErrorHandler = function(e){
                                      var errCodeAndMsg;
                                      try {
                                          var errCode = JSON.parse(e.responseText).msgCode;
                                          var errMsg = JSON.parse(e.responseText).applicationResponseContent;
                                          errCodeAndMsg = "Error: " + errCode + (errMsg !== "" ? " - " : "") + errMsg;
                                      } catch(e) {
                                          errCodeAndMsg = e.responseText;
                                      }
                                      $.alert(errCodeAndMsg, "Heap Dump Error!", false);
                                      $(selector).attr('title', "Last heap dump status: " + errCodeAndMsg);
                                  };

       this.disableEnableHeapDumpButton(selector, requestHeapDump, heapDumpRequestCallback, heapDumpErrorHandler);
   },

    confirmJvmWebServerStopGroupDialogBox: function(id, parentItemId, buttonSelector, msg,callbackOnConfirm, cancelCallback) {
        var dialogId = "start-stop-confirm-dialog-for_group" + parentItemId + "_jvm" + id;
        $(buttonSelector).parent().append("<div id='" + dialogId +"' style='text-align:left'>" + msg + "</div>");
        $(buttonSelector).parent().find("#" + dialogId).dialog({
           title: "Confirmation",
           width: "auto",
           modal: true,
           buttons: {
               "Yes": function() {
                   callbackOnConfirm(id);
                   $(this).dialog("close");
               },
               "No": function() {
                   $(this).dialog("close");
                   cancelCallback();
               }
           },
           open: function() {
               // Set focus to "No button"
               $(this).closest('.ui-dialog').find('.ui-dialog-buttonpane button:eq(1)').focus();
           }
        });
    },
    verifyAndConfirmJvmWebServerControlOperation: function(id,
                                                           parentItemId,
                                                           buttonSelector,
                                                           name,
                                                           groups,
                                                           operation,
                                                           operationCallback,
                                                           cancelCallback,
                                                           serverType) {
        var msg = "<b>" + serverType + " <span style='color:#2a70d0'>" + name + "</span> is a member of:</b><br/>" +
                  groupOperationsHelper.groupArrayToHtmlList(groups, parentItemId)
                  + "<br/><b> Are you sure you want to " + operation + " <span style='color:#2a70d0'>" + name + "</span></b> ?";
        if (groups.length > 1) {
            this.confirmJvmWebServerStopGroupDialogBox(id,
                                                       parentItemId,
                                                       buttonSelector,
                                                       msg,
                                                       operationCallback,
                                                       cancelCallback);
        } else {
            operationCallback(id);
        }
    },
    jvmStart: function(id, buttonSelector, data, parentItemId, cancelCallback) {
        this.verifyAndConfirmJvmWebServerControlOperation(id,
                                                          parentItemId,
                                                          buttonSelector,
                                                          data.jvmName,
                                                          data.groups,
                                                          "start",
                                                          jvmControlService.startJvm,
                                                          cancelCallback,
                                                          "JVM");
    },
    jvmStop: function(id, buttonSelector, data, parentItemId, cancelCallback) {
        this.verifyAndConfirmJvmWebServerControlOperation(id,
                                                          parentItemId,
                                                          buttonSelector,
                                                          data.jvmName,
                                                          data.groups,
                                                          "stop",
                                                          jvmControlService.stopJvm,
                                                          cancelCallback,
                                                          "JVM");
    },
   buildHRef: function(data) {
        return  "idp?saml_redirectUrl=" +
                window.location.protocol + "//" +
                data.hostName + ":" +
                (window.location.protocol.toUpperCase() === "HTTPS:" ? data.httpsPort : data.httpPort) + "/manager/";
   },
    jvmDiagnose: function(id, buttonSelector, data, parentItemId, cancelCallback) {
        this.verifyAndConfirmJvmWebServerControlOperation(id,
                parentItemId,
                buttonSelector,
                data.jvmName,
                data.groups,
                "diganose",
                ServiceFactory.getJvmService().diagnoseJvm,
                cancelCallback,
                "JVM");
    },
    onClickMgr: function(unused1, unused2, data) {
        var url = "idp?saml_redirectUrl=" +
                  window.location.protocol + "//" +
                  data.hostName + ":" +
                  (window.location.protocol.toUpperCase() === "HTTPS:" ? data.httpsPort : data.httpPort) + "/manager/";
        window.open(url);
    },
    onClickHealthCheck: function(unused1, unused2, data) {
        var url = window.location.protocol + "//" +
                  data.hostName +
                  ":" +
                  (window.location.protocol.toUpperCase() === "HTTPS:" ? data.httpsPort : data.httpPort) +
                  tocVars.healthCheckApp;
        window.open(url)
    },
    onClickThreadDump: function(id, unused1) {
        var url = "jvmCommand?jvmId=" + id + "&operation=threadDump";
        window.open(url)
    },

    /* web server callbacks */
    buildHRefLoadBalancerConfig: function(data) {
        return "https://" + data.host + ":" + data.httpsPort + tocVars.loadBalancerStatusMount;
    },
    webServerStart: function(id, buttonSelector, data, parentItemId, cancelCallback) {
        this.verifyAndConfirmJvmWebServerControlOperation(id,
                                                          parentItemId,
                                                          buttonSelector,
                                                          data.name,
                                                          data.groups,
                                                          "start",
                                                          webServerControlService.startWebServer,
                                                          cancelCallback,
                                                          "Web Server");
    },
    webServerStop: function(id, buttonSelector, data, parentItemId, cancelCallback) {
        this.verifyAndConfirmJvmWebServerControlOperation(id,
                                                          parentItemId,
                                                          buttonSelector,
                                                          data.name,
                                                          data.groups,
                                                          "stop",
                                                          webServerControlService.stopWebServer,
                                                          cancelCallback,
                                                          "Web Server");
    },
    getStateForGroup: function(mData, type, fullData) {
        var groupId = fullData.id.id;

        if (this.groupsById !== undefined && this.groupsById[groupId] !== undefined) {
            if (this.groupsById[groupId].state !== undefined) {
                $(".group-state-" + groupId).html(this.groupsById[groupId].state.stateString);
            } else  {
                $(".group-state-" + groupId).html("UNKNOWN");
            }
        }

        return "<span class='group-state-" + groupId + "'/>"
    }
});

/**
 * Displays the state and an error indicator if there are any errors in the state.
 */
var StatusWidget = React.createClass({
    getInitialState: function() {
        return {status:this.props.defaultStatus, errorMessages:[], showErrorBtn:false, newErrorMsg:false};
    },
    render: function() {
        var errorBtn = null;
        if (this.state.showErrorBtn) {
           errorBtn = <FlashingButton className="ui-button-height ui-alert-border ui-state-error error-indicator-button"
                                      spanClassName="ui-icon ui-icon-alert"
                                      flashing={this.state.newErrorMsg.toString()}
                                      flashClass="flash"
                                      callback={this.showErrorMsgCallback}/>
        }
        return <div className="status-widget-container">
                   <div ref="errorDlg" style={{display:"inline-block"}}/>
                   <span className="status-label">{this.state.status}</span>
                   {errorBtn}
               </div>;
    },
    setStatus: function(newStatus, dateTime, errorMsg) {
        var newState = {status:newStatus};
        if (errorMsg !== "") {
            newState["newErrorMsg"] = true;
            newState["showErrorBtn"] = true;
            var errMsg = groupOperationsHelper.splitErrorMsgIntoShortMsgAndStackTrace(errorMsg);
            this.state.errorMessages.push({dateTime:groupOperationsHelper.getCurrentDateTime(dateTime),
                                           msg:errMsg[0],
                                           pullDown:errMsg[1]});
        } else {
            newState["newErrorMsg"] = false;
            newState["showErrorBtn"] = false;
        }
        this.setState(newState);
    },
    showErrorMsgCallback: function() {
        this.setState({newErrorMsg:false});
        React.render(<DialogBox title={this.props.errorMsgDlgTitle}
                                contentDivClassName="maxHeight400px"
                                content={<ErrorMsgList msgList={this.state.errorMessages}/>} />,
                     this.refs.errorDlg.getDOMNode());
    }
});