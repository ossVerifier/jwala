/** @jsx React.DOM */
var WebServerConfig = React.createClass({
    getInitialState: function() {
        selectedWebServer = null;
        return {
            showModalFormAddDialog: false,
            showModalFormEditDialog: false,
            showDeleteConfirmDialog: false,
            webServerFormData: {},
            webServerTableData: [{"name":"","id":{"id":0},"host":"b","port":9000,"groups":[]}],
            groupMultiSelectData: []
        }
    },
    render: function() {
        var btnDivClassName = this.props.className + "-btn-div";
        return  <div className={this.props.className}>
                    <table>
                        <tr>
                            <td>
                                <div>
                                    <GenericButton label="Delete" callback={this.delBtnCallback}/>
                                    <GenericButton label="Add" callback={this.addBtnCallback}/>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <div>
                                    <WebServerDataTable data={this.state.webServerTableData}
                                                    selectItemCallback={this.selectItemCallback}
                                                    editCallback={this.editCallback}
                                                    noUpdateWhen={
                                                      this.state.showModalFormAddDialog || 
                                                      this.state.showDeleteConfirmDialog ||
                                                      this.state.showModalFormEditDialog 
                                                      }/>
                                </div>
                            </td>
                        </tr>
                   </table>
                   <WebServerConfigForm title="Add Web Server"
                                        show={this.state.showModalFormAddDialog}
                                        service={this.props.service}
                                        groupMultiSelectData={this.state.groupMultiSelectData}
                                        successCallback={this.addSuccessCallback}
                                        destroyCallback={this.closeModalFormAddDialog}
                                        className="textAlignLeft"
                                        noUpdateWhen={
                                            this.state.showDeleteConfirmDialog ||
                                            this.state.showModalFormEditDialog
                                        }/>
                   <WebServerConfigForm title="Edit Web Server"
                                    show={this.state.showModalFormEditDialog}
                                    service={this.props.service}
                                    data={this.state.webServerFormData}
                                    groupMultiSelectData={this.state.groupMultiSelectData}
                                    successCallback={this.editSuccessCallback}
                                    destroyCallback={this.closeModalFormEditDialog}
                                    className="textAlignLeft"
                                    noUpdateWhen={
                                        this.state.showModalFormAddDialog ||
                                        this.state.showDeleteConfirmDialog
                                    }/>
                   <ConfirmDeleteModalDialog show={this.state.showDeleteConfirmDialog}
                                             btnClickedCallback={this.confirmDeleteCallback} />
               </div>
    },
    confirmDeleteCallback: function(ans) {
        var self = this;
        this.setState({showDeleteConfirmDialog: false});
        if (ans === "yes") {
            this.props.service.deleteWebServer(selectedWebServer.id.id, self.retrieveData);
        }
    },
    retrieveData: function() {
        var self = this;
        this.props.service.getWebServers(function(response){
                var webServerTableData = response.applicationResponseContent;
                groupService.getGroups(
                    function(response){
                        self.setState({webServerTableData:webServerTableData,
                                       groupMultiSelectData:response.applicationResponseContent});
                    }
                );
        });
    },
    addSuccessCallback: function() {
        this.retrieveData();
        this.closeModalFormAddDialog();
    },
    editSuccessCallback: function() {
        this.retrieveData();
        this.closeModalFormEditDialog();
    },
    addBtnCallback: function() {
        this.setState({showModalFormAddDialog: true})
    },
    delBtnCallback: function() {
        if (selectedWebServer != undefined) {
            this.setState({showDeleteConfirmDialog: true});
        }
    },
    selectItemCallback: function(item) {
        selectedWebServer = item;
    },
    editCallback: function(id) {
        var thisComponent = this;
        this.props.service.getWebServer(id,
            function(response){
                thisComponent.setState({webServerFormData: response.applicationResponseContent,
                                        showModalFormEditDialog: true})
            }
        );
    },
    closeModalFormAddDialog: function() {
        this.setState({showModalFormAddDialog: false})
    },
    closeModalFormEditDialog: function() {
        this.setState({showModalFormEditDialog: false})
    },
    componentDidMount: function() {
        // this.retrieveData();
    },
    componentWillMount: function() {
        this.retrieveData();
    }
});

var WebServerConfigForm = React.createClass({
    validator: null,
    shouldComponentUpdate: function(nextProps, nextState) {
        return !nextProps.noUpdateWhen;
    },
    getInitialState: function() {
        return {
            id: "",
            name: "",
            host: "",
            port: "",
            groupIds: undefined
        }
    },
    getPropVal: function(props, name) {
        var val = "";
        if (props.data !== undefined) {
            if (name === "id" && props.data[name] !== undefined) {
                val = props.data[name].id;
            } else if (name !== "id") {
                val = props.data[name];
            }
        }
        return val;
    },
    componentWillReceiveProps: function(nextProps) {
        this.setState({id:this.getPropVal(nextProps, "id"),
                       name:this.getPropVal(nextProps, "name"),
                       host:this.getPropVal(nextProps, "host"),
                       port:this.getPropVal(nextProps, "port"),
                       groupIds:this.getPropVal(nextProps, "groupIds")});
    },
    mixins: [React.addons.LinkedStateMixin],
    render: function() {
        var self = this;
        return  <div className={this.props.className} style={{display:"none"}}>
                    <form>
                        <input name="webserverId" type="hidden" value={this.state.id} />
                        <table>
                            <tr>
                                <td>*Name</td>
                            </tr>
                            <tr>
                                <td>
                                    <label htmlFor="webserverName" className="error"></label>
                                </td>
                            </tr>
                            <tr>
                                <td><input name="webserverName" type="text" valueLink={this.linkState("name")} required maxLength="35"/></td>
                            </tr>
                            <tr>
                                <td>*Host</td>
                            </tr>
                            <tr>
                                <td>
                                    <label htmlFor="hostName" className="error"></label>
                                </td>
                            </tr>
                            <tr>
                                <td><input name="hostName" type="text" valueLink={this.linkState("host")} required maxLength="35"/></td>
                            </tr>
                            <tr>
                                <td>*Port</td>
                            </tr>
                            <tr>
                                <td>
                                    <label htmlFor="portNumber" className="error"></label>
                                </td>
                            </tr>
                            <tr>
                                <td><input name="portNumber" type="text" valueLink={this.linkState("port")} required maxLength="5"/></td>
                            </tr>
                            <tr>
                                <td>
                                    *Group
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <label htmlFor="groupSelector[]" className="error"></label>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <DataMultiSelectBox name="groupSelector[]"
                                                        data={this.props.groupMultiSelectData}
                                                        selectedValIds={this.state.groupIds}
                                                        key="id"
                                                        keyPropertyName="id"
                                                        val="name"
                                                        className="data-multi-select-box"
                                                        onSelectCallback={this.onSelectGroups}
                                                        idKey="groupId"/>
                                </td>
                            </tr>

                        </table>
                    </form>
                </div>
    },
    onSelectGroups: function(groupIds) {
        this.setState({groupIds:groupIds});
    },
    isValid: function() {
        if (this.validator !== null) {
            this.validator.form();
            if (this.validator.numberOfInvalids() === 0) {
                return true;
            }
        } else {
            alert("There is no validator for the form!");
        }
        return false;
    },
    componentDidMount: function() {
        this.validator = $(this.getDOMNode().children[0]).validate({
            ignore: ":hidden",
            rules: {
                "groupSelector[]": {
                    required: true
                },
                "portNumber": {
                    range: [1, 65535]
                },
                "webserverName": {
                    regex: true
                },
                "hostName": {
                    regex: true
                }
            },
            messages: {
                "groupSelector[]": {
                    required: "Please select at least 1 group"
                }
            }
        });

        $.validator.addMethod("regex", function(value, element) {
            // TODO: Verfiy if Siemen's host naming convention follows that of a regular domain name
            // var exp = /^[a-zA-Z0-9][a-zA-Z0-9-]{1,61}[a-zA-Z0-9]\.[a-zA-Z]{2,}$/;
            var exp = /^[a-zA-Z0-9-_.]+$/i;
            return this.optional(element) || exp.test(value);
        }, "The field must only contain letters, numbers, underscore, dashes or periods.");

    },
    success: function() {
        this.props.successCallback();
        $(this.getDOMNode()).dialog("destroy");
    },
    insertNewWebServer: function() {
        if (this.isValid()) {
            this.props.service.insertNewWebServer(this.state.name,
                                                  this.state.groupIds,
                                                  this.state.host,
                                                  this.state.port,
                                                  this.success,
                                                  function(errMsg) {
                                                        $.errorAlert(errMsg, "Error");
                                                  });
        }
    },
    updateWebServer: function() {
        if (this.isValid()) {
            this.props.service.updateWebServer($(this.getDOMNode().children[0]).serializeArray(),
                                               this.success,
                                               function(errMsg) {
                                                    $.errorAlert(errMsg, "Error");
                                               });
        }
    },
    componentDidUpdate: function() {
        if (this.props.show === true) {
            var okCallback = this.props.data === undefined ? this.insertNewWebServer : this.updateWebServer;
            decorateNodeAsModalFormDialog(this.getDOMNode(),
                                          this.props.title,
                                          okCallback,
                                          this.destroy,
                                          this.props.destroyCallback);
        }
    },
    destroy: function() {
        $(this.getDOMNode()).dialog("destroy");
        this.props.destroyCallback();
    }
});

var WebServerDataTable = React.createClass({
    shouldComponentUpdate: function(nextProps, nextState) {
      return !nextProps.noUpdateWhen;
    },
    render: function() {
        var tableDef = [{sTitle:"Web Server ID", mData:"id.id", bVisible:false},
                        {sTitle:"Name", mData:"name", tocType:"link"},
                        {sTitle:"Host", mData:"host"},
                        {sTitle:"Port", mData:"port"},
                        {sTitle:"Group Assignment",
                         mData:"groups",
                         tocType:"array",
                         displayProperty:"name"}];
        return <TocDataTable tableId="webserver-config-datatable"
                             tableDef={tableDef}
                             colHeaders={["JVM Name", "Host Name"]}
                             data={this.props.data}
                             selectItemCallback={this.props.selectItemCallback}
                             editCallback={this.props.editCallback}
                             expandIcon="public-resources/img/react/components/details-expand.png"
                             collapseIcon="public-resources/img/react/components/details-collapse.png"
                             rowSubComponentContainerClassName="row-sub-component-container"/>
    }
});