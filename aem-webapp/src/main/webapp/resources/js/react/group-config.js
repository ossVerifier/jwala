/** @jsx React.DOM */
var GroupConfig = React.createClass({
    getInitialState: function() {
        selectedGroup = null;
        return {
            showModalFormAddDialog: false,
            showDeleteConfirmDialog: false,
            showModalFormEditDialog: false,
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
                                                    editCallback={this.editCallback}/>
                                </div>
                            </td>
                        </tr>
                   </table>
                   <ModalFormDialog title="Add Group"
                                    show={this.state.showModalFormAddDialog}
                                    form={<GroupConfigForm service={this.props.service}/>}
                                    successCallback={this.addEditSuccessCallback}
                                    destroyCallback={this.closeModalFormAddDialog}/>
                   <ModalFormDialog title="Edit Group"
                                    show={this.state.showModalFormEditDialog}
                                    form={<GroupConfigForm service={this.props.service}
                                                           data={this.state.groupFormData}/>}
                                    successCallback={this.addEditSuccessCallback}
                                    destroyCallback={this.closeModalFormEditDialog}/>
                   <ConfirmDeleteModalDialog show={this.state.showDeleteConfirmDialog}
                                             btnClickedCallback={this.confirmDeleteCallback} />
               </div>
    },
    confirmDeleteCallback: function(ans) {
        var self = this;
        this.setState({showDeleteConfirmDialog: false});
        if (ans === "yes") {
            this.props.service.deleteGroup(selectedGroup.id.id).then(
                function(){
                },
                function(response) {
                    if (response.status !== 200) {
                        $.errorAlert(JSON.stringify(response), "Error");
                    }
                    self.retrieveData();
                }
            );
        }
    },
    retrieveData: function() {
        var self = this;
        this.props.service.getGroups().then(

            function(response){
                self.setState({groupTableData:response.applicationResponseContent});
            },
            function(response) {
                $.errorAlert(JSON.stringify(response), "Error");
            }

        );
    },
    addEditSuccessCallback: function() {
        this.retrieveData();
    },
    addBtnCallback: function() {
        this.setState({showModalFormAddDialog: true})
    },
    selectItemCallback: function(item) {
        selectedGroup = item;
    },
    delBtnCallback: function() {
        if (selectedGroup != undefined) {
            this.setState({showDeleteConfirmDialog: true});
        }
    },
    editCallback: function(name) {
        var thisComponent = this;
        this.props.service.getGroupByName(name).then(
            function(response){
                thisComponent.setState({groupFormData: response.applicationResponseContent,
                                        showModalFormEditDialog: true})
            },
            function(response) {
                $.errorAlert(JSON.stringify(response), "Error");
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
        return <form action="v1.0/groups">
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
        this.setState({validator:$(this.getDOMNode()).validate({ignore: ":hidden"})});
    },
    submit: function(done, fail) {
        var thisComponent = this;
        var svc = thisComponent.props.service;
        var data = thisComponent.props.data;

        $(this.getDOMNode()).one("submit", function(e) {

            if (data === undefined) {
                // TODO: Specifying group name to get value seems brittle, I think this has to be refactored.
                svc.insertNewGroup($("input[name=name]").val()).then(
                    function(){
                        done();
                    },
                    function(response) {
                        fail(JSON.parse(response.responseText).applicationResponseContent);
                    }
                );
            } else {
                svc.updateGroup($(thisComponent.getDOMNode()).serializeArray()).then(
                    function(){
                        done();
                    },
                    function(response) {
                        fail(JSON.parse(response.responseText).applicationResponseContent);
                    }
                );
            }

            e.preventDefault(); // stop the default action
        });

        if (this.state.validator !== null) {
            this.state.validator.cancelSubmit = true;
            this.state.validator.form();
            if (this.state.validator.numberOfInvalids() === 0) {
                $(this.getDOMNode()).submit();
            }
        } else {
            alert("There is no validator for the form!");
        }

    }
});

var GroupDataTable = React.createClass({
    render: function() {
        var headerExt = [{sTitle:"Group ID", mData:"id.id", bVisible:false},
                         {sTitle:"Group Name", mData:"name", tocType:"link"}];
        return <TocDataTable2 theme="default"
                              headerExt={headerExt}
                              data={this.props.data}
                              selectItemCallback={this.props.selectItemCallback}
                              editCallback={this.props.editCallback}/>
    }
});