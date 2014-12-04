/** @jsx React.DOM */
var GroupConfig = React.createClass({


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


    selectedGroup: null,
    getInitialState: function() {
        return {
            showModalFormAddDialog: false,
            showModalFormEditDialog: false,
            showDeleteConfirmDialog: false,
            selectedGroupForEditing: null,
            groupTableData: [{"name":"","id":{"id":0}}]
        }
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
                                    <GroupConfigDataTable data={this.state.groupTableData}
                                                          selectItemCallback={this.selectItemCallback}
                                                          editCallback={this.editCallback}
                                                          noUpdateWhen={
                                                                this.state.showModalFormAddDialog ||
                                                                this.state.showDeleteConfirmDialog ||
                                                                this.state.showModalFormEditDialog ||
                                                                this.cancelFlag.check()
                                                          }/>
                                </div>
                            </td>
                        </tr>
                   </table>

                   <ModalDialogBox title="Add Group"
                                   show={this.state.showModalFormAddDialog}
                                   okCallback={this.okAddCallback}
                                   cancelCallback={this.cancelAddCallback}
                                   content={<GroupConfigForm ref="groupAddForm" />}
                                   width="auto"
                                   height="auto"/>

                   <ModalDialogBox title="Edit Group"
                                   show={this.state.showModalFormEditDialog}
                                   okCallback={this.okEditCallback}
                                   cancelCallback={this.cancelEditCallback}
                                   content={<GroupConfigForm ref="groupEditForm"
                                                             data={this.state.selectedGroupForEditing}/>}
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
        if (this.refs.groupAddForm.isValid()) {
            var self = this;
            this.props.service.insertNewGroup(this.refs.groupAddForm.state.groupName,
                                              function(){
                                                  self.refreshData({showModalFormAddDialog:false});
                                              },
                                              function(errMsg) {
                                                  $.errorAlert(errMsg, "Error");
                                              });
        }
    },
    okEditCallback: function() {
        if (this.refs.groupEditForm.isValid()) {
            var self = this;
            this.props.service.updateGroup($(this.refs.groupEditForm.getDOMNode().children[0]).serializeArray(),
                                           function(){
                                               self.refreshData({showModalFormEditDialog:false});
                                           },
                                           function(errMsg) {
                                               $.errorAlert(errMsg, "Error");
                                           });
        }
    },

    /**
     * Retrieve data from REST Api the set states passed in parameter "states".
     */
    refreshData: function(states, doneCallback) {
        var self = this;
        this.props.service.getGroups(function(response){
                                         states["groupTableData"] = response.applicationResponseContent;
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
        if (this.selectedGroup !== null) {
            this.setState({showDeleteConfirmDialog: true});
        }
    },
    confirmDeleteCallback: function() {
        var self = this;
        this.props.service.deleteGroup(this.selectedGroup.id.id,
                                       this.refreshData.bind(this,
                                                             {showDeleteConfirmDialog: false},
                                                             function(){self.selectedGroup = null}));
    },
    cancelDeleteCallback: function() {
        this.cancelFlag.set();
        this.setState({showDeleteConfirmDialog: false});
    },
    selectItemCallback: function(item) {
        this.selectedGroup = item;
    },
    editCallback: function(data) {
        var self = this;
        this.props.service.getGroup(data.id.id,
            function(response){
                self.setState({selectedGroupForEditing:response.applicationResponseContent,
                               showModalFormEditDialog:true});
            }
        );
    },
    componentDidMount: function() {
        this.refreshData({});
    }
});

/**
 * The form that provides data input.
 */
var GroupConfigForm = React.createClass({
    getInitialState: function() {
        var groupName = this.props.data !== undefined ? this.props.data.name : "";
        return {
            groupName: groupName,
        }
    },
    render: function() {
        var groupId =  this.props.data !== undefined ? this.props.data.id.id : "";
        return <div className={this.props.className}>
                    <form>
                        <input type="hidden" name="id" value={groupId} />
                        <table>
                            <tr>
                                <td>Name</td>
                            </tr>
                            <tr>
                                <td>
                                    <label htmlFor="name" className="error"></label>
                                </td>
                            </tr>
                            <tr>
                                <td><input ref="groupName"
                                           name="name"
                                           type="text"
                                           value={this.state.groupName}
                                           onChange={this.onChangeGroupName}
                                           required/></td>
                            </tr>
                        </table>
                    </form>
               </div>
    },
    onChangeGroupName: function(event) {
        this.setState({groupName:event.target.value});
    },
    validator: null,
    componentDidMount: function() {
        this.validator = $(this.getDOMNode().children[0]).validate({ignore: ":hidden"});
        $(this.refs.groupName.getDOMNode()).focus();
    },
    isValid: function() {
        this.validator.form();
        if (this.validator.numberOfInvalids() === 0) {
            return true;
        }
        return false;
    }
});

/**
 * The group data table.
 */
var GroupConfigDataTable = React.createClass({
    shouldComponentUpdate: function(nextProps, nextState) {
      return !nextProps.noUpdateWhen;
    },
    render: function() {
        var tableDef = [{sTitle:"", mData: "jvms", tocType:"control", colWidth:"10px"},
                        {sTitle:"Group ID", mData:"id.id", bVisible:false},
                        {sTitle:"Group Name", mData:"name", tocType:"link", colWidth:"1132px"}];

        var childTableDetails = {tableIdPrefix:"group-config-jvm-child-table",
                                 className:"simple-data-table"};

        var childTableDef = [{sTitle:"JVM ID", mData:"id.id", bVisible:false},
                             {sTitle:"JVM Name", mData:"jvmName"},
                             {sTitle:"Host", mData:"hostName"}];

        childTableDetails["tableDef"] = childTableDef;

        return <TocDataTable tableId="group-config-table"
                             className="groupConfig dataTable hierarchical"
                             tableDef={tableDef}
                             data={this.props.data}
                             selectItemCallback={this.props.selectItemCallback}
                             editCallback={this.props.editCallback}
                             rowSubComponentContainerClassName="row-sub-component-container"
                             childTableDetails={childTableDetails}/>
    }
});