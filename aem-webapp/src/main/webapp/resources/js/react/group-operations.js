
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
                                                              jvmsById={groupOperationsHelper.keyJvmsById(this.state.jvms)}
                                                              updateWebServerDataCallback={this.updateWebServerDataCallback}/>
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
        this.setState(groupOperationsHelper.processGroupData(this.state.groups,
                                                             [],
                                                             this.state.groupStates,
                                                             newGroupStates));
        var groupsToUpdate = groupOperationsHelper.getGroupStatesById(this.state.groups);
        groupOperationsHelper.updateGroupsInDataTables(this.state.groups);
    },

    updateWebServerStateData: function(newWebServerStates) {
        this.setState(groupOperationsHelper.processWebServerData(this.state.webServers,
                                                                 [],
                                                                 this.state.webServerStates,
                                                                 newWebServerStates));

        var webServersToUpdate = groupOperationsHelper.getWebServerStatesByGroupIdAndWebServerId(this.state.webServers);
        webServersToUpdate.forEach(
        function(webServer) {
            groupOperationsHelper.updateWebServersInDataTables(webServer.groupId.id, webServer.webServerId.id, webServer.stateString);
        });
    },


    updateJvmStateData: function(newJvmStates) {
        this.setState(groupOperationsHelper.processJvmData(this.state.jvms,
                                                           [],
                                                           this.state.jvmStates,
                                                           newJvmStates));

        var jvmsToUpdate = groupOperationsHelper.getJvmStatesByGroupIdAndJvmId(this.state.jvms);
        jvmsToUpdate.forEach(function(jvm) {groupOperationsHelper.updateDataTables(jvm.groupId.id, jvm.jvmId.id, jvm.stateString);});
    },

    pollStates: function() {
        var self = this;
        this.dataSink = this.props.stateService.createDataSink(function(data) {
                                                                                    self.updateStateData(data);
                                                                              });
        this.props.stateService.pollForUpdates(this.props.statePollTimeout, this.dataSink);
    },

    fetchCurrentJvmStates: function() {
        var self = this;
        this.props.stateService.getCurrentJvmStates().then(function(data) { self.updateJvmStateData(data.applicationResponseContent);})
                                                     .caught(function(e) {});
    },
    fetchCurrentWebServerStates: function() {
        var self = this;
        this.props.stateService.getCurrentWebServerStates().then(function(data) { self.updateWebServerStateData(data.applicationResponseContent);})
                                                           .caught(function(e) {});
    },
    fetchCurrentGroupStates: function() {
        var self = this;
        this.props.stateService.getCurrentGroupStates().then(function(data) { self.updateGroupsStateData(data.applicationResponseContent);})
                                                       .caught(function(e) {});
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
        this.fetchCurrentJvmStates();
        this.fetchCurrentWebServerStates();
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
    }
});

var GroupOperationsDataTable = React.createClass({
   jvmStateErrorMessages: [],
   jvmHasNewMessage: [],
   webServerStateErrorMessages: [],
   webServerHasNewMessage: [],
   shouldComponentUpdate: function(nextProps, nextState) {

       // TODO: Set status here
       this.groupsById = groupOperationsHelper.keyGroupsById(nextProps.groups);
       this.webServersById = groupOperationsHelper.keyWebServersById(nextProps.webServers);
       this.jvmsById = groupOperationsHelper.keyJvmsById(nextProps.jvms);

       this.hasNoData = (this.props.data.length === 0);

       // NOTE: This prevents the grid from re rendering every time there's a state change.
       // We need to prevent re rendering for now since we are using Datatable to
       // update the DOM instead of REACT.
       if (!this.hasDrawn) {
           this.hasDrawn = true;
           return true;
       }

       return this.hasNoData;
    },
    render: function() {
        var groupTableDef = [{sTitle:"", mData: "jvms", tocType:"control", colWidth:"10px"},
                             {sTitle:"Group ID", mData:"id.id", bVisible:false},
                             {sTitle:"Group Name", mData:"name", colWidth:"650px"},
                             [{sTitle:"",
                               mData:null,
                               tocType:"button",
                               btnLabel:"Start Group",
                               btnCallback:this.startGroup,
                               className:"inline-block",
                               extraDataToPassOnCallback:"name",
                               colWidth:70},
                              {sTitle:"",
                               mData:null,
                               tocType:"button",
                               btnLabel:"Stop Group",
                               btnCallback:this.stopGroup,
                               className:"inline-block",
                               extraDataToPassOnCallback:"name",
                               colWidth:70}],
                              {sTitle:"State",
                               mData:null,
                               mRender: this.getStateForGroup,
                               colWidth:"94px"}];

        var webServerOfGrpChildTableDef = [{sTitle:"Web Server ID", mData:"id.id", bVisible:false},
                                           {mData:null, colWidth:"10px"},
                                           {sTitle:"Name", mData:"name", colWidth:"240px"},
                                           {sTitle:"Host", mData:"host", colWidth:"240px"},
                                           {sTitle:"HTTP", mData:"port", colWidth:"40px"},
                                           {sTitle:"HTTPS", mData:"httpsPort", colWidth:"40px"},
                                           {sTitle:"Status Path", mData:"statusPath.uriPath", colWidth:"60px"},
                                           {sTitle:"Group",
                                            mData:"groups",
                                            tocType:"array",
                                            displayProperty:"name",
                                            maxDisplayTextLen:20,
                                            colWidth:"138px"},
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
                                             clickedStateClassName:"busy-button",
                                             isBusyCallback:this.isWebServerStartStopInProcess,
                                             buttonClassName:"ui-button-height",
                                             busyStatusTimeout:tocVars.startStopTimeout,
                                             extraDataToPassOnCallback:["name","groups"],
                                             expectedState:"STARTED"},
                                            {tocType:"space"},
                                            {id:"stopWebServer",
                                             sTitle:"Stop",
                                             mData:null,
                                             tocType:"button",
                                             btnLabel:"",
                                             btnCallback:this.webServerStop,
                                             className:"inline-block",
                                             customSpanClassName:"ui-icon ui-icon-stop",
                                             clickedStateClassName:"busy-button",
                                             isBusyCallback:this.isWebServerStartStopInProcess,
                                             buttonClassName:"ui-button-height",
                                             busyStatusTimeout:tocVars.startStopTimeout,
                                             extraDataToPassOnCallback:["name","groups"],
                                             expectedState:"STOPPED"}],
                                           {sTitle:"State",
                                            mData:null,
                                            mRender: this.getStateForWebServer,
                                            colWidth:"105px"}];

        var webServerOfGrpChildTableDetails = {tableIdPrefix:"ws-child-table_",
                                               className:"simple-data-table",
                                               dataCallback:this.getWebServersOfGrp,
                                               title:"Web Servers",
                                               isCollapsible:true,
                                               headerComponents:[
                                                    {id:"startWebServers", tocType:"button", btnLabel:"Start Web Servers", btnCallback: this.startGroupWebServers},
                                                    {id:"stopWebServers", tocType:"button", btnLabel:"Stop Web Servers", btnCallback: this.stopGroupWebServers},
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
                                         {id:"startJvms", tocType:"button", btnLabel:"Start JVMs", btnCallback: this.startGroupJvms},
                                         {id:"stopJvms", tocType:"button", btnLabel:"Stop JVMs", btnCallback: this.stopGroupJvms},
                                         {tocType:"label", className:"inline-block header-component-label", text:""}
                                    ],
                                    initialSortColumn: [[2, "asc"]]};

        var jvmChildTableDef = [/* {sTitle:"", mData:null, tocType:"control"}, !!! Disable for the Aug 11, 2014 Demo */
                                {mData:null, colWidth:"10px"},
                                {sTitle:"JVM ID", mData:"id.id", bVisible:false},
                                {sTitle:"Name", mData:"jvmName", colWidth:"270px"},
                                {sTitle:"Host", mData:"hostName", colWidth:"270px"},
                                {sTitle:"HTTP", mData:"httpPort", colWidth:"40px"},
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
                                  clickedStateClassName:"busy-button",
                                  isBusyCallback:this.isJvmStartStopInProcess,
                                  buttonClassName:"ui-button-height",
                                  busyStatusTimeout:tocVars.startStopTimeout,
                                  extraDataToPassOnCallback:["jvmName","groups"],
                                  expectedState:"STARTED"},
                                 {tocType:"space"},
                                 {id:"stopJvm",
                                  sTitle:"Stop",
                                  mData:null,
                                  tocType:"button",
                                  btnLabel:"",
                                  btnCallback:this.jvmStop,
                                  className:"inline-block",
                                  customSpanClassName:"ui-icon ui-icon-stop",
                                  clickedStateClassName:"busy-button",
                                  isBusyCallback:this.isJvmStartStopInProcess,
                                  buttonClassName:"ui-button-height",
                                  busyStatusTimeout:tocVars.startStopTimeout,
                                  extraDataToPassOnCallback:["jvmName","groups"],
                                  expectedState:"STOPPED"}],
                                {sTitle:"State",
                                 mData:null,
                                 mRender: this.getStateForJvm,
                                 colWidth:"105px"}];

        jvmChildTableDetails["tableDef"] = jvmChildTableDef;

        var childTableDetailsArray = [webServerOfGrpChildTableDetails,
                                      jvmChildTableDetails,
                                      webAppOfGrpChildTableDetails];

        return <TocDataTable tableId="group-operations-table"
                             className="dataTable operationGroupTable"
                             tableDef={groupTableDef}
                             data={this.props.data}
                             expandIcon="public-resources/img/react/components/details-expand.png"
                             collapseIcon="public-resources/img/react/components/details-collapse.png"
                             rowSubComponentContainerClassName="row-sub-component-container"
                             childTableDetails={childTableDetailsArray}
                             selectItemCallback={this.props.selectItemCallback}
                             initialSortColumn={[[2, "asc"]]}/>
   },
   isWebServerStartStopInProcess: function(id, expectedState) {
        if (this.webServersById[id] !== undefined) {
            return (expectedState !== this.webServersById[id].state.stateString);
        }
        return true;
   },
   isJvmStartStopInProcess: function(id, expectedState) {
        return (expectedState !== this.jvmsById[id].state.stateString);
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

            self.webServersById = groupOperationsHelper.keyWebServersById(response.applicationResponseContent);
            responseCallback(response);

            // This will set the state and which triggers DOM rendering thus the state will be updated
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
    enableButtonThunk: function(buttonSelector) {
        return function() {
            $(buttonSelector).button("enable");
        };
    },
    disableButtonThunk: function(buttonSelector) {
        return function() {
            $(buttonSelector).button("disable");
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
   disableEnable: function(buttonSelector, func) {
       var disable = this.disableButtonThunk(buttonSelector);
       var enable = this.enableButtonThunk(buttonSelector);
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
        $(buttonSelector).parent().append("<div id='" + dialogId +"'>" + msg + "</div>");
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
                self.confirmStartStopGroupDialogBox(id,
                                               buttonSelector,
                                               data.applicationResponseContent.join("<br/>")
                                                    + "<br/><br/>Are you sure you want to " + operation + " " + name + " ?",
                                               operationCallback);
            } else {
                operationCallback(id, buttonSelector);
            }
        });
    },
    startGroupCallback: function(id, buttonSelector) {
        this.disableEnable(buttonSelector, function() {return groupControlService.startGroup(id)});
    },
    startGroup: function(id, buttonSelector, name) {
        this.verifyAndConfirmControlOperation(id, buttonSelector, name, "start", this.startGroupCallback);
    },
    stopGroupCallback: function(id, buttonSelector) {
        this.disableEnable(buttonSelector, function() {return groupControlService.stopGroup(id)});
    },
    stopGroup: function(id, buttonSelector, name) {
        this.verifyAndConfirmControlOperation(id, buttonSelector, name, "stop", this.stopGroupCallback);
    },

    startGroupJvms: function(event) {
        var self = this;
        var callback = function(id, buttonSelector) {
                            self.disableEnable(event.data.buttonSelector,
                                               function() { return groupControlService.startJvms(event.data.id)});
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
                                               function() { return groupControlService.stopJvms(event.data.id)});
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
                                               function() { return groupControlService.startWebServers(event.data.id)});
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
                                               function() { return groupControlService.stopWebServers(event.data.id)});
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
        $(buttonSelector).parent().append("<div id='" + dialogId +"'>" + msg + "</div>");
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
                                                           cancelCallback) {
        var msg = name + " is a member of " +
                  groupOperationsHelper.groupArrayToString(groups, parentItemId)
                  + "<br/><br/> Are you sure you want to " + operation + " " + name + " ?";
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
                                                          cancelCallback);
    },
    jvmStop: function(id, buttonSelector, data, parentItemId, cancelCallback) {
        this.verifyAndConfirmJvmWebServerControlOperation(id,
                                                          parentItemId,
                                                          buttonSelector,
                                                          data.jvmName,
                                                          data.groups,
                                                          "stop",
                                                          jvmControlService.stopJvm,
                                                          cancelCallback);
    },
   buildHRef: function(data) {
        return  "idp?saml_redirectUrl=" +
                window.location.protocol + "//" +
                data.hostName + ":" +
                (window.location.protocol.toUpperCase() === "HTTPS:" ? data.httpsPort : data.httpPort) + "/manager/";
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
    jvmErrorAlertCallback: function(alertDlgDivId, jvm) {
        this.jvmHasNewMessage[jvm.id.id] = "false";
        React.unmountComponentAtNode(document.getElementById(alertDlgDivId));
        React.renderComponent(<DialogBox title={jvm.jvmName + " State Error Messages"}
                                         contentDivClassName="maxHeight400px"
                                         content={<ErrorMsgList msgList={this.jvmStateErrorMessages[jvm.id.id]}/>} />,
                                         document.getElementById(alertDlgDivId));

    },
    getStateForJvm: function(mData, type, fullData) {
        var jvmId = fullData.id.id;
        var jvmToRender = this.jvmsById[jvmId];

        var colComponentClassName = "jvm" + jvmId + "-grp" + fullData.parentItemId + "-state";

        if (jvmToRender.state !== undefined) {

            if (jvmToRender.state.message !== undefined && jvmToRender.state.message !== "") {

                if (this.jvmStateErrorMessages[jvmId] === undefined) {
                    this.jvmStateErrorMessages[jvmId] = [];
                }

                var msgs = groupOperationsHelper.splitErrorMsgIntoShortMsgAndStackTrace(jvmToRender.state.message);
                if (!groupOperationsHelper.lastItemEquals(this.jvmStateErrorMessages[jvmId],
                                                          "msg",
                                                          msgs[0])) {

                    this.jvmStateErrorMessages[jvmId].push({dateTime:groupOperationsHelper.getCurrentDateTime(jvmToRender.state.asOf),
                                                            msg:msgs[0],
                                                            pullDown:msgs[1]});
                    this.jvmHasNewMessage[jvmId] = "true";
                }

                if (this.jvmStateErrorMessages[jvmId].length > 0) {

                    var alertBtnDivId = "alert-btn-div-jvm" + jvmId + "-grp" + fullData.parentItemId;
                    var alertDlgDivId = "alert-dlg-div-jvm" + jvmId + "-grp" + fullData.parentItemId;

                    if ($("." + colComponentClassName).parent().find("#" + alertBtnDivId).size() === 0) {
                        $("." + colComponentClassName).parent().html("<div class='" + colComponentClassName + " state' />" +
                                                                      "<div id='" + alertBtnDivId + "' class='inline-block'/>" +
                                                                      "<div id='" + alertDlgDivId + "'>");
                        $("." + colComponentClassName).html(jvmToRender.state.stateString);
                    }

                    if (document.getElementById(alertBtnDivId) !== null) {
                        var flashing = this.jvmHasNewMessage[jvmId] !== undefined ? this.jvmHasNewMessage[jvmId] : "true";
                        React.renderComponent(<FlashingButton className="ui-button-height ui-alert-border ui-state-error"
                                                              spanClassName="ui-icon ui-icon-alert"
                                                              callback={this.jvmErrorAlertCallback.bind(this, alertDlgDivId, jvmToRender)}
                                                              flashing={flashing}
                                                              flashClass="flash"/>, document.getElementById(alertBtnDivId));
                    }
                }

            } else {
                    this.jvmStateErrorMessages[jvmId] = [];
                    React.unmountComponentAtNode(document.getElementById(alertDlgDivId));
                    React.unmountComponentAtNode(document.getElementById(alertBtnDivId));
                    $("." + colComponentClassName).parent().html("<div class='" + colComponentClassName + " state' />");
                    $("." + colComponentClassName).html(jvmToRender.state.stateString);
            }

        } else {
            $("." + colComponentClassName).html("UNKNOWN");
        }

        return "<div class='" + colComponentClassName + "'/>"

    },
    /* web server callbacks */
    buildHRefLoadBalancerConfig: function(data) {
        return "http://" + data.host + ":" + data.port + tocVars.loadBalancerStatusMount;
    },
    webServerStart: function(id, buttonSelector, data, parentItemId, cancelCallback) {
        this.verifyAndConfirmJvmWebServerControlOperation(id,
                                                          parentItemId,
                                                          buttonSelector,
                                                          data.name,
                                                          data.groups,
                                                          "start",
                                                          webServerControlService.startWebServer,
                                                          cancelCallback);
    },
    webServerStop: function(id, buttonSelector, data, parentItemId, cancelCallback) {
        this.verifyAndConfirmJvmWebServerControlOperation(id,
                                                          parentItemId,
                                                          buttonSelector,
                                                          data.name,
                                                          data.groups,
                                                          "stop",
                                                          webServerControlService.stopWebServer,
                                                          cancelCallback);
    },
    webServerErrorAlertCallback: function(alertDlgDivId, ws) {
        this.webServerHasNewMessage[ws.id.id] = "false";
        React.unmountComponentAtNode(document.getElementById(alertDlgDivId));
        React.renderComponent(<DialogBox title={ws.name + " State Error Messages"}
                                         contentDivClassName="maxHeight400px"
                                         content={<ErrorMsgList msgList={this.webServerStateErrorMessages[ws.id.id]}/>} />,
                                         document.getElementById(alertDlgDivId));
    },
    /**
     * This method is responsible for displaying the state in the grid
     *
     */
    getStateForWebServer: function(mData, type, fullData, parentItemId) {
        var webServerId = fullData.id.id;
        var webServerToRender = this.webServersById[webServerId];

        var colComponentClassName = "ws" + webServerId + "-grp" + fullData.parentItemId + "-state";

        if (this.webServersById !== undefined && webServerToRender !== undefined) {
            if (webServerToRender.state !== undefined) {

                if (webServerToRender.state.message !== undefined && webServerToRender.state.message !== "") {

                    if (this.webServerStateErrorMessages[webServerId] === undefined) {
                        this.webServerStateErrorMessages[webServerId] = [];
                    }

                    var msgs = groupOperationsHelper.splitErrorMsgIntoShortMsgAndStackTrace(webServerToRender.state.message);
                    if (!groupOperationsHelper.lastItemEquals(this.webServerStateErrorMessages[webServerId],
                                                              "msg",
                                                              msgs[0])) {
                        this.webServerStateErrorMessages[webServerId].push({dateTime:groupOperationsHelper.getCurrentDateTime(webServerToRender.state.asOf),
                                                                            msg:msgs[0],
                                                                            pullDown:msgs[1]});
                        this.webServerHasNewMessage[webServerId] = "true";
                    }

                    if (this.webServerStateErrorMessages[webServerId].length > 0) {
                        var self = this;
                        var alertBtnDivId = "alert-btn-div-ws" + webServerId + "-grp" + fullData.parentItemId;
                        var alertDlgDivId = "alert-dlg-div-ws" + webServerId + "-grp" + fullData.parentItemId;

                        if ($("." + colComponentClassName).parent().find("#" + alertBtnDivId).size() === 0) {
                            $("." + colComponentClassName).parent().html("<div class='" + colComponentClassName + " state' />" +
                                                                         "<div id='" + alertBtnDivId + "' class='inline-block'/>" +
                                                                         "<div id='" + alertDlgDivId + "'>");
                            $("." + colComponentClassName).html(webServerToRender.state.stateString);
                        }

                        var flashing = this.webServerHasNewMessage[webServerId] !== undefined ? this.webServerHasNewMessage[webServerId] : "true";
                        React.renderComponent(<FlashingButton className="ui-button-height ui-alert-border ui-state-error"
                                                              spanClassName="ui-icon ui-icon-alert"
                                                              callback={self.webServerErrorAlertCallback.bind(this, alertDlgDivId, webServerToRender)}
                                                              flashing={flashing}
                                                              flashClass="flash"/>, document.getElementById(alertBtnDivId));
                    }

                } else {
                    this.webServerStateErrorMessages[webServerId] = [];
                    React.unmountComponentAtNode(document.getElementById(alertDlgDivId));
                    React.unmountComponentAtNode(document.getElementById(alertBtnDivId));
                    $("." + colComponentClassName).parent().html("<div class='" + colComponentClassName + " state' />");
                    $("." + colComponentClassName).html(webServerToRender.state.stateString);
                }

            } else {
                $("." + colComponentClassName).html("UNKNOWN");
            }
        }

        return "<div class='" + colComponentClassName + "'/>"
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