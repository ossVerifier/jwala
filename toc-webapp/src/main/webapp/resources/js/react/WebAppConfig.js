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
                                    <RDataTable colDefinitions={[{key: "secure", renderCallback: this.renderSecureColumn, sortable: false},
                                                                 {title: "WebApp Name", key: "name", renderCallback: this.renderWebAppNameCallback},
                                                                 {title: "Context", key: "webAppContext"},
                                                                 {title: "Web Archive", key: "warName"},
                                                                 {key: "id.id", renderCallback: this.renderUploadIcon, sortable: false},
                                                                 {title: "Group", key:"group.name"}]}
                                                data={this.state.WebAppTableData}/>
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

                  <ModalDialogBox title="Upload WAR"
                                  show={false}
                                  ref="uploadWarDlg"
                                  cancelCallback={this.uploadWarDlgCancelCallback}
                                  cancelLabel="Close"/>
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
    renderUploadIcon: function(id) {
        var self = this;
        return <RButton title="Upload WAR" className="iconBtn ui-widget ui-state-default ui-corner-all ui-button-text-only ui-button-height"
                        hoverClassName="ui-state-hover" spanClassName="ui-icon ui-icon-arrowthick-1-n" onClick={function(){self.onUploadWarBtnClicked(id)}} />
    },
    onUploadWarBtnClicked: function(id) {

        var self = this;
        this.state.WebAppTableData.forEach(function(data){
            if (data.id.id === id) {
                self.refs.uploadWarDlg.show("Upload WAR", <WARUpload service={self.props.service} war={data.warPath}
                                                                     full={data} />);
                return;
            }
        });


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
        if (selectedWebApp != undefined) {
            this.setState({showDeleteConfirmDialog: true});
        }
    },
    selectItemCallback: function(item) {
        selectedWebApp = item;
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
            GroupId: "",
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
            GroupId:this.getWebAppGroupIdProp(nextProps, "id"),
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
                                <DataCombobox name="groupId"
                                              data={this.props.groupSelectData}
                                              selectedVal={this.state.GroupId}
                                              dataField="id.id"
                                              val="name"
                                              onChange={this.onSelectGroup}/>
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

var WebAppDataTable = React.createClass({
   shouldComponentUpdate: function(nextProps, nextState) {

      return !nextProps.noUpdateWhen;

    },
    render: function() {
        var tableDef = [
                         {sTitle:"WebApp ID", mData:"id.id", bVisible:false},
                         {sTitle:"", mData:"secure", tocType:"custom", tocRenderCfgFn: this.renderSecureCol},
                         {sTitle:"WebApp Name", mData:"name", tocType:"custom", tocRenderCfgFn:this.renderNameLink},
                         {sTitle:"Context", mData:"webAppContext"},
                         {sTitle:"Web Archive", mData:"warPath", tocType:"custom", tocRenderCfgFn: this.renderRowData },
                         {sTitle:"Group ID", mData:"group.id.id", bVisible:false},
                         {sTitle:"Group", mData:"group.name"}
                        ];
        return <TocDataTable tableId="WebAppDataTable"
                             className="dataTable"
                             tableDef={tableDef}
                             colHeaders={["...", "..."]}
                             data={this.props.data}
                             selectItemCallback={this.props.selectItemCallback}
                             editCallback={this.props.editCallback}
                             rowSubComponentContainerClassName="row-sub-component-container"
                             isColResizable={true}/>
    },


    renderRowData:function(dataTable, data, aoColumnDefs, itemIndex) {
          dataTable.expandCollapseEnabled = true;
          aoColumnDefs[itemIndex].mDataProp = null;
          aoColumnDefs[itemIndex].sClass = "control textAlignLeft";
          aoColumnDefs[itemIndex].bSortable = false;
          aoColumnDefs[itemIndex].fnCreatedCell = function ( nTd, sData, oData, iRow, iCol ) {
                  var data = sData;
                  var full = oData;
                  var parentItemId = (dataTable.parentItemId === undefined ? full.id.id : dataTable.parentItemId);
                  var theRootId = (dataTable.rootId === undefined ? full.id.id : dataTable.rootId);

                  if(data != null && full != null) {
                    return React.renderComponent(
                      <WARUpload service={this.props.service} war={sData} full={oData} row={iRow} />
                      ,nTd
                    );
                  } else {
                    return ;
                  }
          }.bind(this);

          aoColumnDefs[itemIndex].mRender = function (data, type, full) {

                  return "<div />";
                }.bind(this);
    },

    renderSecureCol: function(dataTable, data, aoColumnDefs, itemIndex) {
        aoColumnDefs[itemIndex].mDataProp = null;
        aoColumnDefs[itemIndex].sClass = "control textAlignLeft";
        aoColumnDefs[itemIndex].bSortable = false;
        aoColumnDefs[itemIndex].mRender = function (data, type, full) {
            if (data) {
                return "<span class='ui-icon ui-icon-locked'></span>";
            }
            return "<span class='ui-icon ui-icon-unlocked'></span>";
        }.bind(this);
    },
    renderNameLink:function(dataTable, data, aoColumnDefs, itemIndex) {
        var self = this;
            aoColumnDefs[itemIndex].fnCreatedCell = function ( nTd, sData, oData, iRow, iCol ) {
                var MAX_VAL_LEN = 50;
                var val = sData;
                var title = "";
                if (val.length > MAX_VAL_LEN) {
                    title = sData;
                    val = val.substring(0, MAX_VAL_LEN) + "...";
                }

                return React.renderComponent(React.createElement("button", {className:"button-link", title:title}, val), nTd, function() {
                                $(this.getDOMNode()).click(oData, self.props.editCallback);
                            });

            };
    }

});

// TODO: Remove stale code.
var WARUpload = React.createClass({
    stripPathRegEx: /(([A-Z]{1}:)?[/\\]?([^/\\]*))(-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})(.war)/,
    getInitialState: function() {
        var noArchive = "No Web Archive";
        var hasWar = (this.props.full.warPath !== undefined && this.props.full.warPath !== null && this.props.full.warPath != "" );
        return {
          editable : false,
          uploadData: null,
          showDeleteConfirmDialog: false,
          hasWar: hasWar,
          noArchive : noArchive,
          warPath: hasWar ? this.props.full.warPath : noArchive,
          /* warName: hasWar ? this.stripPathRegEx.exec(this.props.full.warPath)[3] : noArchive, */
        }
    },
    componentWillUpdate: function(nextProps, nextState) {
      // nextState.warName = nextState.hasWar ? this.stripPathRegEx.exec(nextState.warPath)[3] : this.state.noArchive;
    },
    componentDidMount: function() {
        this.initFileUpload();
    },
    shouldComponentUpdate: function(nextProps, nextState) {
        return nextState.showDeleteConfirmDialog || !(nextState.uploadData && nextState.uploadData);
    },
    render: function() {
        var progressStyle = {clear: 'left', height: '10px', width: '100%', paddingTop: '0.5em'};
        var progressStyleInner = {clear: 'left', height: '10px', width: '0%', backgroundColor: 'green'};

        return <div className="war-upload-component">
                   <div className="archive-file">
                       <div className="file-upload">
                           <form ref="warUploadForm" encType='multipart/form-data' method="POST" action={'v1.0/applications/' + this.props.full.id.id + '/war'}>
                               <div className="fileUploadContainer">
                                   <input ref="fileInput" type="file" name="file"></input>
                                   <input type="button" value="Upload" onClick={this.performUpload} />
                               </div>
                               <div style={progressStyle}>
                                   <div ref="progressBar" style={progressStyleInner} className="progress" />
                               </div>
                               <span className="uploadResult" />
                           </form>
                        </div>
                   </div>
               </div>;
    },

    initFileUpload :function() {
        var self = this;
        var d = new Date();
        $(this.refs.fileInput.getDOMNode()).fileupload({
            dataType: "json",
            url: this.props.service.baseUrl + "/" + this.props.full.id.id + "/war" + "?_"+ d.getTime(),
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
    progressClean: function() {
      var thisForm = $(this.refs.warUploadForm.getDOMNode());
      var thisResult = $('span.uploadResult', thisForm);
      thisResult.removeClass('error');
      thisResult.addClass('ok');
      thisResult.text('');
    },
    progressError: function(errorMsg, progress) {
      var thisForm = $(this.refs.warUploadForm.getDOMNode());
      var thisProgress = $('div.progress', thisForm);
      thisProgress.css({
          'width' : (progress !== undefined ? progress : 100) + '%',
          'background-color' : 'red'
      });
      var thisResult = $('span.uploadResult', thisForm);
      thisResult.addClass('error');
      thisResult.removeClass('ok');
      thisResult.text(errorMsg);
    },
    progressOk: function(okMsg) {
      var thisForm = $(this.refs.warUploadForm.getDOMNode());
      var thisProgress = $('div.progress', thisForm);
      thisProgress.css({
          'background-color' : 'green'
      });
      var thisResult = $('span.uploadResult', thisForm);
      thisResult.addClass('ok');
      thisResult.removeClass('error');
      thisResult.text(okMsg);
    },
    performUpload: function(event) {
        var self = this;
        if (this.state.uploadData !== null) {
            // Note: Don't confuse with "form" submit. Please see initFileUpload.
            this.state.uploadData.submit().success(function(result, textStatus, jqXHR) {
                if (!result || result.msgCode !== "0" || !result.applicationResponseContent) {
                     self.progressError((result.msgCode||"AEM")+ ": " + (result.applicationResponseContent||textStatus));
                }
            }).error (function(result, textStatus, jqXHR) {
                if (result.responseJSON !== undefined) {
                    self.progressError((result.responseJSON.msgCode||"AEM")+ ": " + (result.responseJSON.applicationResponseContent||textStatus));
                } else if ( textStatus ) {
                    self.progressError("AEM: " + textStatus.substring(0,100));
                } else {
                    self.progressError("AEM: Please retry your upload. ");
                };
            });
        } else {
            $(this.refs.fileInput.getDOMNode()).effect("highlight")
        }

        event.preventDefault();
        return false;
    },
    editRequest : function(event) {
        var td = $(this.getDOMNode()).parent().parent().find("td");
        if (td.hasClass("vertical-align-top")) {
            td.removeClass("vertical-align-top");
        } else {
            td.addClass("vertical-align-top");
        }

      this.setState({ editable: !this.state.editable, uploadData: undefined });

    },
    postUploadWarForm : function(event, data) {
      var thisForm = $(this.refs.warUploadForm.getDOMNode());
      this.props.service.postUploadWarForm(this.props.full.id.id, thisForm);
      event.preventDefault();
      return false;
    },
    handleDelete: function() {
        this.setState({showDeleteConfirmDialog: true});
    },
    confirmDeleteCallback: function() {
        var self = this;
        try {
          self.setState({showDeleteConfirmDialog: false});
          self.props.service.deleteWar(self.props.full.id.id).then( function() {
              self.setState({
                hasWar: false,
                warPath: self.state.noArchive
            });
          });
        } finally {
        }
    },
    cancelDeleteCallback: function() {
        this.setState({showDeleteConfirmDialog:false});
    }
});
