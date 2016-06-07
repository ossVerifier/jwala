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
                                         ref="xmlTabs"
                                         uploadDialogCallback={this.launchUpload}
                                         updateGroupTemplateCallback={this.launchUpdateGroupTemplate}
                                         />);

        var splitter = <RSplitter disabled={true}
                                  components={splitterComponents}
                                  orientation={RSplitter.VERTICAL_ORIENTATION}
                                  updateCallback={this.verticalSplitterDidUpdateCallback}
                                  onSplitterChange={this.onChildSplitterChangeCallback}/>;

        return <div className="react-dialog-container resources-div-container">
                    <div className="resource-container">{splitter}</div>
                    <ModalDialogBox
                        title="Upload template"
                        show={false}
                        okCallback={this.okCallback}
                        cancelCallback={this.cancelCallback}
                        content={<TemplateUploadForm ref="templateUploadForm" componentDidMountCallback={this.templateComponentDidMount}/>}
                        ref="templateUploadModal"
                     />
                    <ModalDialogBox
                     title="Confirm update"
                     show={false}
                     okCallback={this.okUpdateGroupTemplateCallback}
                     cancelCallback={this.cancelUpdateGroupTemplateCallback}
                     content={<ConfirmUpdateGroupDialog componentDidMountCallback={this.confirmUpdateGroupDidMount}/>}
                     ref="templateUpdateGroupModal"
                    />
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
     okCallback: function() {
        if (this.refs.templateUploadForm.isValid()) {
                     var self = this;
                     var fileName = this.refs.xmlTabs.state.resourceTemplateName;
                     var fileData = this.refs.templateUploadForm.refs.templateFile.getDOMNode().files[0];
                     var formData = new FormData();
                     formData.append(fileName, fileData);
                     var entityType = this.refs.xmlTabs.state.entityType;
                     var entityName = ResourcesConfig.getEntityName(this.refs.xmlTabs.state.entity, this.refs.xmlTabs.state.entityType);
                     if ("jvms" === entityType){
                         this.props.jvmService.uploadTemplateForm(
                                                         entityName,
                                                         fileName,
                                                         formData,
                                                         function(){
                                                             self.refs.templateUploadModal.close();
                                                             self.refs.xmlTabs.reloadTemplate({jvmName:entityName}, fileName);
                                                         },
                                                         function(errMsg) {
                                                             $.errorAlert(errMsg, "Error");
                                                         });
                     } else if ("webApps" === entityType) {
                          var parentJvmName = this.refs.xmlTabs.state.entityParent.jvmName;
                          this.props.webAppService.uploadTemplateForm(
                                                       entityName,
                                                       parentJvmName,
                                                       fileName,
                                                       formData,
                                                       function(){
                                                           self.refs.templateUploadModal.close();
                                                           self.refs.xmlTabs.reloadTemplate({name:entityName}, fileName);
                                                       },
                                                       function(errMsg) {
                                                           $.errorAlert(errMsg, "Error");
                                                       }
                          );
                     } else if ("webServers" === entityType) {
                           this.props.wsService.uploadTemplateForm(
                                                    entityName,
                                                  fileName,
                                                  formData,
                                                  function(){
                                                      self.refs.templateUploadModal.close();
                                                      self.refs.xmlTabs.reloadTemplate({name:entityName}, fileName);
                                                  },
                                                  function(errMsg) {
                                                      $.errorAlert(errMsg, "Error");
                                                  }
                                               );
                     } else if ("jvmSection" === entityType) {
                           if (this.refs.xmlTabs.state.groupJvmEntityType && this.refs.xmlTabs.state.groupJvmEntityType === "webApp"){
                                    this.props.groupService.uploadGroupAppTemplateForm(
                                                this.refs.xmlTabs.state.entityGroupName,
                                                fileName,
                                                formData,
                                                     function(){
                                                         self.refs.templateUploadModal.close();
                                                         self.refs.xmlTabs.reloadTemplate({name:entityName, groupJvmEntityType: self.refs.xmlTabs.state.groupJvmEntityType}, fileName);
                                                     },
                                                     function(errMsg) {
                                                         $.errorAlert(errMsg, "Error");
                                                     }
                                                  );
                           } else {
                                this.props.groupService.uploadGroupJvmTemplateForm(
                                                    this.refs.xmlTabs.state.entityGroupName,
                                                  fileName,
                                                  formData,
                                                  function(){
                                                      self.refs.templateUploadModal.close();
                                                      self.refs.xmlTabs.reloadTemplate({name:entityName}, fileName);
                                                  },
                                                  function(errMsg) {
                                                      $.errorAlert(errMsg, "Error");
                                                  }
                                               );
                           }
                     } else if ("webServerSection" === entityType) {
                           this.props.groupService.uploadGroupWebServerTemplateForm(
                                                    this.refs.xmlTabs.state.entityGroupName,
                                                  fileName,
                                                  formData,
                                                  function(){
                                                      self.refs.templateUploadModal.close();
                                                      self.refs.xmlTabs.reloadTemplate({name:entityName}, fileName);
                                                  },
                                                  function(errMsg) {
                                                      $.errorAlert(errMsg, "Error");
                                                  }
                                               );
                     }
         } else {
            this.refs.templateUploadModal.refs.okBtn.handleClick = this.okCallback;
         }
     },
     cancelCallback: function() {
         this.refs.templateUploadModal.close();
     },
     okUpdateGroupTemplateCallback: function() {
        this.refs.xmlTabs.saveGroupTemplate();
        this.refs.templateUpdateGroupModal.close();
     },
     cancelUpdateGroupTemplateCallback: function() {
        this.refs.templateUpdateGroupModal.close();
     },
     launchUpload: function() {
         this.refs.templateUploadModal.setState({
            title: "Upload template for " + this.refs.xmlTabs.state.resourceTemplateName,
            entityGroupName: this.refs.xmlTabs.state.entityParent.name
         })
         this.refs.templateUploadModal.show();
     },
     launchUpdateGroupTemplate: function(){
        this.refs.templateUpdateGroupModal.show();
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
        this.refs.selectMetaDataAndTemplateFilesModalDlg.show();
     },
     deleteResourceCallback: function() {
        this.refs.confirmDeleteResourceModalDlg.show(true);
     },
     confirmDeleteResourceCallback: function() {
        var groupName;
        var webServerName;
        var jvmName;
        var webAppName;
        var node = this.refs.resourceEditor.refs.treeList.getSelectedNodeData();

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

        var self = this;
        this.refs.confirmDeleteResourceModalDlg.close();
        this.props.resourceService.deleteResource(this.refs.resourceEditor.refs.resourcePane.getSelectedValue(),
                                                  groupName, webServerName, jvmName, webAppName).then(function(response){
            self.refreshResourcePane();
        }).caught(function(e){
            console.log(e);
            $.errorAlert("Error deleting resource template(s)!", "Error", true);
        });
     },
     onCreateResourceOkClicked: function() {
        var metaDataFile = this.refs.selectMetaDataAndTemplateFilesWidget.refs.metaDataFile.getDOMNode().files[0];
        var templateFile = this.refs.selectMetaDataAndTemplateFilesWidget.refs.templateFile.getDOMNode().files[0];

        if (metaDataFile === undefined) {
            this.refs.selectMetaDataAndTemplateFilesWidget.setState({invalidMetaFile: true});
        }

        if (templateFile === undefined) {
            this.refs.selectMetaDataAndTemplateFilesWidget.setState({invalidTemplateFile: true});
        }

        if (metaDataFile && templateFile) {
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

            var self = this;
            this.props.resourceService.createResource(groupName, webServerName, jvmName, webAppName, formData).then(function(response){
                self.refs.selectMetaDataAndTemplateFilesModalDlg.close();
                self.refreshResourcePane();
            }).caught(function(response){
                console.log(response);
                var errMsg = response.responseJSON ? response.responseJSON.applicationResponseContent : "";
                $.errorAlert("Error creating resource template! " + errMsg, "Error", true);
            });
        }
     },
     refreshResourcePane: function() {
        var data = this.refs.resourceEditor.refs.treeList.getSelectedNodeData();
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
        return {entityType: null, entity: null, entityParent: null, resourceTemplateName: null, template: "", entityGroupName: "", groupJvmEntityType: null}
    },
    render: function() {
        var codeMirrorComponent;
        var xmlPreview;

        if (this.state.resourceTemplateName === null) {
            codeMirrorComponent = <div style={{padding: "5px 5px"}}>Please select a JVM, Web Server or Web Application and a resource</div>;
            xmlPreview = <div style={{padding: "5px 5px"}}>Please select a JVM, Web Server or Web Application and a resource</div>;
        } else {
            codeMirrorComponent = <CodeMirrorComponent ref="codeMirrorComponent" content={this.state.template}
                                   className="xml-editor-container" saveCallback={this.saveResource}
                                   onChange={this.onChangeCallback}/>
            xmlPreview = <XmlPreview ref="xmlPreview" deployCallback={this.deployResource}/>
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
            this.setState({template:template});
            this.props.updateGroupTemplateCallback();
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
            } else {
                thePromise = this.props.groupService.updateGroupJvmResourceTemplate(this.state.entityGroupName, this.state.resourceTemplateName, template);
            }
        }
        return thePromise;
    },
    saveGroupTemplate: function(){
        var thePromise;
        if (this.state.groupJvmEntityType && this.state.groupJvmEntityType === "webApp") {
            thePromise = this.props.groupService.updateGroupAppResourceTemplate(this.state.entityGroupName, this.state.resourceTemplateName, this.state.template);
        }  else if (this.state.entityType === "webServerSection") {
            thePromise = this.props.groupService.updateGroupWebServerResourceTemplate(this.state.entityGroupName, this.state.resourceTemplateName, this.state.template);
        } else {
            thePromise = this.props.groupService.updateGroupJvmResourceTemplate(this.state.entityGroupName, this.state.resourceTemplateName, this.state.template);
        }
        thePromise.then(this.savedResourceCallback).caught(this.failed.bind(this, "Save Resource Template"));
    },
    savedResourceCallback: function(response) {
        if (response.message === "SUCCESS") {
            console.log("Save success!");
            MainArea.unsavedChanges = false;
            this.showFadingStatus("Saved", this.refs.codeMirrorComponent.getDOMNode());
            this.setState({template:response.applicationResponseContent});
        } else {
            throw response;
        }
    },
    deployResourcePromise: function(ajaxProcessDoneCallback) {
        var thePromise;
        console.log("deploying...");
        if (this.state.entity !== null && this.state.resourceTemplateName !== null) {
            if (this.state.entityType === "jvms") {
                thePromise = this.props.jvmService.deployJvmConf(this.state.entity.jvmName,
                                                                 this.state.resourceTemplateName);
            } else if (this.state.entityType === "webServers") {
                thePromise = this.props.wsService.deployHttpdConf(this.state.entity.name,
                                                                  this.state.resourceTemplateName);
            } else if (this.state.entityType === "webApps") {
                if (this.state.entityParent.jvmName){
                    thePromise = this.props.webAppService.deployWebAppsConf(this.state.entity.name,
                                                                        this.state.entity.group.name,
                                                                        this.state.entityParent.jvmName,
                                                                        this.state.resourceTemplateName);
                } else {
                    thePromise = this.props.groupService.deployGroupAppConf(this.state.entityGroupName,
                                                                                this.state.resourceTemplateName);
                }
            } else if (this.state.entityType === "webServerSection") {
                thePromise = this.props.groupService.deployGroupWebServerConf(this.state.entityGroupName,
                                                                              this.state.resourceTemplateName);
            } else if (this.state.entityType === "jvmSection") {
                    thePromise = this.props.groupService.deployGroupJvmConf(this.state.entityGroupName,
                                                                 this.state.resourceTemplateName);
            }
        }
        return thePromise;
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
    deployResource: function(ajaxProcessDoneCallback) {
        var saveAndDeploy = function(response, self, type) {
                        if (response.applicationResponseContent.allStopped === "true") {
                            self.saveResourcePromise(self.refs.codeMirrorComponent.getText())
                            .then(function(response){
                                self.savedResourceCallback(response);
                                return self.deployResourcePromise();
                            })
                            .then(self.deployResourceCallback).caught(self.failed.bind(this, "Deploy Resource"))
                            .lastly(ajaxProcessDoneCallback);
                        } else {
                            $.errorAlert("All " + type + "s in the group must be stopped before continuing. Operation stopped for " + type + " " + response.applicationResponseContent.entityNotStopped,"Error", false);
                            ajaxProcessDoneCallback();
                        }
        };

        if (this.refs.codeMirrorComponent !== undefined && this.refs.codeMirrorComponent.isContentChanged()) {
            var ans = confirm("Changes to the resource template will be saved before deployment. Are you sure you want to proceed ?");
            if (ans) {
                var self = this;
                if (this.state.entityType === "jvmSection") {
                    this.checkGroupJvmsAreStopped(this.state.entityGroupName)
                    .then(function(response) {
                        saveAndDeploy(response, self, "JVM");
                    });
                } else if (this.state.entityType === "webServerSection") {
                    this.checkGroupWebServersAreStopped(this.state.entityGroupName)
                    .then(function(response) {
                        saveAndDeploy(response, self, "Web Server");
                    });
                } else {
                    this.saveResourcePromise(self.refs.codeMirrorComponent.getText())
                    .then(function(response){
                        self.savedResourceCallback(response);
                        return self.deployResourcePromise();
                    })
                    .then(self.deployResourceCallback).caught(self.failed.bind(this, "Deploy Resource"))
                    .lastly(ajaxProcessDoneCallback);
                }
            } else {
                ajaxProcessDoneCallback();
            }
        } else {
            this.deployResourcePromise().then(this.deployResourceCallback)
                                        .caught(this.failed.bind(this, "Deploy Resource"))
                                        .lastly(ajaxProcessDoneCallback);
        }
    },
    deployResourceCallback: function() {
        console.log("Deploy success!");
        $.alert("Successfully deployed resource file(s)", false);
    },
    /*** Save and Deploy methods: End ***/

    reloadTemplate: function(data, resourceName, groupJvmEntityType) {
        var entityType = this.state.entityType;
        if (entityType !== null && resourceName !== null) {
            if (entityType === "jvms") {
                this.props.jvmService.getResourceTemplate(data.jvmName,
                                                          false,
                                                          resourceName,
                                                          this.reloadTemplateCallback.bind(this, data, resourceName, this.state.entityGroupName));
            } else if (entityType === "webServers") {
                this.props.wsService.getResourceTemplate(data.name,
                                                         false,
                                                         resourceName,
                                                         this.reloadTemplateCallback.bind(this, data, resourceName, this.state.entityGroupName));
            } else if (entityType === "webApps" && this.state.entityParent.jvmName) {
                this.props.webAppService.getResourceTemplate(data.name,
                                                             this.state.entity.group.name,
                                                             this.state.entityParent.jvmName,
                                                             false,
                                                             resourceName,
                                                             this.reloadTemplateCallback.bind(this, data, resourceName, this.state.entityGroupName));
            } else if (entityType === "webApps" && this.state.entityParent.rtreeListMetaData.parent.name) {
                var self = this;
                ServiceFactory.getResourceService().getResourceTemplate(this.state.entity.group.name, data.name,
                    resourceName).then(function(response){
                        self.reloadTemplateCallback(data, resourceName, self.state.entity.group.name, response);
                    }).caught(function(e){console.log(e);});
            } else if (entityType === "webServerSection") {
                             this.props.groupService.getGroupWebServerResourceTemplate(this.state.entityGroupName,
                                                                      false,
                                                                      resourceName,
                                                                      this.reloadTemplateCallback.bind(this, data, resourceName, this.state.entityGroupName));
            } else if (entityType === "jvmSection") {
                              if ((groupJvmEntityType && groupJvmEntityType === "webApp") || (data.groupJvmEntityType && data.groupJvmEntityType === "webApp")) {
                                  this.props.groupService.getGroupAppResourceTemplate(this.state.entityGroupName,
                                                                       false,
                                                                       resourceName,
                                                                       this.reloadTemplateCallback.bind(this, data, resourceName, this.state.entityGroupName));
                              } else {
                                  this.props.groupService.getGroupJvmResourceTemplate(this.state.entityGroupName,
                                                                                                     false,
                                                                                                     resourceName,
                                                                                                     this.reloadTemplateCallback.bind(this, data, resourceName, this.state.entityGroupName));
                              }
            }
        } else {
            this.setState({entityType: entityType,
                           entity: null,
                           entityParent: null,
                           resourceTemplateName: null,
                           template: ""});
        }
    },
    reloadTemplateCallback: function(entity, resourceTemplateName, entityGroupName, response) {
        this.setState({entity: entity,
                       resourceTemplateName: resourceTemplateName,
                       template: response.applicationResponseContent,
                       entityGroupName: entityGroupName});
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
//                    if (this.state.entityParent.jvmName) {
//
//                    } else {
////                        ServiceFactory.getResourceService().previewResourceFile(this.state.entity.group.name, this.state.entity.name,
////                            this.refs.codeMirrorComponent.getText()).then(function(response){}).caught(function(e){console.log(e)});
//                    }

                    this.props.webAppService.previewResourceFile(this.state.entity.name,
                                                                 this.state.entity.group.name,
                                                                 this.state.entityParent.jvmName ? this.state.entityParent.jvmName : "",
                                                                 this.refs.codeMirrorComponent.getText(),
                                                                 this.previewSuccessCallback,
                                                                 this.previewErrorCallback);


                }  else if (this.state.entityType === "webServerSection") {
                    this.props.groupService.previewGroupWebServerResourceFile(this.state.entityGroupName,
                                                                 this.refs.codeMirrorComponent.getText(),
                                                                 this.previewSuccessCallback,
                                                                 this.previewErrorCallback);
                } else if (this.state.entityType === "jvmSection") {
                    if (this.state.groupJvmEntityType && this.state.groupJvmEntityType === "webApp") {
                        this.props.groupService.previewGroupAppResourceFile(this.state.entityGroupName,
                                                                                     this.state.resourceTemplateName,
                                                                                     this.refs.codeMirrorComponent.getText(),
                                                                                     this.previewSuccessCallback,
                                                                                     this.previewErrorCallback);
                    } else {
                        this.props.groupService.previewGroupJvmResourceFile(this.state.entityGroupName,
                                                                 this.refs.codeMirrorComponent.getText(),
                                                                 this.previewSuccessCallback,
                                                                 this.previewErrorCallback);
                    }
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

