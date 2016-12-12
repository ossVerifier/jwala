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
                                                {title: "Name", key: "name", renderCallback: this.mediaNameRenderCallback},
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
        mediaService.getAllMedia().then((function(response){
                                             self.refs.dataTable.refresh(response.applicationResponseContent);
                                             if ($.isFunction(afterLoadCallback)) {
                                                 afterLoadCallback();
                                             }
                                         })).caught(function(response){
                                             $.errorAlert(response);
                                         });
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
            ServiceFactory.getMediaService().updateMedia($(this.refs.modalEditMediaDlg.refs.mediaEditForm.refs.form.getDOMNode()).serializeArray())
            .then(function(response){
                self.refs.modalEditMediaDlg.close();
                self.loadTableData(function(){
                    self.state.selectedMedia = self.refs.dataTable.getSelectedItem();
                });
            }).caught(function(response){
                $.errorAlert(JSON.parse(response.responseText).message);
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
    mediaNameRenderCallback: function(name, media) {
        var self = this;
        return <button className="button-link" onClick={function(){self.onClickMediaNameLink(media.id)}}>{name}</button>
    },
    onClickMediaNameLink: function(id) {
        var self = this;
        mediaService.getMediaById(id).then((function(response){
                                                var formData = {};
                                                formData["id"] = response.applicationResponseContent.id;
                                                formData["name"] = response.applicationResponseContent.name;
                                                formData["type"] = response.applicationResponseContent.type;
                                                formData["localPath"] = response.applicationResponseContent.localPath;
                                                formData["remoteDir"] = response.applicationResponseContent.remoteDir;
                                                formData["mediaDir"] = response.applicationResponseContent.mediaDir;
                                                self.refs.modalEditMediaDlg.show("Edit Media", <MediaConfigForm formData={formData}/>);
                                           })).caught(
                                                function(response){$.errorAlert(response)
                                           });
    }
})

var MediaConfigForm = React.createClass({
    mixins: [
        React.addons.LinkedStateMixin
    ],
    getInitialState: function() {
        var name = this.props.formData && this.props.formData.name ? this.props.formData.name : null;
        var type = this.props.formData && this.props.formData.type ? this.props.formData.type : null;
        var localPath = this.props.formData && this.props.formData.localPath ? this.props.formData.localPath : null;
        var remoteDir = this.props.formData && this.props.formData.remoteDir ? this.props.formData.remoteDir : null;
        var mediaDir = this.props.formData && this.props.formData.mediaDir ? this.props.formData.mediaDir : null;
        return {name: name, type: type, localPath: localPath, remoteDir: remoteDir, mediaDir: mediaDir};
    },
    render: function() {
        var idTextHidden = null;
        if (this.props.formData && this.props.formData.id) {
            idTextHidden = <input type="hidden" name="id" value={this.props.formData.id}/>;
        }

        return <div>
                   <form ref="form">
                       {idTextHidden}
                       <label>Name</label>
                       <label htmlFor="name" className="error"/>
                       <input name="name" type="text" valueLink={this.linkState("name")} maxLength="255" required/>
                       <label>Type</label>
                       <label htmlFor="type" className="error"/>
                       <MediaTypeDropdown ref="mediaTypeDropdown" selectedMediaType={this.state.type}/>
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

var MediaTypeDropdown = React.createClass({
    getInitialState: function() {
        return {selectedMediaType: this.props.selectedMediaType, mediaTypes: []}
    },
    componentDidMount: function() {
        var self = this;
        ServiceFactory.getMediaService().getMediaTypes().then(function(response){
            self.setState({mediaTypes: response.applicationResponseContent});
        });
    },
    render: function() {
        var self = this;
        var options = [];
        this.state.mediaTypes.forEach(function(mediaType){
            if (self.state.selectedMediaType === mediaType) {
                options.push(<option value={mediaType} selected="selected">{mediaType}</option>);
            } else {
                options.push(<option value={mediaType}>{mediaType}</option>);
            }
        });

        if (options.length > 0) {
            return <select name="type" refs="mediaTypeSelect" onChange={this.onChangeSelect} value={this.state.selectedMediaType}>{options}</select>
        }
        return <div>Loading Media Types...</div>
    },
    onChangeSelect: function(e) {
        this.setState({selectedMediaType: this.getDOMNode().value});
    }
})