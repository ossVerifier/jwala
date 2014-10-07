/** @jsx React.DOM */
var JvmConfig = React.createClass({
    getInitialState: function() {
        selectedJvm = null;
        return {
            showModalFormAddDialog: false,
            showModalFormEditDialog: false,
            showDeleteConfirmDialog: false,
            jvmFormData: {},
            jvmTableData: [{"jvmName":"","id":{"id":0},"hostName":"b","groups":[]}],
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
                                    <JvmDataTable data={this.state.jvmTableData}
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
                   <JvmConfigForm title="Add JVM"
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
                   <JvmConfigForm title="Edit JVM"
                                    show={this.state.showModalFormEditDialog}
                                    service={this.props.service}
                                    data={this.state.jvmFormData}
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
            this.props.service.deleteJvm(selectedJvm.id.id, self.retrieveData);
        }
    },
    retrieveData: function() {
        var self = this;
        this.props.service.getJvms(function(response){
                var jvmTableData = response.applicationResponseContent;
                groupService.getGroups(
                    function(response){
                        self.setState({jvmTableData:jvmTableData,
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
        if (selectedJvm != undefined) {
            this.setState({showDeleteConfirmDialog: true});
        }
    },
    selectItemCallback: function(item) {
        selectedJvm = item;
    },
    editCallback: function(data) {
        var thisComponent = this;
        this.props.service.getJvm(data.id.id,
            function(response){
                thisComponent.setState({jvmFormData: response.applicationResponseContent,
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

var JvmConfigForm = React.createClass({
    mixins: [
      Toc.mixins.PreventEnterSubmit
    ],
    validator: null,
    shouldComponentUpdate: function(nextProps, nextState) {
        return !nextProps.noUpdateWhen;
    },
    getInitialState: function() {
        return {
            id: "",
            name: "",
            host: "",
            statusPath: "",
            groupIds: undefined,
            httpPort: "",
            httpsPort: "",
            redirectPort: "",
            shutdownPort: "",
            ajpPort: ""
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
                if (name !== "groups") {
                    val = props.data[name];
                    if (subName !== undefined && val !== undefined && val[subName] !== undefined) {
                        val = val[subName];
                    }
                } else {
                    /**
                     * Because the group id is {id:object} where object = {id:[theId]} we need to
                     * convert the group id to {id:[theId]} for the multi-select checkbox component
                     * to consume. This is not the case for Web Server's group ids in which
                     * the said component was first used.
                     */
                    if (props.data[name] !== undefined) {
                        val = [];
                        for (var i = 0; i < props.data[name].length; i++) {
                            val.push({id:props.data[name][i].id.id});
                        }
                    }
                }
            }
        }
        return val;
    },
    componentWillReceiveProps: function(nextProps) {
        this.setState({id:this.getPropVal(nextProps, "id"),
                       name:this.getPropVal(nextProps, "jvmName"),
                       host:this.getPropVal(nextProps, "hostName"),
                       statusPath:this.getPropVal(nextProps, "statusPath", "/manager", "path"),
                       groupIds:this.getPropVal(nextProps, "groups"),
                       httpPort:this.getPropVal(nextProps, "httpPort"),
                       httpsPort:this.getPropVal(nextProps, "httpsPort"),
                       redirectPort:this.getPropVal(nextProps, "redirectPort"),
                       shutdownPort:this.getPropVal(nextProps, "shutdownPort"),
                       ajpPort:this.getPropVal(nextProps, "ajpPort")});
    },
    mixins: [React.addons.LinkedStateMixin],
    render: function() {
        var self = this;
        return  <div className={this.props.className} style={{display:"none"}}>
                    <form>
                        <input name="jvmId" type="hidden" value={this.state.id} />
                        <table>
                            <tr>
                                <td>*Name</td>
                            </tr>
                            <tr>
                                <td>
                                    <label htmlFor="jvmName" className="error"></label>
                                </td>
                            </tr>
                            <tr>
                                <td><input name="jvmName" type="text" valueLink={this.linkState("name")} required maxLength="255"/></td>
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
                                <td>*Status Path</td>
                            </tr>
                            <tr>
                                <td>
                                    <label htmlFor="statusPath" className="error"></label>
                                </td>
                            </tr>
                            <tr>
                                <td><input name="statusPath" type="text" valueLink={this.linkState("statusPath")} required maxLength="64"/></td>
                            </tr>

                            <tr>
                                <td>*HTTP Port</td>
                            </tr>
                            <tr>
                                <td>
                                    <label htmlFor="httpPort" className="error"></label>
                                </td>
                            </tr>
                            <tr>
                                <td><input name="httpPort" type="text" valueLink={this.linkState("httpPort")} required maxLength="5" onBlur={this.handleHttpBlur}/></td>
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
                                <td>*Redirect Port</td>
                            </tr>
                            <tr>
                                <td>
                                    <label htmlFor="redirectPort" className="error"></label>
                                </td>
                            </tr>
                            <tr>
                                <td><input name="redirectPort" type="text" valueLink={this.linkState("redirectPort")} required maxLength="5"/></td>
                            </tr>

                            <tr>
                                <td>*Shutdown Port</td>
                            </tr>
                            <tr>
                                <td>
                                    <label htmlFor="shutdownPort" className="error"></label>
                                </td>
                            </tr>
                            <tr>
                                <td><input name="shutdownPort" type="text" valueLink={this.linkState("shutdownPort")} required maxLength="5"/></td>
                            </tr>

                            <tr>
                                <td>*AJP Port</td>
                            </tr>
                            <tr>
                                <td>
                                    <label htmlFor="ajpPort" className="error"></label>
                                </td>
                            </tr>
                            <tr>
                                <td><input name="ajpPort" type="text" valueLink={this.linkState("ajpPort")} required maxLength="5"/></td>
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
    isHttpPortInteger: function() {
        return (this.state.httpPort % 1 === 0);
    },
    isValidHttpPort: function() {
        var port = this.state.httpPort;
        if ($.trim(port) && !isNaN(port) && this.isHttpPortInteger() && (port > 0 && port < 65532)) {
            return true;
        }
        return false;
    },
    handleHttpBlur: function() {
        if (this.isValidHttpPort()) {
            var basePort = parseInt(this.state.httpPort);
            var ports = {};
            if (!$.trim(this.state.httpsPort)) {
                ports.httpsPort = basePort + 1;
            }
            if (!$.trim(this.state.redirectPort)) {
                ports.redirectPort = basePort + 2;
            }
            if (!$.trim(this.state.shutdownPort)) {
                ports.shutdownPort = basePort + 3;
            }
            if (!$.trim(this.state.ajpPort)) {
                ports.ajpPort = basePort + 4;
            }
            this.setState(ports);
        }
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
                "jvmName": {
                    regex: true
                },
                "hostName": {
                    regex: true
                },
                "statusPath": {
                    pathCheck: true
                },
                "httpPort": {
                    range: [1, 65531]
                },
                "httpsPort": {
                    range: [1, 65535]
                },
                "redirectPort": {
                    range: [1, 65535]
                },
                "shutdownPort": {
                    range: [-1, 65535],
                    notEqualTo: 0
                },
                "ajpPort": {
                    range: [1, 65535]
                }
            },
            messages: {
                "groupSelector[]": {
                    required: "Please select at least 1 group"
                }
            }
        });

        //TODO These should be re-usable between JVM and WebServer
        $.validator.addMethod("pathCheck", function(value, element) {
            var exp = /\/.*/;
            return exp.test(value);
        }, "The field must be a valid, absolute path.");

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
    insertNewJvm: function() {
        if (this.isValid()) {
            this.props.service.insertNewJvm(this.state.name,
                                            this.state.groupIds,
                                            this.state.host,
                                            this.state.statusPath,
                                            this.state.httpPort,
                                            this.state.httpsPort,
                                            this.state.redirectPort,
                                            this.state.shutdownPort,
                                            this.state.ajpPort,
                                            this.success,
                                            function(errMsg) {
                                                $.errorAlert(errMsg, "Error");
                                            });
        }
    },
    updateJvm: function() {
        if (this.isValid()) {
            this.props.service.updateJvm($(this.getDOMNode().children[0]).serializeArray(),
                                         this.success,
                                         function(errMsg) {
                                             $.errorAlert(errMsg, "Error");
                                         });
        }
    },
    componentDidUpdate: function() {
        if (this.props.show === true) {

            // Check first if this component has been decorated already.
            // Decorate only once!
            if (!$(this.getDOMNode()).hasClass("ui-dialog-content")) {
                var okCallback = this.props.data === undefined ? this.insertNewJvm : this.updateJvm;
                decorateNodeAsModalFormDialog(this.getDOMNode(),
                                              this.props.title,
                                              okCallback,
                                              this.destroy,
                                              this.destroy);
            }

        }
    },
    destroy: function() {
        $(this.getDOMNode()).dialog("destroy");
        this.props.destroyCallback();
    }
});

var JvmDataTable = React.createClass({
    shouldComponentUpdate: function(nextProps, nextState) {
      return !nextProps.noUpdateWhen;
    },
    render: function() {
        var tableDef = [{sTitle:"JVM ID", mData:"id.id", bVisible:false},
                        {sTitle:"Name", mData:"jvmName", tocType:"link"},
                        {sTitle:"Host", mData:"hostName"},
                        {sTitle:"Status Path", mData:"statusPath.path"},
                        {sTitle:"Group",
                         mData:"groups",
                         tocType:"array",
                         displayProperty:"name",
                        sWidth: "40%"},
                        {sTitle:"Http", mData:"httpPort"},
                        {sTitle:"Https", mData:"httpsPort"},
                        {sTitle:"Redirect", mData:"redirectPort"},
                        {sTitle:"Shutdown", mData:"shutdownPort"},
                        {sTitle:"AJP", mData:"ajpPort"}];
        return <TocDataTable tableId="jvm-config-datatable"
                             tableDef={tableDef}
                             data={this.props.data}
                             selectItemCallback={this.props.selectItemCallback}
                             editCallback={this.props.editCallback}/>
    }
});