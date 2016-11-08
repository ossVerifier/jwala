/** @jsx React.DOM */
var JvmConfig = React.createClass({

    /**
     * This object is used to let the component know when to update itself.
     * It essentially uses a kind of "toggle switch" pattern wherein when set
     * the flag is set to true then when "checked/viewed" the flag is set to false.
     * This mechanism is required to essentially tell the component if the
     * table needs to be updated since the underlying table is using jQuery Datatable.
     * This should not be necessary if the entire table is purely made in React.
     */
    cancelFlag: {
        flag: false,
        set: function() {
            this.flag = true;
        },
        check: function () {
            var prevFlag = this.flag;
            this.flag = false; // reset the flag
            return prevFlag;
        }
    },


    selectedJvm: null,
    getInitialState: function() {
        return {
            showModalFormAddDialog: false,
            showModalFormEditDialog: false,
            showDeleteConfirmDialog: false,
            selectedJvmForEditing: null,
            jvmTableData: [{"jvmName":"","id":{"id":0},"hostName":"b","groups":[]}]
        }
    },
    render: function() {
        var btnDivClassName = this.props.className + "-btn-div";
        return  <div className={this.props.className} className="dataTables_wrapper">
                    <table className="jvm-config-table-type-container">
                        <tr>
                            <td>
                                <div style={{float:"right"}}>
                                    <GenericButton label="Delete" accessKey="d" callback={this.delBtnCallback}/>
                                    <GenericButton label="Add" accessKey="a" callback={this.addBtnCallback}/>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <div>
                                    <JvmConfigDataTable ref="jvmConfigDataTable"
                                                        data={this.state.jvmTableData}
                                                        selectItemCallback={this.selectItemCallback}
                                                        editCallback={this.editCallback}
                                                        noUpdateWhen={this.state.showModalFormAddDialog ||
                                                                      this.state.showDeleteConfirmDialog ||
                                                                      this.state.showModalFormEditDialog ||
                                                                      this.cancelFlag.check()
                                                        }/>
                                </div>
                            </td>
                        </tr>
                   </table>

                   <ModalDialogBox title="Add JVM"
                                   show={this.state.showModalFormAddDialog}
                                   okCallback={this.okAddCallback}
                                   cancelCallback={this.cancelAddCallback}
                                   content={<JvmConfigForm ref="jvmAddForm" />}
                                   width="auto"
                                   height="auto"/>

                   <ModalDialogBox title="Edit JVM"
                                   show={this.state.showModalFormEditDialog}
                                   okCallback={this.okEditCallback}
                                   cancelCallback={this.cancelEditCallback}
                                   content={<JvmConfigForm ref="jvmEditForm"
                                                           data={this.state.selectedJvmForEditing}/>}
                    />

                    <ModalDialogBox title="Confirmation Dialog Box"
                                    show={this.state.showDeleteConfirmDialog}
                                    okCallback={this.confirmDeleteCallback}
                                    cancelCallback={this.cancelDeleteCallback}
                                    content={<div className="text-align-center"><br/><b>Are you sure you want to delete the selected item ?</b><br/><br/></div>}
                                    okLabel="Yes"
                                    cancelLabel="No" />
               </div>
    },
    cancelAddCallback: function() {
        this.cancelFlag.set();
        this.setState({showModalFormAddDialog:false});
    },
    cancelEditCallback: function() {
        this.cancelFlag.set();
        this.setState({showModalFormEditDialog:false});
    },
    okAddCallback: function() {
        if (this.refs.jvmAddForm.isValid()) {
            var self = this;

            this.props.service.insertNewJvm(this.refs.jvmAddForm.state.name,
                                            this.refs.jvmAddForm.state.groupIds,
                                            this.refs.jvmAddForm.state.host,
                                            this.refs.jvmAddForm.state.statusPath,
                                            "",
                                            this.refs.jvmAddForm.state.httpPort,
                                            this.refs.jvmAddForm.state.httpsPort,
                                            this.refs.jvmAddForm.state.redirectPort,
                                            this.refs.jvmAddForm.state.shutdownPort,
                                            this.refs.jvmAddForm.state.ajpPort,
                                            this.refs.jvmAddForm.state.userName,
                                            this.refs.jvmAddForm.state.encryptedPassword,
                                            function(){
                                                self.refs.jvmConfigDataTable.getDataTableWrapper().deselectAllRows();
                                                self.selectedJvm = null;
                                                self.refreshData({showModalFormAddDialog:false});
                                            },
                                            function(errMsg) {
                                                $.errorAlert(errMsg, "Error");
                                                if (errMsg.indexOf("Multiple groups were associated with the JVM") === 0) {
                                                    self.refreshData({showModalFormAddDialog:false});
                                                }
                                            });
        }
    },
    okEditCallback: function() {
        if (this.refs.jvmEditForm.isValid()) {
            var self = this;
            this.props.service.updateJvm($(this.refs.jvmEditForm.getDOMNode().children[0]).serializeArray(),
                                           function(){
                                               self.refreshData({showModalFormEditDialog:false});
                                           },
                                           function(errMsg) {
                                               $.errorAlert(errMsg, "Error");
                                           });
        }
    },
    refreshData: function(states, doneCallback) {
        var self = this;
        this.props.service.getJvms(function(response){
                                         states["jvmTableData"] = response.applicationResponseContent;
                                         if (doneCallback !== undefined) {
                                            doneCallback();
                                         }
                                         self.setState(states);
                                     });
    },
    addBtnCallback: function() {
        this.setState({showModalFormAddDialog: true})
    },
    delBtnCallback: function() {
        if (this.selectedJvm !== null) {
            this.setState({showDeleteConfirmDialog: true});
        }
    },
    confirmDeleteCallback: function() {
        var self = this;
        this.props.service.deleteJvm(this.selectedJvm.id.id,
                                       this.refreshData.bind(this,
                                                             {showDeleteConfirmDialog: false},
                                                             function(){self.selectedJvm = null}));
    },
    cancelDeleteCallback: function() {
        this.cancelFlag.set();
        this.setState({showDeleteConfirmDialog: false});
    },
    selectItemCallback: function(item) {
        this.selectedJvm = item;
    },
    editCallback: function(e) {
        var self = this;
        this.props.service.getJvm(e.data.id.id,
            function(response){
                self.setState({selectedJvmForEditing:response.applicationResponseContent,
                               showModalFormEditDialog:true});
            }
        );
    },
    componentDidMount: function() {
        this.refreshData({});
    },

});

/**
 * The form that provides data input.
 */
var JvmConfigForm = React.createClass({
    getInitialState: function() {

        var jvmName = "";
        var id = "";
        var name = "";
        var host = "";
        var statusPath = "/manager"; // TODO: Define in a properties file
        var groupIds = [];
        var httpPort = "";
        var httpsPort = "";
        var redirectPort = "";
        var shutdownPort = "";
        var ajpPort = "";
        var userName = "";
        var encryptedPassword = "";

        if (this.props.data !== undefined) {
            id = this.props.data.id;
            name = this.props.data.jvmName;
            host = this.props.data.hostName;
            statusPath = this.props.data.statusPath.path;
            this.props.data.groups.forEach(function(group) {
                groupIds.push(group.id);
            });
            httpPort = this.props.data.httpPort;
            httpsPort = this.props.data.httpsPort;
            redirectPort = this.props.data.redirectPort;
            shutdownPort = this.props.data.shutdownPort;
            ajpPort = this.props.data.ajpPort;
            userName = this.props.data.userName;
            encryptedPassword = this.props.data.encryptedPassword;
        }

        return {
            id: id,
            name: name,
            host: host,
            statusPath: statusPath,
            groupIds: groupIds,
            groupMultiSelectData: [],
            httpPort: httpPort,
            httpsPort: httpsPort,
            redirectPort: redirectPort,
            shutdownPort: shutdownPort,
            ajpPort: ajpPort,
            userName: userName,
            encryptedPassword: encryptedPassword
        }
    },
    mixins: [React.addons.LinkedStateMixin],
    render: function() {
        var jvmId =  this.props.data !== undefined ? this.props.data.id.id : "";
        return <div className={this.props.className}>
                    <form ref="jvmConfigForm">
                        <input type="hidden" name="id" value={jvmId} />
                        <input type="hidden" name="systemProperties" value="" />
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
                                <td><input ref="jvmName" name="jvmName" type="text" valueLink={this.linkState("name")}
                                           required maxLength="255" className="width-max"/></td>
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
                                <td><input name="hostName" type="text" valueLink={this.linkState("host")}
                                           required maxLength="255" className="width-max"/></td>
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
                                <td>
                                    <div className="jvmStatusUrl">
                                        {window.location.protocol + "//" + this.state.host + ":" + (window.location.protocol === "https:" ? this.state.httpsPort : this.state.httpPort) + this.state.statusPath}
                                    </div>
                                    <input name="statusPath" type="text" valueLink={this.linkState("statusPath")} required maxLength="64" className="width-max"/>
                                </td>
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
                            	<td>Username</td>
                            </tr>
                            <tr>
                            	<td>
                                	<label htmlFor="userName" className="error"></label>
                                </td>
                            </tr>
                            <tr>
                            	<td><input name="userName" type="text" valueLink={this.linkState("userName")} /></td>
                            </tr>
                            
                            <tr>
                            	<td>Password</td>
                            </tr>
                            <tr>
                        		<td>
                            		<label htmlFor="encryptedPassword" className="error"></label>
                            	</td>
                            </tr>
                            <tr>
                        		<td><input name="encryptedPassword" type="password" valueLink={this.linkState("encryptedPassword")} /></td>
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
                                    <div className="jvmStatusUrl">
                                    {this.props.data === undefined && this.state.groupIds.length > 1 ? "The JVM templates will only be inherited from a single group" : ""}
                                    </div>
                                    <DataMultiSelectBox name="groupSelector[]"
                                                        data={this.state.groupMultiSelectData}
                                                        selectedValIds={this.state.groupIds}
                                                        dataField="id.id"
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
    validator: null,
    componentDidMount: function() {
        this.validator = $(this.getDOMNode().children[0]).validate({ignore: ":hidden",
                                                                    rules: {"groupSelector[]": {required: true},
                                                                            "jvmName": {nameCheck: true},
                                                                            "hostName": {hostNameCheck: true},
                                                                            "statusPath": {pathCheck: true},
                                                                            "httpPort": {range: [1, 65531]},
                                                                            "httpsPort": {range: [1, 65535]},
                                                                            "redirectPort": {range: [1, 65535]},
                                                                            "shutdownPort": {
                                                                                range: [-1, 65535],
                                                                                notEqualTo: 0
                                                                             },
                                                                            "ajpPort": {range: [1, 65535]}
                                                                            },
                                                                            messages: {
                                                                                "groupSelector[]": {
                                                                                    required: "Please select at least 1 group"
                                                                                 }
                                                                            }
                                                                    });
        $(this.refs.jvmName.getDOMNode()).focus();

        $(this.refs.jvmConfigForm.getDOMNode()).submit(function(e) {
            e.preventDefault();
        });

        this.retrieveGroups();
    },
    isValid: function() {
        this.validator.form();
        if (this.validator.numberOfInvalids() === 0) {
            return true;
        }
        return false;
    },
    retrieveGroups: function() {
        var self = this;
        groupService.getGroups().then(function(response){
                                        self.setState({groupMultiSelectData:response.applicationResponseContent});
                                      });
    }
});

/**
 * The jvm data table.
 */
var JvmConfigDataTable = React.createClass({
    shouldComponentUpdate: function(nextProps, nextState) {
        return !nextProps.noUpdateWhen;
    },
    render: function() {
        var tableDef = [{sTitle:"JVM ID", mData:"id.id", bVisible:false},
                        {sTitle:"Name", mData:"jvmName", jwalaType:"custom", jwalaRenderCfgFn:this.renderNameLink, colWidth:"200px"},
                        {sTitle:"Host", mData:"hostName", colWidth:"150px", maxDisplayTextLen:45},
                        {sTitle:"Sts Path", mData:"statusPath.path", colWidth:"100px", maxDisplayTextLen:20},
                        {sTitle:"Group",
                         mData:"groups",
                         jwalaType:"array",
                         displayProperty:"name",
                         colWidth: "100px",
                         maxDisplayTextLen:10},
                        {sTitle:"HTTP", mData:"httpPort"},
                        {sTitle:"HTTPS", mData:"httpsPort"},
                        {sTitle:"Redir", mData:"redirectPort"},
                        {sTitle:"Shutd", mData:"shutdownPort"},
                        {sTitle:"AJP", mData:"ajpPort"},
                        {sTitle:"Username", mData: "userName"}];
        return <JwalaDataTable tableId="jvm-config-datatable"
                               ref="dataTableWrapper"
                               tableDef={tableDef}
                               data={this.props.data}
                               selectItemCallback={this.props.selectItemCallback}
                               editCallback={this.props.editCallback}
                               isColResizable={true}/>
    },
    renderNameLink:function(dataTable, data, aoColumnDefs, itemIndex) {
        var self = this;
        aoColumnDefs[itemIndex].fnCreatedCell = function ( nTd, sData, oData, iRow, iCol ) {
            return React.renderComponent(React.createElement("button", {className:"button-link", title:sData}, sData), nTd, function() {
                $(this.getDOMNode()).click(oData, self.props.editCallback);
            });
        };
   },
   getDataTableWrapper: function() {
       return this.refs.dataTableWrapper;
   }
});
