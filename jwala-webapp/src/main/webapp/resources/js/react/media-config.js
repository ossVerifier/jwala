/** @jsx React.DOM */
var MediaConfig = React.createClass({
    getInitialState: function() {
        return {selectedMedia: null};
    },
    render: function() {
        return <div className="MediaConfig">
                   <div className="btnContainer">
                       <GenericButton label="Delete" accessKey="d" callback={this.delBtnCallback}/>
                       <GenericButton label="Add" accessKey="a" callback={this.addBtnCallback}/>
                   </div>

                   <RDataTable ref="dataTable"
                               tableIndex="id"
                               colDefinitions={[{key: "id", isVisible: false},
                                                {title: "Type", key: "type"},
                                                {title: "Path", key: "path"},
                                                {title: "Name", key: "name"},
                                                {title: "Remote Host Path", key: "remoteHostPath"}]}
                               selectItemCallback={this.selectItemCallback}
                               deselectAllRowsCallback={this.deselectAllRowsCallback}/>

                   <ModalDialogBox ref="modalAddMediaDlg"
                                   title="Add Media"
                                   okCallback={this.okAddCallback}
                                   content={<MediaConfigForm ref="mediaAddForm"/>}/>

                   <ModalDialogBox ref="modalEditMediaDlg"
                                   title="Edit Media"
                                   contentReferenceName="mediaEditForm"
                                   okCallback={this.okEditCallback}/>

                   <ModalDialogBox ref="confirmDeleteMediaDlg"
                                   okLabel="Yes"
                                   okCallback={this.confirmDeleteCallback}
                                   cancelLabel="No"/>
               </div>;
    },
    componentDidMount: function() {
        this.loadTableData();
    },
    loadTableData: function(afterLoadCallback) {
        var self = this;
        // TODO implement service call
        mediaService.getMedia((function(response){
                                          self.refs.dataTable.refresh(response.applicationResponseContent);
                                          if ($.isFunction(afterLoadCallback)) {
                                            afterLoadCallback();
                                          }
                                      }));
    },
    addBtnCallback: function() {
        this.refs.modalAddMediaDlg.show();
    },
    okAddCallback: function() {
        var self = this;
        if (this.refs.mediaAddForm.isValid()) {
            var serializedData = $(this.refs.mediaAddForm.refs.form.getDOMNode()).serializeArray();
            // TODO implement service call
             this.props.service.insertNewMedia(
                serializedData,
                function(){
                    self.refs.modalAddWebAppDlg.close();
                    self.loadTableData(function(){
                        self.refs.dataTable.deselectAllRows();
                    });
                },
                function(errMsg){
                    $.errorAlert(errMsg, "Error");
                });
        }
    },
    okEditCallback: function() {
        var self = this;
        if (this.refs.modalEditMediaDlg.refs.mediaEditForm.isValid()) {
            var serializedData = $(this.refs.modalEditMediaDlg.refs.mediaEditForm.refs.form.getDOMNode()).serializeArray();
            // TODO implement service call
            this.props.service.updateMedia(serializedData,
                                            function(response){
                                                self.refs.modalEditWebAppDlg.close();
                                                self.loadTableData(function(){
                                                    self.state.selectedMedia = self.refs.dataTable.getSelectedItem();
                                                });
                                            },
                                            function(errMsg) {
                                                $.errorAlert(errMsg, "Error");
                                            });
        }
    },
    selectItemCallback: function(item) {
        this.state.selectedMedia = item;
    },
    deselectAllRowsCallback: function() {
        this.state.selectedMedia = null;
    },
    delBtnCallback: function() {
        if (this.state.selectedMedia) {
            this.refs.confirmDeleteMediaDlg.show("Delete Media", "Are you sure you want to delete " + this.state.selectedMedia["str-name"] + " ?");
        }
    },
    confirmDeleteCallback: function() {
        var self = this;
        // TODO implement service call
        mediaService.deleteMedia(this.state.selectedMedia.id.id).then(function(){
            self.refs.confirmDeleteWebAppDlg.close();
            self.loadTableData(function(){
                self.state.selectedMedia = null;
            });
        });
    },
    onMediaNameClick: function(name) {
        var self = this;
        // TODO implement service call
//        ServiceFactory.getWebAppService().getWebAppByName(name).then(function(response){
//            var formData = {};
//            formData["id"] = response.applicationResponseContent.id;
//            formData["name"] = response.applicationResponseContent.name;
//            formData["context"] = response.applicationResponseContent.webAppContext;
//            formData["groupIds"] = [response.applicationResponseContent.group.id];
//            formData["secure"] = response.applicationResponseContent.unpackWar;
//            formData["unpackWar"] = response.applicationResponseContent.unpackWar;
//            formData["loadBalance"] = response.applicationResponseContent.loadBalance;
//            formData["loadBalanceAcrossServers"] = response.applicationResponseContent.loadBalanceAcrossServers;
//            self.refs.modalEditWebAppDlg.show("Edit Web Application",
//                <WebAppConfigForm formData={formData}/>);
//        });
    }
})

var MediaConfigForm = React.createClass({
    mixins: [
        React.addons.LinkedStateMixin
    ],
    getInitialState: function() {
        var name = this.props.formData && this.props.formData.name ? this.props.formData.name : null;
        var type = this.props.formData && this.props.formData.type ? this.props.formData.type : null;
        var path = this.props.formData && this.props.formData.path ? this.props.formData.path : null;
        var remoteHostPath = this.props.formData && this.props.formData.remoteHostPath ? this.props.formData.remoteHostPath : null;
        return {name: name, type: type, path: path, remoteHostPath: remoteHostPath};
    },
    render: function() {
        var idTextHidden = null;
        if (this.props.formData && this.props.formData.id) {
            idTextHidden = <input type="hidden" name="formId" value={this.props.formData.id.id}/>;
        }

        return <div>
                   <form ref="form">
                       {idTextHidden}
                       <label>Name</label><br/>
                       <label htmlFor="name" className="error"/>
                       <input name="name" type="text" valueLink={this.linkState("name")} maxLength="255" required className="width-max"/>
                       <label>Path</label><br/>
                       <label htmlFor="path" className="error"/>
                       <input name="path" type="text" valueLink={this.linkState("path")} required maxLength="255" className="width-max"/>
                       <label>Type</label><br/>
                       <label htmlFor="type" className="error"/>
                       <input name="type" type="text" valueLink={this.linkState("type")} required maxLength="255" className="width-max"/>
                       <label>Remote Host Path</label>
                       <label htmlFor="remoteHostPath" className="error"/>
                       <input name="remoteHostPath" type="text" valueLink={this.linkState("remoteHostPath")} required maxLength="255" className="width-max"/>
                   </form>
               </div>
    },
    validator: null,
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
    }
});