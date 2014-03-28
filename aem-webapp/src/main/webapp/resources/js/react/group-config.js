/** @jsx React.DOM */
var GroupConfig = React.createClass({
    getInitialState: function() {
        return {
            showModalFormAddDialog: false,
            showDeleteConfirmDialog: false,
            showModalFormEditDialog: false,
            groupData: {}
        }
    },
    render: function() {
        return  <div className={this.props.className}>
                    <table className={this.props.className}>
                        <tr>
                            <td>
                                <GenericButton label="Add" callback={this.addBtnCallback}/>
                                <GenericButton label="Delete" callback={this.delBtnCallback}/>
                                <GenericButton label="Temp Edit" callback={this.tmpEditBtnCallback}/>
                            </td>
                        </tr>
                        <tr>
                            <td>Data table here...</td>
                        </tr>
                   </table>
                   <ModalFormDialog title="Add Group"
                                    show={this.state.showModalFormAddDialog}
                                    form={<GroupConfigForm service={this.props.service}/>}
                                    destroyCallback={this.closeModalFormAddDialog}/>
                   <ModalFormDialog title="Edit Group"
                                    show={this.state.showModalFormEditDialog}
                                    form={<GroupConfigForm service={this.props.service} data={this.state.groupData}/>}
                                    destroyCallback={this.closeModalFormEditDialog}/>
               </div>
    },
    addBtnCallback: function() {
        this.setState({showModalFormAddDialog: true})
    },
    delBtnCallback: function() {
        // this.setState({showDeleteConfirmDialog: true})
        // TODO: Wire this up with the confirmation dialog once the datatable is done.
        this.props.service.deleteGroup(102).then(

            function(){
                alert("Done!");
            },
            function(response) {
                alert(JSON.stringify(response));
            }

        );
    },
    // TODO: Remove once data table is already working
    tmpEditBtnCallback: function() {

        var thisComponent = this;
        this.props.service.getGroup(4).then(
            function(response){
                thisComponent.setState({groupData: response.applicationResponseContent,
                                        showModalFormEditDialog: true})
            },
            function(response) {
                alert(JSON.stringify(response));
            }
        );

    },
    closeModalFormAddDialog: function() {
        this.setState({showModalFormAddDialog: false})
    },
    closeModalFormEditDialog: function() {
        this.setState({showModalFormEditDialog: false})
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