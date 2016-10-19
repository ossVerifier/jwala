/** @jsx React.DOM */
var WebAppConfig = React.createClass({
    getInitialState: function() {
        return {
            showModalFormAddDialog: false,
            showModalFormEditDialog: false,
            WebAppFormData: {},
            WebAppTableData: [],
            groupSelectData: [],
            selectedWebApp: null
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
                                    <GenericButton label="Delete" accessKey="d" callback={this.delBtnCallback}/>
                                    <GenericButton label="Add" accessKey="a" callback={this.addBtnCallback}/>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <div>
                                    <RDataTable colDefinitions={[{key: "id", isVisible: false},
                                                                 {key: "secure", renderCallback: this.renderSecureColumn, sortable: false},
                                                                 {title: "WebApp Name", key: "name", renderCallback: this.renderWebAppNameCallback},
                                                                 {title: "Context", key: "webAppContext"},
                                                                 {title: "Web Archive", key: "warName"},
                                                                 {key: "actionIcons", renderCallback: this.renderActionIcons, sortable: false},
                                                                 {title: "Group", key:"group.name"}]}
                                                data={this.state.WebAppTableData}
                                                selectItemCallback={this.selectItemCallback} />
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
                                      this.state.showModalFormAddDialog
                                  }/>

                  <ModalDialogBox ref="confirmDeleteWebAppDlg"
                                  okLabel="Yes"
                                  okCallback={this.confirmDeleteCallback}
                                  cancelLabel="No"/>

                  <ModalDialogBox title="Upload WAR"
                                  ref="uploadWarDlg"
                                  okLabel="Upload"
                                  okCallback={this.uploadCallback}
                                  cancelCallback={this.uploadWarDlgCancelCallback}
                                  cancelLabel="Close"
                                  contentReferenceName="uploadWarWidget" />

                  <ModalDialogBox ref="confirmDeleteWarDlg"
                                  okLabel="Yes"
                                  okCallback={this.deleteWarCallback}
                                  cancelLabel="No"/>

               </div>
    },
    renderWebAppNameCallback: function(name) {
        var self = this;
        return <button className="button-link" onClick={function(){self.onWebAppNameClick(name)}}>{name}</button>
    },
    onWebAppNameClick: function(name) {
        var self = this;
        this.state.WebAppTableData.forEach(function(data){
            if (data.name === name) {
                console.log(data.id);
                self.props.service.getWebApp(data.id.id,
                    function(response){
                        self.setState({WebAppFormData: response.applicationResponseContent,
                                       showModalFormEditDialog: true})
                    }
                );
                return;
            }
        });
    },
    renderSecureColumn: function(secure) {
        if (secure) {
            return <span className="ui-icon ui-icon-locked"></span>;
        }
        return <span className="ui-icon ui-icon-unlocked"></span>;
    },
    renderActionIcons: function(id, data) {
        var self = this;
        var uploadBtn = data.warName ? null : <RButton title="Upload WAR" className="upArrowIconBtn ui-widget ui-state-default ui-corner-all ui-button-text-only ui-button-height"
                                                       hoverClassName="ui-state-hover" spanClassName="ui-icon ui-icon-arrowthick-1-n" onClick={function(){self.onUploadWarBtnClicked(data)}} />
        var deleteBtn = data.warName ? <RButton title="Delete WAR" className="trashIconBtn ui-widget ui-state-default ui-corner-all ui-button-text-only ui-button-height"
                                                hoverClassName="ui-state-hover" spanClassName="ui-icon ui-icon-trash" onClick={function(){self.onDeleteWarBtnClicked(data)}} /> : null;
        return <div>
                   {uploadBtn}
                   {deleteBtn}
               </div>
    },
    onUploadWarBtnClicked: function(data) {
        this.refs.uploadWarDlg.show("Upload WAR", <UploadWarWidget service={this.props.service} data={data}
                                                                   afterUploadCallback={this.afterUploadCallback} />);
    },
    uploadCallback: function() {
        this.refs.uploadWarDlg.refs.uploadWarWidget.performUpload();
    },
    afterUploadCallback: function(files) {
        this.retrieveData();
        this.refs.uploadWarDlg.close();
        $.alert(files[0].name + " upload successful!");
    },
    onDeleteWarBtnClicked: function(data) {
        var self = this;
        this.refs.confirmDeleteWarDlg.show("Delete WAR", "Are you sure you want to delete " + data.warName + " ?",
                                           function(){self.deleteWarCallback(data)});
    },
    deleteWarCallback: function(data) {
        var self = this;
        this.props.service.deleteWar(data.id.id).then(function(){
            self.refs.confirmDeleteWarDlg.close();
            self.retrieveData();
        });
    },
    confirmDeleteCallback: function() {
        var self = this;
        this.props.service.deleteWebApp(this.state.selectedWebApp.id.id).then(function(){
            self.refs.confirmDeleteWebAppDlg.close();
            self.retrieveData();
        });
    },
    retrieveData: function() {
        var self = this;
        this.props.service.getWebApps(function(response){
                                        self.setState({WebAppTableData:response.applicationResponseContent});
                                     });
        this.props.groupService.getGroups().then(
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
        if (this.state.selectedWebApp) {
            this.refs.confirmDeleteWebAppDlg.show("Confirm delete Web App", "Are you sure you want to delete " +
                                                  this.state.selectedWebApp["str-name"] + " ?");
        }
    },
    selectItemCallback: function(item) {
        this.state["selectedWebApp"] = item; // let's not use setState because we don't want to trigger re rendering
    },
    editCallback: function(e) {
        var thisComponent = this;
        this.props.service.getWebApp(e.data.id.id,
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
    mixins: [
      React.addons.LinkedStateMixin
    ],
    validator: null,
    shouldComponentUpdate: function(nextProps, nextState) {
        return !nextProps.noUpdateWhen;
    },
    getInitialState: function() {
        return {
            WebAppId: "",
            WebAppName: "",
            WebAppContext: "",
            groupIds: [],
            secure: true,
            unpackWar: false,
            loadBalanceAcrossServers: true
        }
    },
    getWebAppIdProp: function(props) {
        if (props.data !== undefined && props.data.id !== undefined) {
            return props.data.id.id;
        }
        return "";
    },
    getWebAppProp: function(props, name, defaultVal) {
        if (props.data !== undefined) {
            return props.data[name];
        }
        return defaultVal;
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
            WebAppName:this.getWebAppProp(nextProps, "name", ""),
            WebAppContext:this.getWebAppProp(nextProps, "webAppContext", ""),
            groupIds:[{id: this.getWebAppGroupIdProp(nextProps, "id")}],
            GroupName:this.getWebAppGroupProp(nextProps, "name"),
            secure:this.getWebAppProp(nextProps, "secure", true),
            unpackWar:this.getWebAppProp(nextProps, "unpackWar", false),
            loadBalanceAcrossServers:this.getWebAppProp(nextProps, "loadBalanceAcrossServers", true)
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
                                <td>
                                    <label htmlFor="name" className="error"></label>
                                </td>
                            </tr>
                            <tr>
                                <td><input name="name"
                                           type="text"
                                           value={this.state.WebAppName}
                                           onChange={this.onChangeWebAppName}
                                           maxLength="255"
                                           required
                                           className="width-max"/></td>
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
                                <td><input name="webappContext" type="text" valueLink={this.linkState("WebAppContext")}
                                           required maxLength="255" className="width-max"/></td>
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
                                 <DataMultiSelectBox name="groupId"
                                                     data={this.props.groupSelectData}
                                                     selectedValIds={this.state.groupIds}
                                                     dataField="id.id"
                                                     val="name"
                                                     className="data-multi-select-box"
                                                     onSelectCallback={this.onSelectGroups}
                                                     idKey="groupId"
                                                     singleSelect={true}/>
                                </td>
                            </tr>
                            <tr>
                                <td>Secure</td>
                            </tr>
                            <tr>
                                <td>
                                    <input name="secure" type="checkbox" checked={this.state.secure} onChange={this.onSecureCheckboxChanged}/>
                                </td>
                            </tr>

                            <tr>
                                <td>Unpack War</td>
                            </tr>
                            <tr>
                                <td>
                                    <input name="unpackWar" type="checkbox" checked={this.state.unpackWar} onChange={this.onUnpackWarCheckboxChanged}/>
                                </td>
                            </tr>

                            <tr>
                                <td>Load Balance</td>
                            </tr>
                            <tr>
                                <td>
                                    <input type="radio"
                                           name="loadBalance"
                                           value="acrossServers"
                                           checked={this.state.loadBalanceAcrossServers}
                                           onClick={this.onLbAcrossServersCheckboxChanged}>Across Servers</input>
                                    <input type="radio"
                                           name="loadBalance"
                                           value="locally"
                                           checked={!this.state.loadBalanceAcrossServers}
                                           onClick={this.onLbLocallyCheckboxChanged}>Local Only</input>
                                </td>
                            </tr>

                        </table>
                    </form>
               </div>
    },
    onSecureCheckboxChanged: function() {
        this.setState({secure:!this.state.secure});
    },
    onUnpackWarCheckboxChanged: function() {
        this.setState({unpackWar:!this.state.unpackWar});
    },
    onLbAcrossServersCheckboxChanged: function() {
        this.setState({loadBalanceAcrossServers: true});
    },
    onLbLocallyCheckboxChanged: function() {
        this.setState({loadBalanceAcrossServers: false});
    },
    onSelectGroups: function(groupIds) {
        this.setState({groupIds: groupIds});
    },
    onChangeWebAppName: function(event) {
        this.setState({WebAppName:event.target.value});
    },
    componentDidMount: function() {
        if (this.validator === null) {
            this.validator = $(this.getDOMNode().children[0])
                .validate({ignore: ":hidden", rules: {"groupId": {required: true}},
                           messages: {
                               "groupId": {
                                   required: "Please select at least 1 group"
                               }
                           }});
        }
    },
    componentDidUpdate: function() {
        if (this.props.show === true) {

            // Check first if this component has been decorated already.
            // Decorate only once!
            if (!$(this.getDOMNode()).hasClass("ui-dialog-content")) {
                var okCallback = this.props.data === undefined ? this.insertNewWebApp : this.updateWebApp;
                decorateNodeAsModalFormDialog(this.getDOMNode(),
                                              this.props.title,
                                              okCallback,
                                              this.destroy,
                                              this.destroy);
            }

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
            return true;
        }
        return false;
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
            return true;
        }
        return false;
    },
    destroy: function() {
        this.validator.resetForm();
        $(this.getDOMNode()).dialog("destroy");
        this.props.destroyCallback();
    }
});


/**
 * Upload WAR widget.
 */
var UploadWarWidget = React.createClass({
    getInitialState: function() {
        return {
            properties: {},
            showProperties: false,
            deployPath: "",
            uploadData: null
        }
    },
    render: function() {
        var self = this;

        var rows = [];
        for (var key in this.state.properties) {
            if (UploadWarWidget.isPossiblePath(this.state.properties[key]) && UploadWarWidget.isEncrypted(this.state.properties[key])) {
                rows.push(<PropertyRow key={key} onAddPropertiesClickCallback={self.onAddPropertiesClick}
                                       propertyName={key} propertyValue={this.state.properties[key]} />);
            }
        }

        var propertiesTable = <div><table ref="propertiesTable">{rows}</table></div>;

        return <div className="war-upload-component">
                  <div className="archive-file">
                      <div className="file-upload">
                          <form ref="warUploadForm" encType='multipart/form-data' method="POST" action={'v1.0/applications/' + this.props.data.id.id + '/war'}>
                              <div className="fileUploadContainer">
                                  <input ref="fileInput" type="file" name="file"></input>
                              </div>
                              <div>Deploy Path</div>
                              <div className="deployPath">
                                  <input ref="deployPathInput" name="deployPath" value={this.state.deployPath} onChange={this.onDeployPathChanged} />
                                  <div className="openCloseProperties">
                                      <span ref="openClosePropertiesIcon" className={this.state.showProperties ? "ui-icon ui-icon-triangle-1-s" : "ui-icon ui-icon-triangle-1-e"}
                                            onClick={this.openClosePropertiesIconCallback} />
                                      <span>Properties</span>
                                      {this.state.showProperties ? propertiesTable : null}
                                  </div>
                              </div>
                          </form>
                          <span ref="uploadResult" />
                          <div className="progressBar">
                            <div ref="progressBar" className="inner" />
                          </div>
                       </div>
                  </div>
              </div>;
    },
    componentDidMount: function() {
        this.initFileUpload();
    },
    openClosePropertiesIconCallback: function() {
        if (this.state.showProperties) {
            this.setState({showProperties: false});
        } else {
            ServiceFactory.getAdminService().reloadProperties(this.onPropertiesLoad);
        }
    },
    onPropertiesLoad: function(response) {
        this.setState({properties: response.applicationResponseContent, showProperties: true});
    },
    onDeployPathChanged: function(e) {
        this.setState({deployPath: $(this.refs.deployPathInput.getDOMNode()).val()});
    },
    onAddPropertiesClick: function(val) {
        this.setState({deployPath: this.state.deployPath + val});
    },
    initFileUpload :function() {
        var self = this;
        var d = new Date();
        $(this.refs.fileInput.getDOMNode()).fileupload({
            dataType: "json",
            url: "v1.0/applications/" + this.props.data.id.id + "/war" + "?_"+ d.getTime(),
            forceIframeTransport: false,
            replaceFileInput: false,
            add: function(e, data) {
                self.setState({uploadData: data});
            },
            progressall: function(e, data) {
                var progress = parseInt(data.loaded / data.total * 100, 10);
                $(self.refs.progressBar.getDOMNode()).css({'width' : progress + '%', 'background-color' : 'green'});
            }
        });
    },
    performUpload: function() {
        var self = this;
        if (this.state.uploadData !== null) {
            // Note: Don't confuse with "form" submit. Please see initFileUpload.
            this.state.uploadData.submit().success(function(result, textStatus, jqXHR) {

                if (result && result.msgCode === "0") {
                    if ($.isFunction(self.props.afterUploadCallback)) {
                        self.props.afterUploadCallback(self.refs.fileInput.getDOMNode().files);
                    }
                } else {
                    self.progressError("Error has occurred! Please check browser console logs for details.");
                    console.log(result);
                    console.log(textStatus);
                }

            }).error (function(result, textStatus, jqXHR) {
                console.log(result);
                self.progressError(JSON.parse(result.responseText).applicationResponseContent);
            });
        } else {
            $(this.refs.fileInput.getDOMNode()).effect("highlight")
        }
    },
    progressError: function(errorMsg, progress) {

      $(this.refs.progressBar.getDOMNode()).css({
          'width' : (progress !== undefined ? progress : 100) + '%',
          'background-color' : 'red'
      });

      var uploadResult = $(this.refs.uploadResult.getDOMNode());
      uploadResult.addClass("error");
      uploadResult.removeClass("ok");
      uploadResult.text(errorMsg);
    },
    statics: {
        isPossiblePath: function(path) {
            return path.indexOf(":") > -1 || path.indexOf("\\") > -1 || path.indexOf("/") > -1;
        },
        isEncrypted: function(val) {
            return val.charAt(val.length - 1) !== '=';
        }
    }
});

var PropertyRow = React.createClass({
    render: function() {
        var self = this;
        return <tr>
                   <td><span className="ui-icon ui-icon-plus" onClick={function(){self.props.onAddPropertiesClickCallback("${vars['" + self.props.propertyName + "']}")}} /></td>
                   <td><span className="key">{self.props.propertyName}</span>: {self.props.propertyValue}</td>
               </tr>;
    }
});
