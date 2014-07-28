
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
                    <table>
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
        groupOperationsHelper.updateGroupsInDataTables();
    },

    updateWebServerStateData: function(newWebServerStates) {
        this.setState(groupOperationsHelper.processWebServerData(this.state.webServers,
                                                                 [],
                                                                 this.state.webServerStates,
                                                                 newWebServerStates));

        var webServersToUpdate = groupOperationsHelper.getWebServerStatesByGroupIdAndWebServerId(this.state.webServers);
        webServersToUpdate.forEach(
            function(webServer) {
                groupOperationsHelper.updateWebServersInDataTables(webServer.groupId.id, webServer.webServerId.id, webServer.state);
            });
    },


    updateJvmStateData: function(newJvmStates) {
        this.setState(groupOperationsHelper.processJvmData(this.state.jvms,
                                                           [],
                                                           this.state.jvmStates,
                                                           newJvmStates));

        var jvmsToUpdate = groupOperationsHelper.getJvmStatesByGroupIdAndJvmId(this.state.jvms);
        jvmsToUpdate.forEach(function(jvm) {groupOperationsHelper.updateDataTables(jvm.groupId.id, jvm.jvmId.id, jvm.jvmState);});
    },

    pollStates: function() {
        var self = this;
        this.dataSink = this.props.stateService.createDataSink(function(data) {
                                                                                    self.updateStateData(data);
                                                                              });
        // TODO: Change this.props.jvmStateTimeout to this.props.stateTimeout
        this.props.stateService.pollForUpdates(this.props.jvmStateTimeout, this.dataSink);
    },

    fetchCurrentJvmStates: function() {
        var self = this;
        this.props.stateService.getCurrentJvmStates().then(function(data) { self.updateJvmStateData(data.applicationResponseContent);})
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
        this.fetchCurrentJvmStates();
    },
    componentWillUnmount: function() {
        this.dataSink.stop();
    },
    updateWebServerDataCallback: function(webServerData) {
        this.setState({webServers: webServerData});
    }
});

var GroupOperationsDataTable = React.createClass({
   shouldComponentUpdate: function(nextProps, nextState) {

       // TODO: Set status here
       this.groupsById = groupOperationsHelper.keyGroupsById(nextProps.groups);
       this.webServersById = groupOperationsHelper.keyWebServersById(nextProps.webServers);
       this.jvmsById = groupOperationsHelper.keyJvmsById(nextProps.jvms);
       this.hasNoData = (this.props.data.length === 0);
       if (!this.hasDrawn) {
           this.hasDrawn = true;
           return true;
       }

       return this.hasNoData;
    },
    render: function() {
        var groupTableDef = [{sTitle:"", mData: "jvms", tocType:"control"},
                             {sTitle:"Group ID", mData:"id.id", bVisible:false},
                             {sTitle:"Group Name", mData:"name"},
                             [{sTitle:"",
                               mData:null,
                               tocType:"button",
                               btnLabel:"Start Group",
                               btnCallback:this.startGroup,
                               className:"inline-block"},
                              {sTitle:"",
                               mData:null,
                               tocType:"button",
                               btnLabel:"Stop Group",
                               btnCallback:this.stopGroup,
                               className:"inline-block"}],
                              {sTitle:"State",
                               mData:null,
                               mRender: this.getStateForGroup}];

        var webServerOfGrpChildTableDef = [{sTitle:"Web Server ID", mData:"id.id", bVisible:false},
                                           {sTitle:"Name", mData:"name"},
                                           {sTitle:"Host", mData:"host"},
                                           {sTitle:"HTTP", mData:"port"},
                                           {sTitle:"HTTPS", mData:"httpsPort"},
                                           {sTitle:"Group",
                                            mData:"groups",
                                            tocType:"array",
                                            displayProperty:"name"},
                                           {sTitle:"",
                                            mData:null,
                                            tocType:"link",
                                            linkLabel:"Load Balancer",
                                            hRefCallback:this.buildHRefLoadBalancerConfig},
                                           [{sTitle:"",
                                             mData:null,
                                             tocType:"button",
                                             btnLabel:"",
                                             btnCallback:this.webServerStart,
                                             className:"inline-block",
                                             customBtnClassName:"start-button",
                                             clickedStateClassName:"start-button-busy"},
                                            {tocType:"space"},
                                            {tocType:"space"},
                                            {tocType:"space"},
                                            {sTitle:"",
                                             mData:null,
                                             tocType:"button",
                                             btnLabel:"",
                                             btnCallback:this.webServerStop,
                                             className:"inline-block",
                                             customBtnClassName:"stop-button",
                                             clickedStateClassName:"stop-button-busy"}],
                                           {sTitle:"State",
                                            mData:null,
                                            mRender: this.getStateForWebServer}];

        var webServerOfGrpChildTableDetails = {tableIdPrefix:"group-operations-web-server-child-table",
                                               className:"simple-data-table",
                                               dataCallback:this.getWebServersOfGrp,
                                               title:"Web Servers",
                                               isCollapsible:true,
                                               headerComponents:[
                                                    {id:"startWebServers", tocType:"button", btnLabel:"Start", btnCallback: this.startGroupWebServers},
                                                    {id:"stopWebServers", tocType:"button", btnLabel:"Stop", btnCallback: this.stopGroupWebServers}
                                               ]};

        webServerOfGrpChildTableDetails["tableDef"] = webServerOfGrpChildTableDef;

        var webAppOfGrpChildTableDetails = {tableIdPrefix:"group-operations-web-app-child-table",
                                            className:"simple-data-table",
                                            dataCallback:this.getApplicationsOfGrp,
                                            title:"Web Applications",
                                            isCollapsible:true};

        var webAppOfGrpChildTableDef = [{sTitle:"Web App ID", mData:"id.id", bVisible:false},
                                        {sTitle:"Name", mData:"name"},
                                        {sTitle:"War Path", mData:"warPath", tocType:"custom", tocRenderCfgFn: this.renderWebAppRowData},
                                        {sTitle:"Context", mData:"webAppContext"},
                                        {sTitle:"",
                                         mData:null,
                                         tocType:"button",
                                         btnLabel:"Undeploy",
                                         btnCallback:this.undeploy}];

        webAppOfGrpChildTableDetails["tableDef"] = webAppOfGrpChildTableDef;

        var webAppOfJvmChildTableDetails = {tableIdPrefix:"group-operations-web-app-of-jvm-child-table",
                                                    className:"simple-data-table",
                                                    dataCallback:this.getApplicationsOfJvm,
                                                    defaultSorting: {col:5, sort:"asc"}};

        var webAppOfJvmChildTableDef = [{sTitle:"Web App ID", mData:"id.id", bVisible:false},
                                        {sTitle:"Web App in JVM", mData:"name"},
                                        {sTitle:"War Path", mData:"warPath"},
                                        {sTitle:"Context", mData:"webAppContext"},
                                        {sTitle:"Group", mData:"group.name"},
                                        {sTitle:"Class Name", mData:"className", bVisible:false},
                                        {sTitle:"",
                                         mData:null,
                                         tocType:"button",
                                         btnLabel:"Undeploy",
                                         btnCallback:this.undeploy}];

        webAppOfJvmChildTableDetails["tableDef"] = webAppOfJvmChildTableDef;

        var jvmChildTableDetails = {tableIdPrefix:"group-operations-jvm-child-table",
                                    className:"simple-data-table",
                                    childTableDetails:webAppOfJvmChildTableDetails,
                                    title:"JVMs",
                                    isCollapsible:true,
                                    headerComponents:[
                                         {id:"startJvms", tocType:"button", btnLabel:"Start", btnCallback: this.startGroupJvms},
                                         {id:"stopJvms", tocType:"button", btnLabel:"Stop", btnCallback: this.stopGroupJvms}
                                    ]};

        var jvmChildTableDef = [{sTitle:"", mData:null, tocType:"control"},
                                {sTitle:"JVM ID", mData:"id.id", bVisible:false},
                                {sTitle:"Name", mData:"jvmName"},
                                {sTitle:"Host", mData:"hostName"},
                                {sTitle:"Group",
                                 mData:"groups",
                                 tocType:"array",
                                     displayProperty:"name"},
                                {sTitle:"",
                                 mData:null,
                                 tocType:"link",
                                 linkLabel:"Manager",
                                 hRefCallback:this.buildHRef},
                                {sTitle:"",
                                 mData:null,
                                 tocType:"link",
                                 linkLabel:"Heap Dump",
                                 onClickCallback:this.jvmHeapDump},
                                {sTitle:"",
                                 mData:null,
                                 tocType:"link",
                                 linkLabel:"Thread Dump",
                                 onClickCallback:this.onClickThreadDump},
                                [{sTitle:"",
                                  mData:null,
                                  tocType:"button",
                                  btnLabel:"",
                                  btnCallback:this.jvmStart,
                                  className:"inline-block",
                                  customBtnClassName:"start-button",
                                  clickedStateClassName:"start-button-busy"},
                                 {tocType:"space"},
                                 {tocType:"space"},
                                 {tocType:"space"},
                                 {sTitle:"",
                                  mData:null,
                                  tocType:"button",
                                  btnLabel:"",
                                  btnCallback:this.jvmStop,
                                  className:"inline-block",
                                  customBtnClassName:"stop-button",
                                  clickedStateClassName:"stop-button-busy"}],
                                {sTitle:"State",
                                 mData:null,
                                 mRender: this.getStateForJvm}];

        jvmChildTableDetails["tableDef"] = jvmChildTableDef;

        var childTableDetailsArray = [webServerOfGrpChildTableDetails,
                                      jvmChildTableDetails,
                                      webAppOfGrpChildTableDetails];

        return <TocDataTable tableId="group-operations-table"
                             tableDef={groupTableDef}
                             data={this.props.data}
                             expandIcon="public-resources/img/react/components/details-expand.png"
                             collapseIcon="public-resources/img/react/components/details-collapse.png"
                             rowSubComponentContainerClassName="row-sub-component-container"
                             childTableDetails={childTableDetailsArray}
                             selectItemCallback={this.props.selectItemCallback}/>
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
            self.webServersById = groupOperationsHelper.keyWebServersById(response.applicationResponseContent);

            responseCallback(response);

            // This will set the state and which triggers DOM rendering thus the state will be updated
            self.props.updateWebServerDataCallback(response.applicationResponseContent);
        });
   },
   getApplicationsOfGrp: function(idObj, responseCallback) {
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
   undeploy: function(id) {
        alert("Undeploy applications for group_" + id + "...");
   },
   startGroup: function(id) {
        groupControlService.startGroup(id);
   },
   stopGroup: function(id) {
        groupControlService.stopGroup(id);
   },
   startGroupJvms: function(event) {
       groupControlService.startJvms(event.data.id);
  },
  stopGroupJvms: function(event) {
       groupControlService.stopJvms(event.data.id);
  },
  startGroupWebServers: function(event) {
      groupControlService.startWebServers(event.data.id);
 },
 stopGroupWebServers: function(event) {
      groupControlService.stopWebServers(event.data.id);
 },
   jvmManager: function(id) {
        alert("JVM show manager for jvm_" + id + "...");
   },
   jvmHeapDump: function(id) {
        alert("JVM show heap dump for jvm_" + id + "...");
   },
   jvmStart: function(id) {
        jvmControlService.startJvm(id);
        return true; // TODO Once status can be retrieved, return true if JVM was successfully started
   },
   jvmStop: function(id) {
        jvmControlService.stopJvm(id);
        return true; // TODO Once status can be retrieved, return true if JVM was successfully stopped
   },
   buildHRef: function(data) {
        return  "idp?saml_redirectUrl=" +
                window.location.protocol + "//" +
                data.hostName + ":" + data.httpPort + "/manager/";
   },
    onClickThreadDump: function(data) {
        var redirectUrl = window.location.protocol + "//" +
                          data.hostName + ":" + data.httpPort +
                          "/manager/jmxproxy/?invoke=java.lang:type=Threading&op=dumpAllThreads&ps=true,true";
        // TODO: Call via ajax/promises to be able to parse the thread dump response first
        window.open("idp?saml_redirectUrl=" + encodeURIComponent(redirectUrl));
    },
    getStateForJvm: function(mData, type, fullData) {
        var jvmId = fullData.id.id;

        // The code is currently in transition (from a jvm specific state data to a generic once hence the if...)
        // TODO: When the backend is finished, we need to update this also!
        if (this.jvmsById[jvmId].state.jvmState !== undefined) {
            $(".jvm-state-" + jvmId).html(this.jvmsById[jvmId].state.jvmState);
        } else {
            $(".jvm-state-" + jvmId).html(this.jvmsById[jvmId].state.state);
        }

        return "<span class='jvm-state-" + jvmId + "'/>"
    },
    /* web server callbacks */
    buildHRefLoadBalancerConfig: function(data) {
        return "http://" + data.host + ":" + data.port + tocVars.loadBalancerStatusMount;
    },
    webServerStart: function(id) {
        webServerControlService.startWebServer(id);
        return true; // TODO Once status can be retrieved, return true if Web Server was successfully started
    },
    webServerStop: function(id) {
        webServerControlService.stopWebServer(id);
        return true;
    },

    /**
     * This method is responsible for displaying the state in the grid
     *
     */
    getStateForWebServer: function(mData, type, fullData) {
        var webServerId = fullData.id.id;

        if (this.webServersById !== undefined && this.webServersById[webServerId] !== undefined) {
            if (this.webServersById[webServerId].state !== undefined) {
                $(".ws-state-" + webServerId).html(this.webServersById[webServerId].state.state);
            } else {
                // ideally we should get the current state here instead of waiting for the next poll
                $(".ws-state-" + webServerId).html("UNKNOWN");
            }
        }

        return "<span class='ws-state-" + webServerId + "'/>"
    },

    getStateForGroup: function(mData, type, fullData) {
        var groupId = fullData.id.id;

        if (this.groupsById !== undefined && this.groupsById[groupId] !== undefined) {
            if (this.groupsById[groupId].state !== undefined) {
                $(".group-state-" + groupId).html(this.groupsById[groupId].state.state);
            } else  {
                // ideally we should get the current state here instead of waiting for the next poll
                $(".group-state-" + groupId).html("UNKNOWN");
            }
        }

        return "<span class='group-state-" + groupId + "'/>"
    }
});
