/** @jsx React.DOM */
var WebServerConfig = React.createClass({
    getInitialState: function() {
        selectedWebServer = null;
        return {
            showModalFormAddDialog: false,
            showModalFormEditDialog: false,
            showDeleteConfirmDialog: false,
            webServerFormData: {},
            webServerTableData: [],
            groupMultiSelectData: []
        }
    },
    shouldComponentUpdate: function(nextProps, nextState) {
        if(
          (!nextState.showModalFormAddDialog && this.state.showModalFormAddDialog)
        ||(!nextState.showModalFormEditDialog && this.state.showModalFormEditDialog)
        ){
          return false;
        }
        return true;
    },
    render: function() {
        var btnDivClassName = this.props.className + "-btn-div";
        return  <div className={this.props.className}>
                    <table>
                        <tr>
                            <td>
                                <div style={{float:"right"}}>
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
    editCallback: function(data) {
        var thisComponent = this;
        this.props.service.getWebServer(data.id.id,
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
            httpsPort: "",
            statusPath: "",
            httpConfigFile: "",
            groupIds: undefined
        }
    },
    getPropVal: function(props, name, defaultVal, subName) {
        var val = "";
        if (defaultVal !== undefined) {
            val = defaultVal;
        }
        if (props.data !== undefined) {
            if (name === "id" && props.data[name] !== undefined) {
                val = props.data[name].id;
            } else if (name !== "id") {
                val = props.data[name];
            }
            if (subName !== undefined && val !== undefined && val[subName] !== undefined) {
                val = val[subName];
            }
        }
        return val;
    },
    componentWillReceiveProps: function(nextProps) {
        this.setState({id:this.getPropVal(nextProps, "id"),
                       name:this.getPropVal(nextProps, "name"),
                       host:this.getPropVal(nextProps, "host"),
                       port:this.getPropVal(nextProps, "port"),
                       httpsPort:this.getPropVal(nextProps, "httpsPort"),
                       statusPath:this.getPropVal(nextProps, "statusPath", tocVars.loadBalancerStatusMount, "path"),
                       httpConfigFile:this.getPropVal(nextProps,
                                                      "httpConfigFile",
                                                      "D:/apache/httpd-2.4.9/conf/httpd.conf",
                                                      "path"),
                       groupIds:this.getPropVal(nextProps, "groupIds")});
    },
    mixins: [
        React.addons.LinkedStateMixin,
        Toc.mixins.PreventEnterSubmit
    ],
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
                                <td><input name="webserverName" type="text" valueLink={this.linkState("name")} required maxLength="255"/></td>
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
                                <td><input name="hostName" type="text" valueLink={this.linkState("host")} required maxLength="255"/></td>
                            </tr>
                            <tr>
                                <td>*HTTP Port</td>
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
                                <td>HTTPS Port</td>
                            </tr>
                            <tr>
                                <td>
                                    <label htmlFor="httpsPort" className="error"></label>
                                </td>
                            </tr>
                            <tr>
                                <td><input name="httpsPort" type="text" valueLink={this.linkState("httpsPort")} maxLength="5"/></td>
                            </tr>
                            <tr>
                                <td>Status Path</td>
                            </tr>
                            <tr>
                                <td>
                                    <label htmlFor="statusPath" className="error"></label>
                                </td>
                            </tr>
                            <tr>
                                <td><input name="statusPath" type="text" valueLink={this.linkState("statusPath")} maxLength="64"/></td>
                            </tr>
                            <tr>
                                <td>*HTTP Config File</td>
                            </tr>
                            <tr>
                                <td>
                                    <label htmlFor="httpConfigFile" className="error"></label>
                                </td>
                            </tr>
                            <tr>
                                <td><input name="httpConfigFile" type="text" valueLink={this.linkState("httpConfigFile")} maxLength="64"/></td>
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
                "httpsPort": {
                    range: [1, 65535]
                },
                "webserverName": {
                    regex: true
                },
                "hostName": {
                    regex: true
                },
                "statusPath": {
                    pathCheck: true
                },
                "httpConfigFile": {
                    required: true
                }
            },
            messages: {
                "groupSelector[]": {
                    required: "Please select at least 1 group"
                }
            }
        });

        $.validator.addMethod("pathCheck", function(value, element) {
            var exp = /\/.*/;
            return exp.test(value);
        }, "The field must be a valid, absolute path.");

        $.validator.addMethod("regex", function(value, element) {
            // TODO: Verfiy if Siemen's host naming convention follows that of a regular domain name
            // var exp = /^[a-zA-Z0-9][a-zA-Z0-9-]{1,61}[a-zA-Z0-9]\.[a-zA-Z]{2,}$/;
            var exp = /^[a-zA-Z0-9-.]+$/i;
            return this.optional(element) || exp.test(value);
        }, "The field must only contain letters, numbers, dashes or periods.");

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
                                                  this.state.httpsPort,
                                                  this.state.statusPath,
                                                  this.state.httpConfigFile,
                                                  this.success,
                                                  function(errMsg) {
                                                        $.errorAlert(errMsg, "Error");
                                                  });
            return true;
        }
        return false;
    },
    updateWebServer: function() {
        if (this.isValid()) {
            this.props.service.updateWebServer($(this.getDOMNode().children[0]).serializeArray(),
                                               this.success,
                                               function(errMsg) {
                                                    $.errorAlert(errMsg, "Error");
                                               });
            return true;
        }
        return false;
    },
    componentDidUpdate: function() {
        if (this.props.show === true) {

            // Check first if this component has been decorated already.
            // Decorate only once!
            if (!$(this.getDOMNode()).hasClass("ui-dialog-content")) {
                var okCallback = this.props.data === undefined ? this.insertNewWebServer : this.updateWebServer;
                decorateNodeAsModalFormDialog(this.getDOMNode(),
                                              this.props.title,
                                              okCallback,
                                              this.destroy,
                                              this.destroy);
            }

        }
    },
    destroy: function() {
        this.validator.resetForm();
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
                        {sTitle:"Name", mData:"name", tocType:"custom", tocRenderCfgFn:this.renderNameLink},
                        {sTitle:"Host", mData:"host"},
                        {sTitle:"Port", mData:"port"},
                        {sTitle:"Https Port", mData:"httpsPort"},
			{sTitle:"Status Path", mData:"statusPath.path"},
			{sTitle:"HTTP Config File", mData:"httpConfigFile.path"},
                        {sTitle:"Group",
                         mData:"groups",
                         tocType:"array",
                         displayProperty:"name",
                         sWidth: "40%", maxDisplayTextLen:20}];
        return <TocDataTable tableId="webserver-config-datatable"
                             tableDef={tableDef}
                             colHeaders={["JVM Name", "Host Name"]}
                             data={this.props.data}
                             selectItemCallback={this.props.selectItemCallback}
                             editCallback={this.props.editCallback}
                             rowSubComponentContainerClassName="row-sub-component-container"/>
    },
    renderNameLink:function(dataTable, data, aoColumnDefs, itemIndex) {
        var self = this;
        aoColumnDefs[itemIndex].fnCreatedCell = function ( nTd, sData, oData, iRow, iCol ) {
            return React.renderComponent(new React.DOM.button({className:"button-link",
                                         onClick:self.props.editCallback.bind(this, oData)}, sData), nTd);
        }.bind(this);
    }
});
