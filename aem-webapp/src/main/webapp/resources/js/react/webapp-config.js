/** @jsx React.DOM */
var WebAppConfig = React.createClass({
    getInitialState: function() {
        selectedWebApp = null;
        return {
            showModalFormAddDialog: false,
            showModalFormEditDialog: false,
            showDeleteConfirmDialog: false,
            WebAppFormData: {},
            WebAppTableData: [],
            groupSelectData: [],
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
                                    <WebAppDataTable data={this.state.WebAppTableData}
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
                   <WebAppConfigForm title="Add WebApp"
                                    show={this.state.showModalFormAddDialog}
                                    groupSelectData={this.state.groupSelectData}
                                    service={this.props.service}
                                    successCallback={this.addSuccessCallback}
                                    destroyCallback={this.closeModalFormAddDialog}
                                    className="textAlignLeft"
                                    noUpdateWhen={
                                        this.state.showDeleteConfirmDialog ||
                                        this.state.showModalFormEditDialog
                                    }/>
                  <WebAppConfigForm title="Edit WebApp"
                                  show={this.state.showModalFormEditDialog}
                                  service={this.props.service}
                                  data={this.state.WebAppFormData}
                                  groupSelectData={this.state.groupSelectData}
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
            this.props.service.deleteWebApp(selectedWebApp.id.id, self.retrieveData);
        }
    },
    retrieveData: function() {
        var self = this;
        this.props.service.getWebApps(function(response){
                                        self.setState({WebAppTableData:response.applicationResponseContent});
                                     });
        this.props.groupService.getGroups(
            function(response){
                self.setState({groupSelectData:response.applicationResponseContent});
            }
        );                                     
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
        if (selectedWebApp != undefined) {
            this.setState({showDeleteConfirmDialog: true});
        }
    },
    selectItemCallback: function(item) {
        selectedWebApp = item;
    },
    editCallback: function(id) {
        var thisComponent = this;
        this.props.service.getWebApp(id,
            function(response){
                thisComponent.setState({WebAppFormData: response.applicationResponseContent,
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

var WebAppConfigForm = React.createClass({
    mixins: [React.addons.LinkedStateMixin],
    validator: null,
    shouldComponentUpdate: function(nextProps, nextState) {
        return !nextProps.noUpdateWhen;
    },
    getInitialState: function() {
        return {
            WebAppId: "",
            WebAppName: "",
            WebAppContext: "",
            GroupId: "",
            groupIds: [],
        }
    },
    getWebAppIdProp: function(props) {
        if (props.data !== undefined && props.data.id !== undefined) {
            return props.data.id.id;
        }
        return "";
    },
    getWebAppProp: function(props, name) {
        if (props.data !== undefined) {
            return props.data[name];
        }
        return "";
    },
    getWebAppGroupIdProp: function(props) {
        if (props.data !== undefined && props.data.group !== undefined && props.data.group.id !== undefined) {
            return props.data.group.id.id;
        }
        return "";
    },
    getWebAppGroupProp: function(props, name) {
        if (props.data !== undefined && props.data.group !== undefined ) {
            return props.data.group[name];
        }
        return "";
    },
    componentWillReceiveProps: function(nextProps) {
        this.setState(
          { WebAppId:this.getWebAppIdProp(nextProps), 
            WebAppName:this.getWebAppProp(nextProps, "name"),
            WebAppContext:this.getWebAppProp(nextProps, "webAppContext"),
            GroupId:this.getWebAppGroupIdProp(nextProps, "id"),
            GroupName:this.getWebAppGroupProp(nextProps, "name"),
          });
    },
    render: function() {
      
        return <div className={this.props.className} style={{display:"none"}}>
                    <form>
                        <input name="webappId" type="hidden" value={this.state.WebAppId} />
                        <table>
                            <tr>
                                <td>Name</td>
                            </tr>
                            <tr>
                                <td><input name="name"
                                           type="text"
                                           value={this.state.WebAppName}
                                           onChange={this.onChangeWebAppName}
                                           maxLength="255"
                                           required/></td>
                            </tr>
                            <tr>
                                <td>
                                    *Context path
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <label htmlFor="webappContext" className="error"></label>
                                </td>
                            </tr>
                            <tr>
                                <td><input name="webappContext" type="text" valueLink={this.linkState("WebAppContext")} required maxLength="255"/></td>
                            </tr>
                            <tr>
                                <td>
                                    *Associated Group
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <label htmlFor="groupId" className="error"></label>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                <DataCombobox           name="groupId"
                                                        data={this.props.groupSelectData}
                                                        selectedVal={this.state.GroupId}
                                                        key="id.id"
                                                        val="name"
                                                        onChange={this.onSelectGroup}/>
                                </td>
                            </tr>
                        </table>
                    </form>
               </div>
    },
    /*                                    <DataMultiSelectBox name="groupSelector[]"
                                                        data={this.props.groupSelectData}
                                                        selectedValIds={this.state.groupIds}
                                                        key="id"
                                                        keyPropertyName="id"
                                                        val="name"
                                                        className="data-multi-select-box"
                                                        onSelectCallback={this.onSelectGroups}
                                                        idKey="groupId"/>*/

    onSelectGroups: function(groupIds) {
        this.setState({groupIds:groupIds});
    },
    onSelectGroup: function(e) {
        this.setState({GroupId:e.target.value});
    },
    onChangeWebAppName: function(event) {
        this.setState({WebAppName:event.target.value});
    },
    componentDidMount: function() {
        if (this.validator === null) {
            this.validator = $(this.getDOMNode().children[0]).validate({ignore: ":hidden"});
        }
    },
    componentDidUpdate: function() {
        if (this.props.show === true) {
            var okCallback = this.props.data === undefined ? this.insertNewWebApp : this.updateWebApp;
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
    insertNewWebApp: function() {
        var self = this;
        if (this.isValid()) {
            this.props.service.insertNewWebApp($(this.getDOMNode().children[0]).serializeArray(),
                                              function(){
                                                self.props.successCallback();
                                                $(self.getDOMNode()).dialog("destroy");
                                              },
                                              function(errMsg) {
                                                    $.errorAlert(errMsg, "Error");
                                              });
        }
    },
    updateWebApp: function() {
        var self = this;
        if (this.isValid()) {
            this.props.service.updateWebApp($(this.getDOMNode().children[0]).serializeArray(),
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

var WebAppDataTable = React.createClass({
   shouldComponentUpdate: function(nextProps, nextState) {

      return !nextProps.noUpdateWhen;

    },
    render: function() {
        var tableDef = [
                         {sTitle:"WebApp ID", mData:"id.id", bVisible:false},
                         {sTitle:"WebApp Name", mData:"name", tocType:"link"},
                         {sTitle:"Context", mData:"webAppContext"},
                         {sTitle:"Web Archive", mData:"warPath"},
                         {sTitle:"Group ID", mData:"group.id.id", bVisible:false},
                         {sTitle:"Group", mData:"group.name"},
                        ];
        return <TocDataTable tableId="WebAppDataTable"
                             tableDef={tableDef}
                             colHeaders={["...", "..."]}
                             data={this.props.data}
                             selectItemCallback={this.props.selectItemCallback}
                             editCallback={this.props.editCallback}
                             expandIcon="public-resources/img/react/components/details-expand.png"
                             collapseIcon="public-resources/img/react/components/details-collapse.png"
                             rowSubComponentContainerClassName="row-sub-component-container"/>
    }
});