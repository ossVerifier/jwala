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
                                                {title: "Name", key: "name", renderCallback: this.onMediaNameClick},
                                                {title: "Path", key: "localPath"},
                                                {title: "Type", key: "type"},
                                                {title: "Remote Target Directory", key: "remoteDir"},
                                                {title: "Media Directory Name", key: "mediaDir"}]}
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
        mediaService.getAllMedia((function(response){
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
            ServiceFactory.getMediaService().createMedia($(this.refs.mediaAddForm.refs.form.getDOMNode()).serializeArray())
            .then(function(response){
                self.refs.modalAddMediaDlg.close();
                self.loadTableData();
            }).caught(function(response){
                $.errorAlert(JSON.parse(response.responseText).message);
            });
        }
    },
    okEditCallback: function() {
        var self = this;
        if (this.refs.modalEditMediaDlg.refs.mediaEditForm.isValid()) {
            var serializedData = $(this.refs.modalEditMediaDlg.refs.mediaEditForm.refs.form.getDOMNode()).serializeArray();
            // TODO implement service call
            mediaService.updateMedia(serializedData,
                                            function(response){
                                                self.refs.modalEditMediaDlg.close();
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
            this.refs.confirmDeleteMediaDlg.show("Delete Media", "Are you sure you want to delete " +
                                                 this.state.selectedMedia["str-name"] + " ?");
        }
    },
    confirmDeleteCallback: function() {
        var self = this;
        ServiceFactory.getMediaService().deleteMedia(this.state.selectedMedia["str-name"]).then(function(response){
            self.refs.confirmDeleteMediaDlg.close();
            self.loadTableData(function(){
                self.state.selectedMedia = null;
            });
        }).caught(function(response){
            $.errorAlert(response);
        });
    },
    editMediaDlg: function(name) {
          var self = this;
              mediaService.getMedia(name, (function(response){
                                           var formData = {};
                                                 formData["name"] = response.applicationResponseContent.name;
                                                 formData["type"] = response.applicationResponseContent.type;
                                                 formData["localPath"] = response.applicationResponseContent.path;
                                                 formData["remoteDir"] = response.applicationResponseContent.remoteHostPath;
                                                         self.refs.modalEditMediaDlg.show("Edit Media",
                                                             <MediaConfigForm formData={formData}/>);
                                                }));




    },
    onMediaNameClick: function(name) {
        var self = this;
        return <button className="button-link" onClick={function(){self.editMediaDlg(name)}}/*{this.editMediaDlg.bind(this, name)}*/>{name}</button>
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
                       <label>Name</label>
                       <label htmlFor="name" className="error"/>
                       <input name="name" type="text" valueLink={this.linkState("name")} maxLength="255" required/>
                       <label>Type</label>
                       <label htmlFor="type" className="error"/>
                       <input name="type" type="text" valueLink={this.linkState("type")} required maxLength="255"/>
                       <label>Path</label>
                       <label htmlFor="localPath" className="error"/>
                       <input name="localPath" type="text" valueLink={this.linkState("localPath")} required maxLength="255"/>
                       <label>Remote Directory</label>
                       <label htmlFor="remoteDir" className="error"/>
                       <input name="remoteDir" type="text" valueLink={this.linkState("remoteDir")} required maxLength="255"/>
                       <label>Media Directory</label>
                       <label htmlFor="mediaDir" className="error"/>
                       <input name="mediaDir" type="text" valueLink={this.linkState("mediaDir")} required maxLength="255"/>
                   </form>
               </div>
    },
    validator: null,
    componentDidMount: function() {
        var self = this;
        if (self.validator === null) {
            self.validator = $(self.refs.form.getDOMNode()).validate();
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
    }
});