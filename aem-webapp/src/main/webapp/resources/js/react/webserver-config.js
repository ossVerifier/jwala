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
                   <ModalFormDialog title="Add Web Server"
                                    show={this.state.showModalFormAddDialog}
                                    form={<WebServerConfigForm service={this.props.service}
                                                               groupMultiSelectData={this.state.groupMultiSelectData}/>}
                                    successCallback={this.addEditSuccessCallback}
                                    destroyCallback={this.closeModalFormAddDialog}
                                    className="textAlignLeft"/>
                   <ModalFormDialog title="Edit Web Server"
                                    show={this.state.showModalFormEditDialog}
                                    form={<WebServerConfigForm service={this.props.service}
                                                           data={this.state.webServerFormData}
                                                           groupMultiSelectData={this.state.groupMultiSelectData}/>}
                                    successCallback={this.addEditSuccessCallback}
                                    destroyCallback={this.closeModalFormEditDialog}
                                    className="textAlignLeft"/>
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
                self.setState({webServerTableData:response.applicationResponseContent})
            });

        groupService.getGroups(
            function(response){
                self.setState({groupMultiSelectData:response.applicationResponseContent});
            }
        );
    },
    addEditSuccessCallback: function() {
        this.retrieveData();
        return true;
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
    getInitialState: function() {
        var id = "";
        var name = "";
        var host = "";
        var port = "";

        if (this.props.data !== undefined) {
            id = this.props.data.id.id;
            name = this.props.data.name;
            host = this.props.data.host;
            port = this.props.data.port;
        }
        return {
            validator: null,
            id: id,
            name: name,
            host: host,
            port: port
        }
    },
    render: function() {
        var self = this;
        return <form action="v1.0/webServers">
                    <input name="webserverId" type="hidden" defaultValue={this.state.id} />
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
                            <td><input name="webserverName" type="text" defaultValue={this.state.name} required maxLength="35"/></td>
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
                            <td><input name="hostName" type="text" defaultValue={this.state.host} required maxLength="35"/></td>
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
                            <td><input name="portNumber" type="text" defaultValue={this.state.port} required maxLength="5"/></td>
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
                                                    key="id"
                                                    keyPropertyName="id"
                                                    val="name"
                                                    className="data-multi-select-box"/>
                            </td>
                        </tr>

                    </table>
               </form>
    },
    componentDidMount: function() {

        var validator = $(this.getDOMNode()).validate({
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
            return this.optional(element) || /^[a-zA-Z0-9-_]+$/i.test(value);
        }, "The field must only contain letters, numbers, underscore, or dashes.");

        if (this.props.data !== undefined) {
            // Process multi-select data if there are any
            $.each(this.props.data.groups, function(i, obj) {
                $("input[value=" + obj.id.id + "]").prop("checked", true)
            });
        }

        this.setState({validator:validator});
    },
    submit: function(done, fail) {
        var thisComponent = this;
        var svc = thisComponent.props.service;
        var data = thisComponent.props.data;

        $(this.getDOMNode()).one("submit", function(e) {

            if (data === undefined) {

                var groupIds = [];
                $("input[name='groupSelector[]']").each(function () {
                    if ($(this).prop("checked")) {
                        groupIds.push({groupId:$(this).val()});
                    }
                });

                svc.insertNewWebServer($("input[name=webserverName]").val(),
                                          groupIds,
                                          $("input[name=hostName]").val(),
                                          $("input[name=portNumber]").val(),
                                          done,
                                          fail);

            } else {
                svc.updateWebServer($(thisComponent.getDOMNode()).serializeArray(),
                                    done,
                                    fail);
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

var WebServerDataTable = React.createClass({
   shouldComponentUpdate: function(nextProps, nextState) {
    
      return !nextProps.noUpdateWhen;
        
    },
    render: function() {
        var headerExt = [{sTitle:"Web Server ID", mData:"id.id", bVisible:false},
                         {sTitle:"Name", mData:"name", tocType:"link"},
                         {sTitle:"Host", mData:"host"},
                         {sTitle:"Port", mData:"port"},
                         {sTitle:"Group Assignment",
                          mData:"groups",
                          tocType:"array",
                          displayProperty:"name"}];
        return <TocDataTable theme="default"
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