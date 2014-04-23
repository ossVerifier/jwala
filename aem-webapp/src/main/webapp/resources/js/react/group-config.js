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
                                    <GroupDataTable data={this.state.groupTableData}
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
                   <ModalFormDialog title="Add Group"
                                    show={this.state.showModalFormAddDialog}
                                    form={<GroupConfigForm service={this.props.service}
                                                           successCallback={this.addSuccessCallback}/>}
                                    destroyCallback={this.closeModalFormAddDialog}
                                    className="textAlignLeft"
                                    noUpdateWhen={
                                        this.state.showDeleteConfirmDialog ||
                                        this.state.showModalFormEditDialog
                                    }/>
                   <ModalFormDialog title="Edit Group"
                                    show={this.state.showModalFormEditDialog}
                                    form={<GroupConfigForm service={this.props.service}
                                                           data={this.state.groupFormData}
                                                           successCallback={this.editSuccessCallback}/>}
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
    getInitialState: function() {
        var groupId = "";
        var groupName = "";
        if (this.props.data !== undefined) {
            groupId = this.props.data.id.id;
            groupName = this.props.data.name;
        }
        return {
            validator: null,
            groupId: groupId,
            groupName: groupName,
        }
    },
    render: function() {
        return <form>
                    <input name="id" type="hidden" defaultValue={this.state.groupId} />
                    <table>
                        <tr>
                            <td>Name</td>
                        </tr>
                        <tr>
                            <td><input name="name" type="text" defaultValue={this.state.groupName} required/></td>
                        </tr>
                    </table>
               </form>
    },
    componentDidMount: function() {

        var thisComponent = this;
        var svc = thisComponent.props.service;
        var data = thisComponent.props.data;

        $(this.getDOMNode()).off("submit");
        $(this.getDOMNode()).on("submit", function(e) {

            if (data === undefined) {
                svc.insertNewGroup($("input[name=name]").val(),
                                   thisComponent.props.successCallback,
                                   function(errMsg) {
                                        $.errorAlert(errMsg, "Error");
                                   });
            } else {
                svc.updateGroup($(thisComponent.getDOMNode()).serializeArray(),
                                  thisComponent.props.successCallback,
                                  function(errMsg) {
                                        $.errorAlert(errMsg, "Error");
                                  });
            }

            e.preventDefault(); // stop the default action
        });

        this.setState({validator:$(this.getDOMNode()).validate({ignore: ":hidden"})});
    }
});

var GroupDataTable = React.createClass({
   shouldComponentUpdate: function(nextProps, nextState) {
    
      return !nextProps.noUpdateWhen;
        
    },
    render: function() {
        var headerExt = [{sTitle:"", mData: "jvms", tocType:"control"},
                         {sTitle:"Group ID", mData:"id.id", bVisible:false},
                         {sTitle:"Group Name", mData:"name", tocType:"link"}];
        return <TocDataTable tableId="groupDataTable"
                             theme="default"
                             headerExt={headerExt}
                             colHeaders={["JVM Name", "Host Name"]}
                             data={this.props.data}
                             selectItemCallback={this.props.selectItemCallback}
                             editCallback={this.props.editCallback}
                             expandIcon="public-resources/img/react/components/details-expand.png"
                             collapseIcon="public-resources/img/react/components/details-collapse.png"
                             rowSubComponentContainerClassName="row-sub-component-container"/>
    }
});