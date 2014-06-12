/** @jsx React.DOM */
var WebServerOperations = React.createClass({
    getInitialState: function() {
        return {
            webServerTableData: []
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
                                 {sTitle:"Name", mData:"name"},
                                 {sTitle:"Host", mData:"host"},
                                 {sTitle:"HTTP Port", mData:"port"},
                                 {sTitle:"HTTPS Port", mData:"httpsPort"},
                                 {sTitle:"Group Assignment",
                                  mData:"groups",
                                  tocType:"array",
                                  displayProperty:"name"},
                                 {sTitle:"",
                                  mData:null,
                                  tocType:"link",
                                  linkLabel:"Load Balancer Configuration",
                                  hRefCallback:this.buildHRefLoadBalancerConfig},
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
                             collapseIcon="public-resources/img/react/components/details-collapse.png"/>
   },
   buildHRefLoadBalancerConfig: function(data) {
        return "http://" + data.host + ":" + data.port + "/jkmanager/status";
   },
   showLoadBalancer: function(id) {
        alert("Show load balancer for web server with id = " + id);
   },
   start: function(id) {
        webServerControlService.startWebServer(id);
        return true; // TODO Once status can be retrieved, return true if Web Server was successfully started
   },
   stop: function(id) {
        webServerControlService.stopWebServer(id);
        return true;
   }

});