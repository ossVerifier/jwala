/** @jsx React.DOM */
var GroupOperations = React.createClass({
    getInitialState: function() {
        selectedGroup = null;
        return {
            groupFormData: {},
            groupTableData: [{"name":"","id":{"id":0}}]
        }
    },
    render: function() {
        var btnDivClassName = this.props.className + "-btn-div";
        return  <div className={this.props.className}>
                    <table>
                        <tr>
                            <td>
                                <div>
                                    <GroupOperationsDataTable data={this.state.groupTableData}
                                                              selectItemCallback={this.selectItemCallback}/>
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
                                     });
    },
    componentDidMount: function() {
        this.retrieveData();
    }
});

var GroupOperationsDataTable = React.createClass({
   shouldComponentUpdate: function(nextProps, nextState) {
      return !nextProps.noUpdateWhen;
    },
    render: function() {
        var tableDef = [{sTitle:"", mData: "jvms", tocType:"control"},
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
                         btnLabel:"Undeploy",
                         btnCallback:this.undeploy},
                        {sTitle:"",
                         mData:null,
                         tocType:"button",
                         btnLabel:"Start",
                         btnCallback:this.start,
                         isToggleBtn:true,
                         label2:"Stop",
                         callback2:this.stop}];

        var webAppChildTableDetails = {tableIdPrefix:"group-operations-web-app-child-table",
                                       className:"simple-data-table",
                                       getDataCallback:this.getApplications};

        var webAppChildTableDef = [{sTitle:"JVM ID", mData:"id.id", bVisible:false},
                                   {sTitle:"Name", mData:"name"},
                                   {sTitle:"War Path", mData:"warPath"},
                                   {sTitle:"Context", mData:"webAppContext"}];

        webAppChildTableDetails["tableDef"] = webAppChildTableDef;


        var jvmChildTableDetails = {tableIdPrefix:"group-operations-jvm-child-table",
                                    className:"simple-data-table",
                                    childTableDetails:webAppChildTableDetails};

        var jvmChildTableDef = [{sTitle:"", mData:null, tocType:"control"},
                                {sTitle:"JVM ID", mData:"id.id", bVisible:false},
                                {sTitle:"Name", mData:"jvmName"},
                                {sTitle:"Host", mData:"hostName"},
                                {sTitle:"",
                                 mData:null,
                                 tocType:"button",
                                 btnLabel:"Manager",
                                 btnCallback:this.jvmManager},
                                {sTitle:"",
                                 mData:null,
                                 tocType:"button",
                                 btnLabel:"Heap Dump",
                                 btnCallback:this.jvmHeapDump},
                                {sTitle:"",
                                 mData:null,
                                 tocType:"button",
                                 btnLabel:"Thread Dump",
                                 btnCallback:this.jvmThreadDump},
                                {sTitle:"",
                                 mData:null,
                                 tocType:"button",
                                 btnLabel:"Deploy",
                                 btnCallback:this.jvmDeploy},
                                {sTitle:"",
                                 mData:null,
                                 tocType:"button",
                                 btnLabel:"Start",
                                 btnCallback:this.jvmStart,
                                 isToggleBtn:true,
                                 label2:"Stop",
                                 callback2:this.jvmStop}];

        jvmChildTableDetails["tableDef"] = jvmChildTableDef;


        return <TocDataTable tableId="group-operations-table"
                             tableDef={tableDef}
                             data={this.props.data}
                             expandIcon="public-resources/img/react/components/details-expand.png"
                             collapseIcon="public-resources/img/react/components/details-collapse.png"
                             rowSubComponentContainerClassName="row-sub-component-container"
                             childTableDetails={jvmChildTableDetails}
                             selectItemCallback={this.props.selectItemCallback}/>
   },
   getApplications: function(groupId, responseCallback) {
        webAppService.getWebAppsByGroup(groupId, responseCallback);
   },
   deploy: function(id) {
        alert("Deploy applications for group_" + id + "...");
   },
   undeploy: function(id) {
        alert("Undeploy applications for group_" + id + "...");
   },
   start: function(id) {
        alert("Start applications for group_" + id + "...");
   },
   stop: function(id) {
        alert("Stop applications for group_" + id + "...");
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
   jvmDeploy: function(id) {
        alert("JVM deploy applications for jvm_" + id + "...");
   },
   jvmStart: function(id) {
        alert("JVM start applications for jvm_" + id + "...");
   },
   jvmStop: function(id) {
        alert("JVM stop applications for jvm_" + id + "...");
   }

});