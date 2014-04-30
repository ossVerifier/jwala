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
    editCallback: function(id) {
        var thisComponent = this;
        this.props.service.getJvm(id,
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
    validator: null,
    shouldComponentUpdate: function(nextProps, nextState) {
        return !nextProps.noUpdateWhen;
    },
    getInitialState: function() {
        return {
            id: "",
            name: "",
            host: "",
            groupIds: undefined
        }
    },
    getPropVal: function(props, name) {
        var val = "";
        if (props.data !== undefined) {
            if (name === "id" && props.data[name] !== undefined) {
                val = props.data[name].id;
            } else if (name !== "id") {
                if (name !== "groups") {
                    val = props.data[name];
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
                       groupIds:this.getPropVal(nextProps, "groups")});
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
                                <td><input name="jvmName" type="text" valueLink={this.linkState("name")} required maxLength="35"/></td>
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
                "jvmName": {
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
    insertNewJvm: function() {
        if (this.isValid()) {
            this.props.service.insertNewJvm(this.state.name,
                                                  this.state.groupIds,
                                                  this.state.host,
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
            var okCallback = this.props.data === undefined ? this.insertNewJvm : this.updateJvm;
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

var JvmDataTable = React.createClass({
    shouldComponentUpdate: function(nextProps, nextState) {
      return !nextProps.noUpdateWhen;
    },
    render: function() {
        var tableDef = [{sTitle:"JVM ID", mData:"id.id", bVisible:false},
                        {sTitle:"Name", mData:"jvmName", tocType:"link"},
                        {sTitle:"Host", mData:"hostName"},
                        {sTitle:"Group Assignment",
                         mData:"groups",
                         tocType:"array",
                         displayProperty:"name"}];
        return <TocDataTable tableId="jvm-config-datatable"
                             tableDef={tableDef}
                             data={this.props.data}
                             selectItemCallback={this.props.selectItemCallback}
                             editCallback={this.props.editCallback}/>
    }
});