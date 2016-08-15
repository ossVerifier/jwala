/** @jsx React.DOM */
var ResourcesConfig = React.createClass({
    render: function() {
        var splitterComponents = [];

        splitterComponents.push(<ResourceEditor ref="resourceEditor"
                                                resourceService={this.props.resourceService}
                                                groupService={this.props.groupService}
                                                jvmService={this.props.jvmService}
                                                wsService={this.props.wsService}
                                                webAppService={this.props.webAppService}
                                                getTemplateCallback={this.getTemplateCallback}
                                                selectEntityCallback={this.selectEntityCallback}
                                                selectResourceTemplateCallback={this.selectResourceTemplateCallback}
                                                createResourceCallback={this.createResourceCallback}
                                                deleteResourceCallback={this.deleteResourceCallback}/>);
        splitterComponents.push(<XmlTabs jvmService={this.props.jvmService}
                                         wsService={this.props.wsService}
                                         webAppService={this.props.webAppService}
                                         groupService={this.props.groupService}
                                         resourceService={this.props.resourceService}
                                         ref="xmlTabs"
                                         uploadDialogCallback={this.launchUpload}
                                         updateGroupTemplateCallback={this.launchUpdateGroupTemplate}
                                         updateExtPropsAttributesCallback={this.updateExtPropsAttributesCallback}
                                         />);

        var splitter = <RSplitter disabled={true}
                                  components={splitterComponents}
                                  orientation={RSplitter.VERTICAL_ORIENTATION}
                                  updateCallback={this.verticalSplitterDidUpdateCallback}
                                  onSplitterChange={this.onChildSplitterChangeCallback}/>;

        return <div className="react-dialog-container resources-div-container">
                    <div className="resource-container">{splitter}</div>
                    <ModalDialogBox
                     title="Confirm update"
                     show={false}
                     cancelCallback={this.cancelUpdateGroupTemplateCallback}
                     ref="templateUpdateGroupModal"/>
                    <ModalDialogBox title="External Properties Template"
                                    ref="warnExternalPropertiesUploadModalDlg"
                                    show={false}
                                    cancelCallback={this.warnExternalPropertiesUploadCallback}
                                    cancelLabel="Continue"
                                    content={<div className="text-align-center"><br/>Only one template is allowed to be uploaded for the External Properties.<br/>Any existing template will be overwritten.<br/><br/></div>}/>
                    <ModalDialogBox ref="selectTemplateFilesModalDlg"
                                                        title="Upload External Properties"
                                                        show={false}
                                                        okCallback={this.onCreateResourceOkClicked}
                                                        content={<SelectTemplateFilesWidget ref="selectTemplateFileWidget"/>}/>
                    <ModalDialogBox ref="selectMetaDataAndTemplateFilesModalDlg"
                                    title="Create Resource Template"
                                    show={false}
                                    okCallback={this.onCreateResourceOkClicked}
                                    content={<SelectMetaDataAndTemplateFilesWidget ref="selectMetaDataAndTemplateFilesWidget"/>}/>
                    <ModalDialogBox ref="confirmDeleteResourceModalDlg"
                                    title="Confirm Resource Template Deletion"
                                    show={false}
                                    okCallback={this.confirmDeleteResourceCallback}
                                    content={<div className="text-align-center"><br/><b>Are you sure you want to delete the selected resource template(s) ?</b><br/><br/></div>}
                                    okLabel="Yes"
                                    cancelLabel="No" />
                    <ModalDialogBox ref="confirmDeletePropertyModalDlg"
                                    title="Confirm Property file deletion"
                                    show={false}
                                    okCallback={this.confirmDeleteResourceCallback}
                                    content={<div className="text-align-center"><br/><b>Are you sure you want to delete the property file ?</b><br/><br/></div>}
                                    okLabel="Yes"
                                    cancelLabel="No" />
                </div>
    },
    componentDidMount: function() {
        MainArea.unsavedChanges = false;
        window.onbeforeunload = function() {
                                    console.log(this.refs);
                                    if (MainArea.unsavedChanges === true) {
                                        return "A resource template has recently been modified.";
                                    }
                                };
    },
    onChildSplitterChangeCallback: function(dimensions) {
        this.refs.resourceEditor.onParentSplitterChange(dimensions);
    },
    generateXmlSnippetResponseCallback: function(response) {
        this.refs.xmlTabs.refreshXmlDisplay(response.applicationResponseContent);
    },
    getTemplateCallback: function(template) {
        this.refs.xmlTabs.refreshTemplateDisplay(template);
    },
    selectEntityCallback: function(data, entity, parent) {
        if (this.refs.xmlTabs.refs.codeMirrorComponent !== undefined && this.refs.xmlTabs.refs.codeMirrorComponent.isContentChanged()) {
            var ans = confirm("All your changes won't be saved if you view another resource. Are you sure you want to proceed ?");
            if (!ans) {
                return false;
            }
        }

        this.refs.xmlTabs.setState({entityType: data.rtreeListMetaData.entity,
                                    entity: data,
                                    entityGroupName: data.rtreeListMetaData.parent.name,
                                    resourceTemplateName: null,
                                    entityParent: data.rtreeListMetaData.parent,
                                    template: ""});
        return true;
    },
    selectResourceTemplateCallback: function(entity, resourceName, groupJvmEntityType) {
        if (this.refs.xmlTabs.refs.codeMirrorComponent !== undefined && this.refs.xmlTabs.refs.codeMirrorComponent.isContentChanged()) {
            var ans = confirm("All your changes won't be saved if you view another resource. Are you sure you want to proceed ?");
            if (!ans) {
                return false;
            }
        }

        this.refs.xmlTabs.setState({groupJvmEntityType: groupJvmEntityType});
        this.refs.xmlTabs.reloadTemplate(entity, resourceName, groupJvmEntityType);
        return true;
    },
    templateComponentDidMount: function(template) {
         var fileName = this.refs.xmlTabs.state.resourceTemplateName;
         var entityType = this.refs.xmlTabs.state.entityType;
        template.setState({
            fileName: fileName,
            entityName: ResourcesConfig.getEntityName(this.refs.xmlTabs.state.entity, this.refs.xmlTabs.state.entityType),
            entityType: entityType,
            entityGroupName: this.refs.xmlTabs.state.entityParent.name
        })
    },
    confirmUpdateGroupDidMount: function (confirmDialog){
        var entityType = this.refs.xmlTabs.state.entityType;
        var entityGroupName = this.refs.xmlTabs.state.entityParent.name;
        confirmDialog.setState({
            entityType: entityType,
            entityGroupName: entityGroupName
        });
    },
    okUpdateGroupTemplateCallback: function(template) {
        this.refs.xmlTabs.saveGroupTemplate(template);
        this.refs.templateUpdateGroupModal.close();
    },
    cancelUpdateGroupTemplateCallback: function() {
        this.refs.templateUpdateGroupModal.close();
    },

    launchUpdateGroupTemplate: function(template){
        var self = this;
        this.refs.templateUpdateGroupModal.show("Confirm update",
            <ConfirmUpdateGroupDialog componentDidMountCallback={this.confirmUpdateGroupDidMount}
                                      template={template}/>, function() {
                                        self.okUpdateGroupTemplateCallback(template);
                                      });
    },
    updateExtPropsAttributesCallback: function(){
        var self = this;
        this.props.resourceService.getExternalProperties().then(function(response){
            var attributes = self.refs.resourceEditor.refs.resourceAttrPane.state.attributes;
            attributes["ext"] = response.applicationResponseContent;
            self.refs.resourceEditor.refs.resourceAttrPane.setState({attributes: attributes})
        });
    },
    verticalSplitterDidUpdateCallback: function() {
        if (this.refs.xmlTabs.refs.codeMirrorComponent !== undefined) {
            this.refs.xmlTabs.refs.codeMirrorComponent.resize();
        }

        if (this.refs.xmlTabs.refs.xmlPreview !== undefined) {
            this.refs.xmlTabs.refs.xmlPreview.resize();
        }

        var tabContentHeight = $(".horz-divider.rsplitter.childContainer.vert").height() - 20;
        $(".xml-editor-preview-tab-component").not(".content").css("cssText", "height:" + tabContentHeight + "px !important;");
    },
    createResourceCallback: function(data) {
        if (data.rtreeListMetaData.entity === "extProperties") {
            this.refs.warnExternalPropertiesUploadModalDlg.show();
        } else {
            this.refs.selectMetaDataAndTemplateFilesModalDlg.show();
        }
    },
    warnExternalPropertiesUploadCallback: function(){
        this.refs.warnExternalPropertiesUploadModalDlg.close();
        this.refs.selectTemplateFilesModalDlg.show();
    },
    deleteResourceCallback: function() {
    if (this.refs.xmlTabs.state.entityType !== "extProperties") {
        this.refs.confirmDeleteResourceModalDlg.show();
    } else {
        this.refs.confirmDeletePropertyModalDlg.show();
    }
    },
    confirmDeleteResourceCallback: function() {
        var groupName;
        var webServerName;
        var jvmName;
        var webAppName;
        var node = this.refs.resourceEditor.refs.treeList.getSelectedNodeData();

        if (this.refs.xmlTabs.state.entityType !== "extProperties") {
            if (node.rtreeListMetaData.entity === "webApps") {
                webAppName = node.name;
                if (node.rtreeListMetaData.parent.rtreeListMetaData.entity === "jvms") {
                    jvmName = node.rtreeListMetaData.parent.jvmName;
                } else {
                    groupName = node.rtreeListMetaData.parent.rtreeListMetaData.parent.name;
                }
            } else if (node.rtreeListMetaData.entity === "jvmSection") {
                groupName = node.rtreeListMetaData.parent.name;
                jvmName = "*";
            } else if (node.rtreeListMetaData.entity === "jvms") {
                jvmName = node.jvmName;
            } else if (node.rtreeListMetaData.entity === "webServerSection") {
                groupName = node.rtreeListMetaData.parent.name;
                webServerName = "*";
            } else if (node.rtreeListMetaData.entity === "webServers") {
                webServerName = node.name;
            }
        }

        var self = this;
        this.refs.confirmDeleteResourceModalDlg.close();

        this.props.resourceService.deleteResources(this.refs.resourceEditor.refs.resourcePane.getCheckedItems(),
                                                  groupName, webServerName, jvmName, webAppName).then(function(response){
            self.refreshResourcePane();

            // clear the editor
            self.refs.xmlTabs.clearEditor();

            if (self.refs.xmlTabs.state.entityType === "extProperties"){
                self.updateExtPropsAttributesCallback();
            }
        }).caught(function(e){
            console.log(e);
            $.errorAlert("Error deleting resource template(s)!", "Error", true);
        });

    },
    onCreateResourceOkClicked: function() {
        var isExtProperties = this.refs.xmlTabs.state.entityType === "extProperties";
        var metaDataFile,templateFile;

        if(!isExtProperties){
            metaDataFile = this.refs.selectMetaDataAndTemplateFilesWidget.refs.metaDataFile.getDOMNode().files[0];
            templateFile = this.refs.selectMetaDataAndTemplateFilesWidget.refs.templateFile.getDOMNode().files[0];
            if (metaDataFile === undefined) {
                        this.refs.selectMetaDataAndTemplateFilesWidget.setState({invalidMetaFile: true});
            }
            if (templateFile === undefined) {
                        this.refs.selectMetaDataAndTemplateFilesWidget.setState({invalidTemplateFile: true});
            }
        } else {
//            metaDataFile = undefined;
            templateFile = this.refs.selectTemplateFileWidget.refs.templateFile.getDOMNode().files[0];
            if (templateFile === undefined) {
                this.refs.selectTemplateFileWidget.setState({invalidTemplateFile: true});
            }
        }

        if ((isExtProperties && templateFile) || (metaDataFile && templateFile)) {
            // Submit!
            var formData = new FormData();
            var self = this;
            formData.append("metaData", metaDataFile);
            formData.append("templateFile", templateFile);

            var groupName;
            var webServerName;
            var jvmName;
            var webAppName;
            var node = this.refs.resourceEditor.refs.treeList.getSelectedNodeData();

            if (!isExtProperties) {
                if (node.rtreeListMetaData.entity === "webApps") {
                    webAppName = node.name;
                    if (node.rtreeListMetaData.parent.rtreeListMetaData.entity === "jvms") {
                        jvmName = node.rtreeListMetaData.parent.jvmName;
                    } else {
                        groupName = node.rtreeListMetaData.parent.rtreeListMetaData.parent.name;
                    }
                } else if (node.rtreeListMetaData.entity === "jvmSection") {
                    groupName = node.rtreeListMetaData.parent.name;
                    jvmName = "*";
                } else if (node.rtreeListMetaData.entity === "jvms") {
                    groupName = node.rtreeListMetaData.parent.rtreeListMetaData.parent.name;
                    jvmName = node.jvmName;
                } else if (node.rtreeListMetaData.entity === "webServerSection") {
                    groupName = node.rtreeListMetaData.parent.name;
                    webServerName = "*";
                } else if (node.rtreeListMetaData.entity === "webServers") {
                    webServerName = node.name;
                }
            }

            var self = this;
            this.props.resourceService.createResource(groupName, webServerName, jvmName, webAppName, formData).then(function(response){
                if(!isExtProperties){
                    self.refs.selectMetaDataAndTemplateFilesModalDlg.close();
                } else {
                    self.refs.selectTemplateFilesModalDlg.close();
                }
                self.refreshResourcePane();
                if (isExtProperties){
                    self.updateExtPropsAttributesCallback();
                }
            }).caught(function(response){
                console.log(response);
                var errMsg = response.responseJSON ? response.responseJSON.applicationResponseContent : "";
                $.errorAlert("Error creating resource template! " + errMsg, "Error", true);
            });
        }
    },
    refreshResourcePane: function() {
        var data = this.refs.resourceEditor.refs.treeList.getSelectedNodeData();
        if (data === null && this.refs.xmlTabs.state.entityType === "extProperties"){
            // External Properties was the last node selected
            var rtreeListMetaData = {
                entity: "extProperties",
                parent:{
                    name:"Ext Properties parent",
                    key:"extPropertiesParent"
                }
            };
            data = {
                rtreeListMetaData: rtreeListMetaData,
                name: "External Properties"
            };

        }
        this.refs.resourceEditor.refs.resourcePane.getData(data);
    },
    statics: {
        getEntityName: function(entity, type) {
            if (type === "jvms") {
                return entity.jvmName;
            }
            return entity.name;
        }
    }
})

var XmlTabs = React.createClass({
    getInitialState: function() {
        return {entityType: null, entity: null, entityParent: null, resourceTemplateName: null, template: "",
                entityGroupName: "", groupJvmEntityType: null, readOnly: false}
    },
    clearEditor: function() {
        this.setState({resourceTemplateName: null, template: ""});
    },
    render: function() {
        var codeMirrorComponent;
        var xmlPreview;

        if (this.state.resourceTemplateName === null) {
            codeMirrorComponent = <div style={{padding: "5px 5px"}}>Please select a JVM, Web Server or Web Application and a resource</div>;
            xmlPreview = <div style={{padding: "5px 5px"}}>Please select a JVM, Web Server or Web Application and a resource</div>;
        } else {
            console.log("readOnly = " + this.state.readOnly);
            codeMirrorComponent = <CodeMirrorComponent ref="codeMirrorComponent" content={this.state.template}
                                   className="xml-editor-container" saveCallback={this.saveResource}
                                   onChange={this.onChangeCallback} readOnly={this.state.readOnly}/>
            if (this.state.entityType === "webServerSection" || this.state.entityType === "jvmSection") {
                xmlPreview = <div style={{padding: "5px 5px"}}>A group level web server or JVM template cannot be previewed. Please select a specific web server or JVM instead.</div>;
            } else {
                xmlPreview = <XmlPreview ref="xmlPreview" />
            }
        }

        var xmlTabItems = [{title: "Template", content:codeMirrorComponent},
                           {title: "Preview", content:xmlPreview}];

        return <RTabs ref="tabs" items={xmlTabItems} depth={2} onSelectTab={this.onSelectTab}
                      className="xml-editor-preview-tab-component"
                      contentClassName="xml-editor-preview-tab-component content" />
    },
    componentWillUpdate: function(nextProps, nextState) {
        this.refs.tabs.setState({activeHash: "#/Configuration/Resources/Template/",
            entityGroupName: nextState.entityParent.name});
    },
    onChangeCallback: function() {
        if (this.refs.codeMirrorComponent !== undefined && this.refs.codeMirrorComponent.isContentChanged()) {
            MainArea.unsavedChanges = true;
        } else {
            MainArea.unsavedChanges = false;
        }
    },
    checkGroupJvmsAreStopped: function(groupName){
        return this.props.groupService.getAllGroupJvmsAreStopped(groupName);
    },
    checkGroupWebServersAreStopped: function(groupName){
        return this.props.groupService.getAllGroupWebServersAreStopped(groupName);
    },
    /*** Save and Deploy methods: Start ***/
    saveResource: function(template) {
        if (this.state.entityType === "jvmSection" || this.state.entityType === "webServerSection"){
            this.props.updateGroupTemplateCallback(template);
        } else {
            this.saveResourcePromise(template).then(this.savedResourceCallback).caught(this.failed.bind(this, "Save Resource Template"));
        }
    },
    saveResourcePromise: function(template) {
        var thePromise;
        console.log("saving...");
        if (this.state.entity !== null && this.state.resourceTemplateName !== null) {
            if (this.state.entityType === "jvms") {
                thePromise = this.props.jvmService.updateResourceTemplate(this.state.entity.jvmName,
                    this.state.resourceTemplateName, template);
            } else if (this.state.entityType === "webServers") {
                thePromise = this.props.wsService.updateResourceTemplate(this.state.entity.name,
                    this.state.resourceTemplateName, template);
            } else if (this.state.entityType === "webApps") {
                thePromise = this.props.webAppService.updateResourceTemplate(this.state.entity.name,
                    this.state.resourceTemplateName, template, this.state.entityParent.jvmName, this.state.entity.group.name);
            } else if (this.state.groupJvmEntityType && this.state.groupJvmEntityType === "webApp") {
                thePromise = this.props.groupService.updateGroupAppResourceTemplate(this.state.entityGroupName, this.state.resourceTemplateName, template);
            }  else if (this.state.entityType === "webServerSection") {
                thePromise = this.props.groupService.updateGroupWebServerResourceTemplate(this.state.entityGroupName, this.state.resourceTemplateName, template);
            } else if (this.state.entityType === "extProperties"){
                thePromise = this.props.resourceService.updateResourceContent(this.state.resourceTemplateName, template, null, null, null, null);
            } else {
                thePromise = this.props.groupService.updateGroupJvmResourceTemplate(this.state.entityGroupName, this.state.resourceTemplateName, template);
            }
        }
        return thePromise;
    },
    saveGroupTemplate: function(template){
        var thePromise;
        var self = this;
        if (this.state.groupJvmEntityType && this.state.groupJvmEntityType === "webApp") {
            thePromise = this.props.groupService.updateGroupAppResourceTemplate(this.state.entityGroupName, this.state.resourceTemplateName, template);
        }  else if (this.state.entityType === "webServerSection") {
            thePromise = this.props.groupService.updateGroupWebServerResourceTemplate(this.state.entityGroupName, this.state.resourceTemplateName, template);
        } else {
            thePromise = this.props.groupService.updateGroupJvmResourceTemplate(this.state.entityGroupName, this.state.resourceTemplateName, template);
        }
        thePromise.then(this.savedResourceCallback).caught(this.failed.bind(this, "Save Resource Template"));
    },
    savedResourceCallback: function(response) {
        if (response.message === "SUCCESS") {
            console.log("Save success!");
            MainArea.unsavedChanges = false;
            this.showFadingStatus("Saved", this.refs.codeMirrorComponent.getDOMNode());
            this.setState({template:response.applicationResponseContent});
            if (this.state.entity === "extProperties"){
                this.props.updateExtPropsAttributesCallback();
            }
        } else {
            throw response;
        }
    },
    failed: function(title, response) {
        try {
            // Note: This will do for now. The problem is that the response's responseText is in HTML for save and
            //       JSON for deploy. We should standardize the responseText (e.g. JSON only) before we can modify
            //       this method to display the precise error message.
            var jsonResponseText = JSON.parse(response.responseText);
            var msg = jsonResponseText.applicationResponseContent === undefined ? "Operation was not successful!" :
                jsonResponseText.applicationResponseContent;
            $.errorAlert(msg, title, false);
        } catch (e) {
            console.log(response);
            console.log(e);
            $.errorAlert("Operation was not successful! Please check console logs for details.", title, false);
        }
    },
    /*** Save and Deploy methods: End ***/

    reloadTemplate: function(data, resourceName, groupJvmEntityType) {
        var entityType = this.state.entityType;
        if (entityType !== null && resourceName !== null) {
            if (entityType === "jvms") {
                this.getResourceContent(data, resourceName, null, null, data.jvmName);
            } else if (entityType === "webServers") {
                this.getResourceContent(data, resourceName, null, data.name);
            } else if (entityType === "webApps" && this.state.entityParent.jvmName) {
                this.getResourceContent(data, resourceName, null, null, this.state.entityParent.jvmName, data.name);
            } else if (entityType === "webApps" && this.state.entityParent.rtreeListMetaData.parent.name) {
                this.getResourceContent(data, resourceName, this.state.entity.group.name, null, null, data.name);
            } else if (entityType === "webServerSection") {
                this.getResourceContent(data, resourceName, this.state.entityGroupName, "*");
            } else if (entityType === "jvmSection") {
                this.getResourceContent(data, resourceName, this.state.entityGroupName, null, "*");
            } else if (entityType === "extProperties") {
                this.getResourceContent(data, resourceName, null, null, null, null);
            }
        } else {
            this.setState({entityType: entityType, entity: null, entityParent: null, resourceTemplateName: null,
                           template: ""});
        }
    },
    getResourceContent: function(data, resourceName, groupName, webServerName, jvmName, appName) {
        var self = this;
        ServiceFactory.getResourceService().getResourceContent(resourceName, groupName, webServerName, jvmName, appName)
        .then(function(response){
            var metaData = response.applicationResponseContent.metaData;
            var readOnly = false;
            if (metaData) {
                var jsonMetaData = JSON.parse(metaData);
                if (jsonMetaData.contentType === "application/binary") {
                    readOnly = true;
                }
            }

            self.setState({entity: data,
                           resourceTemplateName: resourceName,
                           template: response.applicationResponseContent.content,
                           entityGroupName: self.state.entityGroupName,
                           readOnly: readOnly});
        }).caught(function(response) {
            $.errorAlert("Error loading template!", "Error");
        });
    },
    onSelectTab: function(index) {
        if (this.state.entity !== null && this.state.resourceTemplateName !== null) {
            if (index === 1 ) {
                if (this.state.entityType === "jvms") {
                    this.props.jvmService.previewResourceFile(this.state.entity.jvmName,
                                                              this.state.entityParent.rtreeListMetaData.parent.name,
                                                              this.refs.codeMirrorComponent.getText(),
                                                              this.previewSuccessCallback,
                                                              this.previewErrorCallback);
                } else if (this.state.entityType === "webServers") {
                    this.props.wsService.previewResourceFile(this.state.entity.name,
                                                             this.state.entityParent.rtreeListMetaData.parent.name,
                                                             this.refs.codeMirrorComponent.getText(),
                                                             this.previewSuccessCallback,
                                                             this.previewErrorCallback);
                } else if (this.state.entityType === "webApps") {
                    this.props.webAppService.previewResourceFile(this.state.entity.name,
                                                                 this.state.entity.group.name,
                                                                 this.state.entityParent.jvmName ? this.state.entityParent.jvmName : "",
                                                                 this.refs.codeMirrorComponent.getText(),
                                                                 this.previewSuccessCallback,
                                                                 this.previewErrorCallback);


                }  else if (this.state.entityType === "jvmSection") {
                    if (this.state.groupJvmEntityType && this.state.groupJvmEntityType === "webApp") {
                        this.props.groupService.previewGroupAppResourceFile(this.state.entityGroupName,
                                                                                     this.state.resourceTemplateName,
                                                                                     this.refs.codeMirrorComponent.getText(),
                                                                                     this.previewSuccessCallback,
                                                                                     this.previewErrorCallback);
                    }
                } else if (this.state.entityType === "extProperties"){
                    this.props.resourceService.previewResourceFile(this.refs.codeMirrorComponent.getText(),
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   null,
                                                                   this.previewSuccessCallback,
                                                                   this.previewErrorCallback);

                }
            }
        }
    },
    previewSuccessCallback: function(response) {
        this.refs.xmlPreview.refresh(response.applicationResponseContent);
        // this.refs.xmlTabs.refs.xmlPreview.resize();
    },
    previewErrorCallback: function(errMsg) {
        this.refs.tabs.setState({activeHash: "#/Configuration/Resources/Template/"});
        $.errorAlert(errMsg, "Error");
    },
    /**
     * Uses jquery to take advantage of the fade out effect and to reuse the old code...for now.
     */
    showFadingStatus: function(msg, containerDom) {
        var toolTipId = "saveXmlBtn";
        if (msg !== undefined && $("#" + toolTipId).length === 0) {
            var top = $(containerDom).position().top + 10;
            var left = $(containerDom).position().left + 10;
            $(containerDom).parent().append("<div id='" + toolTipId +
                "' role='tooltip' class='ui-tooltip ui-widget ui-corner-all ui-widget-content' " +
                "style='top:" + top + "px;left:" + left + "px'>" + msg + "</div>");

            $("#" + toolTipId).fadeOut(3000, function() {
                $("#" + toolTipId).remove();
            });

        }
    }
});

var TemplateUploadForm = React.createClass({
    getInitialState: function(){
        return {
            fileName: "",
            entityName: "",
            entityGroupName: ""
        };
    },
    render: function() {
        var entityLabel = "JVM";
        if ("webApps" === this.state.entityType){
            entityLabel = "App";
        } else if ("webServers" === this.state.entityType) {
            entityLabel = "Web Server";
        } else if ("jvmSection" === this.state.entityType) {
            entityLabel = this.state.entityGroupName;
        } else if ("webServerSection" === this.state.entityType){
            entityLabel = this.state.entityGroupName;
        }
        return <div className={this.props.className}>
                 <form ref="templateForm" className="template-upload-form">
                    <div> {entityLabel}: {this.state.entityName} </div>
                    <div>
                        <label> Please select a template file (*.tpl) </label>
                    </div>
                    <div>
                        <label htmlFor="templateFile" className="error"></label>
                    </div>
                    <div>
                        <input type="file" name="templateFile" ref="templateFile"></input>
                    </div>
                 </form>
               </div>

    },
    componentDidMount: function() {
        this.validator = $(this.getDOMNode().children[0]).validate({
                                                                            rules: {"templateFile": {
                                                                                        required: true
                                                                                        }
                                                                                    },
                                                                            messages: {
                                                                                "templateFile": {
                                                                                    required: "Please select a file for upload"
                                                                                 },
                                                                            }
                                                                    });
        $(this.refs.templateFile.getDOMNode()).focus();
        $(this.refs.templateForm.getDOMNode()).submit(function(e) {
            e.preventDefault();
        });
        this.props.componentDidMountCallback(this);
        var self = this;
        var templateValidator = this.validator;
        this.refs.templateFile.getDOMNode().onchange = function() {
            if (templateValidator) {
                templateValidator.form();
            }
        };
    },
    isValid: function() {
       this.validator.form();
       if (this.validator.numberOfInvalids() === 0) {
           return true;
       }
       return false;
    },
    validator: null
});

var ConfirmUpdateGroupDialog = React.createClass({
    getInitialState: function(){
        return {
            entityType: "",
            entityGroupName: ""
        };
    },
    render: function() {
        var entityType = "JVM";
        if (this.state.entityType === "webServerSection"){
            entityType = "Web Server"
        }
        return <div className={this.props.className}>
                 Saving will overwrite all the {entityType} templates in the group
                 <br/>
                 {this.state.entityGroupName}.
                 <br/><br/>
                 Do you wish to continue?
               </div>
    },
    componentDidMount: function(){
        this.props.componentDidMountCallback(this);
    }
});

/**
 * Lets user select the meta data and template file used to create a resource.
 */
var SelectMetaDataAndTemplateFilesWidget = React.createClass({
    getInitialState: function() {
        // Let's not use jQuery form validation since we only need to check if the user has chosen files to use in creating the resource.
        // Besides, this is doing it the React way. :)
        return {invalidMetaFile: false, invalidTemplateFile: false};
    },
    render: function() {
        return <div className="select-meta-data-and-template-files-widget">
                   <form ref="form">
                       <p className="note">Note: The resource template that is about to be created will not follow the<br/>
                          selected topology since the meta data (*.json) file is the one that determines it.</p>
                       <div className={(!this.state.invalidMetaFile ? "hide " : "") + "error"}>Please select a meta data file (*.json)</div>
                       <div className="file-input-container">
                           <input type="file" ref="metaDataFile" name="metaDataFile" accept=".json" onChange={this.onMetaDataFileChange}>Meta Data File</input>
                       </div>
                       <div className={(!this.state.invalidTemplateFile ? "hide " : "") + "error"}>Please select a resource template file (*.tpl)</div>
                       <div>
                           <input type="file" ref="templateFile" name="templateFile" accept=".tpl" onChange={this.onTemplateFileChange}>Template File</input>
                       </div>
                   </form>
               </div>
    },
    componentDidMount: function() {
        $(this.refs.form.getDOMNode()).submit(function(e) {
                                                  console.log("Submit!");
                                                  e.preventDefault();
                                              });
    },
    onMetaDataFileChange: function(e) {
        if(this.refs.metaDataFile.getDOMNode().files[0]) {
            this.setState({invalidMetaFile: false});
        }
    },
    onTemplateFileChange: function(e) {
        if(this.refs.templateFile.getDOMNode().files[0]) {
            this.setState({invalidTemplateFile: false});
        }
    }
});


var SelectTemplateFilesWidget = React.createClass({
    getInitialState: function() {
        // Let's not use jQuery form validation since we only need to check if the user has chosen files to use in creating the resource.
        // Besides, this is doing it the React way. :)
        return {invalidTemplateFile: false};
    },
    render: function() {
        return <div className="select-meta-data-and-template-files-widget">
                   <form ref="form">
                       <div className={(!this.state.invalidTemplateFile ? "hide " : "") + "error"}>Please select a file</div>
                       <div>
                           <input type="file" ref="templateFile" onChange={this.onTemplateFileChange} name="templateFile">External Properties</input>
                       </div>
                   </form>
               </div>
    },
    componentDidMount: function() {
        $(this.refs.form.getDOMNode()).submit(function(e) {
                                                  console.log("Submit!");
                                                  e.preventDefault();
                                              });
    },
    onTemplateFileChange: function(e) {
        if(this.refs.templateFile.getDOMNode().files[0]) {
            this.setState({invalidTemplateFile: false});
        }
    }
});
