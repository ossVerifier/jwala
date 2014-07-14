/** @jsx React.DOM */
var GroupOperations = React.createClass({
    getInitialState: function() {
        selectedGroup = null;
        this.allJvmData = { jvms: [],
                            jvmStates: []};
        return {
//            Rationalize/unify all the groups/jvms/webapps/groupTableData/etc. stuff so it's coherent
            groupFormData: {},
            groupTableData: [],
            groups: [],
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
                                                              jvms={this.state.jvms}
                                                              jvmsById={groupOperationsHelper.keyJvmsById(this.state.jvms)}/>
                                </div>
                            </td>
                        </tr>
                   </table>
               </div>
    },
    selectItemCallback: function(item) {
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
    updateJvmStateData: function(newJvmStates) {
        this.setState(groupOperationsHelper.processJvmData(this.state.jvms,
                                                           [],
                                                           this.state.jvmStates,
                                                           newJvmStates));

        var jvmsToUpdate = groupOperationsHelper.getJvmStatesByGroupIdAndJvmId(this.state.jvms);
        jvmsToUpdate.forEach(function(jvm) { groupOperationsHelper.updateDataTables(jvm.groupId.id, jvm.jvmId.id, jvm.state);});
    },
    pollJvmStates: function() {
        var self = this;
        this.dataSink = this.props.jvmStateService.createDataSink(function(data) { self.updateJvmStateData(data);});
        this.props.jvmStateService.pollForUpdates(this.props.jvmStateTimeout, this.dataSink);
    },
    fetchCurrentJvmStates: function() {
        var self = this;
        this.props.jvmStateService.getCurrentStates().then(function(data) { self.updateJvmStateData(data.applicationResponseContent);})
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
        this.pollJvmStates();
        this.fetchCurrentJvmStates();
    },
    componentWillUnmount: function() {
        this.dataSink.stop();
    }
});

var GroupOperationsDataTable = React.createClass({
   shouldComponentUpdate: function(nextProps, nextState) {
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
                             {sTitle:"",
                              mData:null,
                              tocType:"button",
                              btnLabel:"Deploy",
                              btnCallback:this.deploy},
                              {sTitle:"",
                               mData:null,
                               tocType:"button",
                               btnLabel:"Start Group",
                               btnCallback:this.startGroupJvms},
                              {sTitle:"",
                               mData:null,
                               tocType:"button",
                               btnLabel:"Stop Group",
                               btnCallback:this.stopGroupJvms}];

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
                                           {sTitle:"",
                                            mData:null,
                                            tocType:"button",
                                            btnLabel:"Start",
                                            btnCallback:this.webServerStart},
                                           {sTitle:"",
                                            mData:null,
                                            tocType:"button",
                                            btnLabel:"Stop",
                                            btnCallback:this.webServerStop},
                                           {sTitle:"State",
                                            mData:null,
                                            mRender: this.getStateForWebServer}];

        var webServerOfGrpChildTableDetails = {tableIdPrefix:"group-operations-web-server-child-table",
                                               className:"simple-data-table",
                                               dataCallback:this.getWebServersOfGrp,
                                               title:"Web Servers",
                                               isCollapsible:true};

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
                                    isCollapsible:true};

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
                                 linkClick:this.jvmHeapDump},
                                {sTitle:"",
                                 mData:null,
                                 tocType:"link",
                                 linkLabel:"Thread Dump",
                                 linkClick:this.jvmThreadDump},
                                {sTitle:"",
                                 mData:null,
                                 tocType:"button",
                                 btnLabel:"",
                                 btnCallback:this.jvmStart,
                                 className:"start-button"},
                                {sTitle:"",
                                 mData:null,
                                 tocType:"button",
                                 btnLabel:"",
                                 btnCallback:this.jvmStop,
                                 className:"stop-button"},
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
        webServerService.getWebServerByGroupId(idObj.parentId, responseCallback);
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
   startGroupJvms: function(id) {
        groupControlService.startGroupJvms(id);
   },
   stopGroupJvms: function(id) {
        groupControlService.stopGroupJvms(id);
   },
   jvmManager: function(id) {
        alert("JVM show manager for jvm_" + id + "...");
   },
   jvmHeapDump: function(id) {
        alert("JVM show heap dump for jvm_" + id + "...");
   },
   jvmThreadDump: function(id) {
        alert("JVM show thread dump for jvm_" + id + "...");
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
    getStateForJvm: function(mData, type, fullData) {
        var jvmId = fullData.id.id;
        $(".jvm-state-" + jvmId).html(this.jvmsById[jvmId].state.jvmState);
        return "<span class='jvm-state-" + jvmId + "'/>"
    },
    /* web server callbacks */
    buildHRefLoadBalancerConfig: function(data) {
        return "http://" + data.host + ":" + data.port + loadBalancerStatusMount;
    },
    webServerStart: function(id) {
        webServerControlService.startWebServer(id);
        return true; // TODO Once status can be retrieved, return true if Web Server was successfully started
    },
    webServerStop: function(id) {
        webServerControlService.stopWebServer(id);
        return true;
    },
    getStateForWebServer: function(mData, type, fullData) {
        return "(UNDER_CONSTRUCTION)";
    },
});