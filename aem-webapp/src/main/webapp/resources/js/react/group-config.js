/** @jsx React.DOM */
var GroupConfig = React.createClass({
    getInitialState: function() {
        selectedGroup = null;
        return {
            showModalFormAddDialog: false,
            showModalFormEditDialog: false,
            showDeleteConfirmDialog: false,
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
                                                                this.state.showModalFormEditDialog
                                                          }/>
                                </div>
                            </td>
                        </tr>
                   </table>
                   <GroupConfigForm title="Add Group"
                                    show={this.state.showModalFormAddDialog}
                                    service={this.props.service}
                                    successCallback={this.addSuccessCallback}
                                    destroyCallback={this.closeModalFormAddDialog}
                                    className="textAlignLeft"
                                    noUpdateWhen={
                                        this.state.showDeleteConfirmDialog ||
                                        this.state.showModalFormEditDialog
                                    }/>
                  <GroupConfigForm title="Edit Group"
                                  show={this.state.showModalFormEditDialog}
                                  service={this.props.service}
                                  data={this.state.groupFormData}
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
            this.props.service.deleteGroup(selectedGroup.id.id, self.retrieveData);
        }
    },
    retrieveData: function() {
        var self = this;
        this.props.service.getGroups(function(response){
                                        self.setState({groupTableData:response.applicationResponseContent});
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
        if (selectedGroup != undefined) {
            this.setState({showDeleteConfirmDialog: true});
        }
    },
    selectItemCallback: function(item) {
        selectedGroup = item;
    },
    editCallback: function(id) {
        var thisComponent = this;
        this.props.service.getGroup(id,
            function(response){
                thisComponent.setState({groupFormData: response.applicationResponseContent,
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
        this.retrieveData();
    }
});

var GroupConfigForm = React.createClass({
    validator: null,
    shouldComponentUpdate: function(nextProps, nextState) {
        return !nextProps.noUpdateWhen;
    },
    getInitialState: function() {
        return {
            groupId: "",
            groupName: "",
        }
    },
    getGroupIdProp: function(props) {
        if (props.data !== undefined && props.data.id !== undefined) {
            return props.data.id.id;
        }
        return "";
    },
    getGroupProp: function(props, name) {
        if (props.data !== undefined) {
            return props.data[name];
        }
        return "";
    },
    componentWillReceiveProps: function(nextProps) {
        this.setState({groupId:this.getGroupIdProp(nextProps), groupName:this.getGroupProp(nextProps, "name")});
    },
    render: function() {
        return <div className={this.props.className} style={{display:"none"}}>
                    <form>
                        <input name="id" type="hidden" value={this.state.groupId} />
                        <table>
                            <tr>
                                <td>Name</td>
                            </tr>
                            <tr>
                                <td><input name="name"
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
    componentDidMount: function() {
        if (this.validator === null) {
            this.validator = $(this.getDOMNode().children[0]).validate({ignore: ":hidden"});
        }
    },
    componentDidUpdate: function() {
        if (this.props.show === true) {
            var okCallback = this.props.data === undefined ? this.insertNewGroup : this.updateGroup;
            decorateNodeAsModalFormDialog(this.getDOMNode(),
                                          this.props.title,
                                          okCallback,
                                          this.destroy,
                                          this.props.destroyCallback);
        }
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
    insertNewGroup: function() {
        var self = this;
        if (this.isValid()) {
            this.props.service.insertNewGroup(this.state.groupName,
                                              function(){
                                                self.props.successCallback();
                                                $(self.getDOMNode()).dialog("destroy");
                                              },
                                              function(errMsg) {
                                                    $.errorAlert(errMsg, "Error");
                                              });
        }
    },
    updateGroup: function() {
        var self = this;
        if (this.isValid()) {
            this.props.service.updateGroup($(this.getDOMNode().children[0]).serializeArray(),
                                           function(){
                                                self.props.successCallback();
                                                $(self.getDOMNode()).dialog("destroy");
                                           },
                                           function(errMsg) {
                                                $.errorAlert(errMsg, "Error");
                                           });
        }
    },
    destroy: function() {
        $(this.getDOMNode()).dialog("destroy");
        this.props.destroyCallback();
    }
});

var GroupConfigDataTable = React.createClass({
    shouldComponentUpdate: function(nextProps, nextState) {
      return !nextProps.noUpdateWhen;
    },
    render: function() {
        var tableDef = [{sTitle:"", mData: "jvms", tocType:"control"},
                        {sTitle:"Group ID", mData:"id.id", bVisible:false},
                        {sTitle:"Group Name", mData:"name", tocType:"link"}];

        var childTableDetails = {tableIdPrefix:"group-config-jvm-child-table",
                                 className:"simple-data-table"};

        var childTableDef = [{sTitle:"JVM ID", mData:"id.id", bVisible:false},
                             {sTitle:"JVM Name", mData:"jvmName"},
                             {sTitle:"Host", mData:"hostName"}];

        childTableDetails["tableDef"] = childTableDef;

        return <TocDataTable tableId="group-config-table"
                             tableDef={tableDef}
                             data={this.props.data}
                             selectItemCallback={this.props.selectItemCallback}
                             editCallback={this.props.editCallback}
                             expandIcon="public-resources/img/react/components/details-expand.png"
                             collapseIcon="public-resources/img/react/components/details-collapse.png"
                             rowSubComponentContainerClassName="row-sub-component-container"
                             childTableDetails={childTableDetails}/>
    }
});