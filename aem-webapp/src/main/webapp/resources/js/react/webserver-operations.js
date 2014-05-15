/** @jsx React.DOM */
var WebServerOperations = React.createClass({
    getInitialState: function() {
        // selectedGroup = null;
        return {
        //    groupFormData: {},
            webServerTableData: [{"id":{"id":0},"host":"b","port":9000,"groups":[]}]
        }
    },
    render: function() {
        var btnDivClassName = this.props.className + "-btn-div";
        return  <div className={this.props.className}>
                    <table>
                        <tr>
                            <td>
                                <div>
                                    <WebServerOperationsDataTable data={this.state.webServerTableData}
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
        this.props.service.getWebServers(function(response){
                                        self.setState({webServerTableData:response.applicationResponseContent});
                                     });
    },
    componentDidMount: function() {
        this.retrieveData();
    }
});

var WebServerOperationsDataTable = React.createClass({
   shouldComponentUpdate: function(nextProps, nextState) {
      return !nextProps.noUpdateWhen;
    },
    render: function() {
        var webServerTableDef = [{sTitle:"Web Server ID", mData:"id.id", bVisible:false},
                                 {sTitle:"Host", mData:"host"},
                                 {sTitle:"Port", mData:"port"},
                                 {sTitle:"Group Assignment",
                                  mData:"groups",
                                  tocType:"array",
                                  displayProperty:"name"},
                                 {sTitle:"",
                                  mData:null,
                                  tocType:"button",
                                  btnLabel:"Load Balancer Configuration",
                                  btnCallback:this.showLoadBalancerConfig},
                                 {sTitle:"",
                                  mData:null,
                                  tocType:"button",
                                  btnLabel:"Load Balancer",
                                  btnCallback:this.showLoadBalancer},
                                 {sTitle:"",
                                  mData:null,
                                  tocType:"button",
                                  btnLabel:"Start",
                                  btnCallback:this.start,
                                  isToggleBtn:true,
                                  label2:"Stop",
                                  callback2:this.stop}];

        return <TocDataTable tableId="web-server-operations-table"
                             tableDef={webServerTableDef}
                             data={this.props.data}
                             expandIcon="public-resources/img/react/components/details-expand.png"
                             collapseIcon="public-resources/img/react/components/details-collapse.png"
                             selectItemCallback={this.props.selectItemCallback}/>
   },
   showLoadBalancerConfig: function(id) {
        alert("Show load balancer config for web server with id = " + id);
   },
   showLoadBalancer: function(id) {
        alert("Show load balancer for web server with id = " + id);
   },
   start: function(id) {
        alert("Start web server with id = " + id);
        return true;
   },
   stop: function(id) {
        alert("Stop web server with id = " + id);
        return true;
   }

});