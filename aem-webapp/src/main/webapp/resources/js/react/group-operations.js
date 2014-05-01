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

        var childTableDetails = {tableIdPrefix:"group-operations-jvm-child-table",
                                 className:"simple-data-table"};

        var childTableDef = [{sTitle:"JVM ID", mData:"id.id", bVisible:false},
                             {sTitle:"Name", mData:"jvmName"},
                             {sTitle:"Host", mData:"hostName"}];

        childTableDetails["tableDef"] = childTableDef;

        return <TocDataTable tableId="group-operations-table"
                             tableDef={tableDef}
                             data={this.props.data}
                             expandIcon="public-resources/img/react/components/details-expand.png"
                             collapseIcon="public-resources/img/react/components/details-collapse.png"
                             rowSubComponentContainerClassName="row-sub-component-container"
                             childTableDetails={childTableDetails}
                             selectItemCallback={this.props.selectItemCallback}/>
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
   }
});