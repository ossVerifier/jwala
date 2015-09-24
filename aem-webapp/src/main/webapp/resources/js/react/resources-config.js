/** @jsx React.DOM */
var ResourcesConfig = React.createClass({
    render: function() {
        var splitterComponents = [];

        splitterComponents.push(<div><ResourceEditor resourceService={this.props.resourceService}
                                                     groupService={this.props.groupService}
                                                     jvmService={this.props.jvmService}
                                                     wsService={this.props.wsService}
                                                     webAppService={this.props.webAppService}
                                                     generateXmlSnippetCallback={this.generateXmlSnippetCallback}
                                                     getTemplateCallback={this.getTemplateCallback}
                                                     selectEntityCallback={this.selectEntityCallback}
                                                     selectResourceTemplateCallback={this.selectResourceTemplateCallback}/></div>);
        splitterComponents.push(<XmlTabs jvmService={this.props.jvmService}
                                         wsService={this.props.wsService}
                                         webAppService={this.props.webAppService}
                                         ref="xmlTabs"
                                         uploadDialogCallback={this.launchUpload} />);

        var splitter = <RSplitter disabled={true} components={splitterComponents} orientation={RSplitter.VERTICAL_ORIENTATION} updateCallback={this.verticalSplitterDidUpdateCallback}/>

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
                </div>
    },
    generateXmlSnippetCallback: function(resourceName, groupName) {
        this.props.resourceService.getXmlSnippet(resourceName, groupName, this.generateXmlSnippetResponseCallback);
    },
    generateXmlSnippetResponseCallback: function(response) {
        this.refs.xmlTabs.refreshXmlDisplay(response.applicationResponseContent);
    },
    getTemplateCallback: function(template) {
        this.refs.xmlTabs.refreshTemplateDisplay(template);
    },
    selectEntityCallback: function(data, resourceName) {
        this.refs.xmlTabs.setState({entityType: data.rtreeListMetaData.entity,
                                    entity: data,
                                    resourceTemplateName: null,
                                    entityParent: data.rtreeListMetaData.parent,
                                    template: ""});
    },
    selectResourceTemplateCallback: function(entity, resourceName) {
        this.refs.xmlTabs.reloadTemplate(entity, resourceName);
    },
    templateComponentDidMount: function(template) {
         var fileName = this.refs.xmlTabs.state.resourceTemplateName;
         var entityType = this.refs.xmlTabs.state.entityType;
        template.setState({
            fileName: fileName,
            entityName: ResourcesConfig.getEntityName(this.refs.xmlTabs.state.entity, this.refs.xmlTabs.state.entityType),
            entityType: entityType
        })
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
                          this.props.webAppService.uploadTemplateForm(
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
                     } else {
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
                     }
         } else {
            this.refs.templateUploadModal.refs.okBtn.handleClick = this.okCallback;
         }
     },
     cancelCallback: function() {
         this.refs.templateUploadModal.close();
     },
     launchUpload: function() {
         this.refs.templateUploadModal.setState({
            title: "Upload template for " + this.refs.xmlTabs.state.resourceTemplateName
         })
         this.refs.templateUploadModal.show();
     },
     verticalSplitterDidUpdateCallback: function() {
         this.refs.xmlTabs.refs.xmlEditor.resize();
         var tabContentHeight = $(".horz-divider.rsplitter.childContainer.vert").height() - 20;
         $(".xml-editor-preview-tab-component").not(".content").css("cssText", "height:" + tabContentHeight + "px !important;");
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
        return {entityType: null, entity: null, entityParent: null, resourceTemplateName: null, template: ""}
    },
    render: function() {
        var xmlEditor;
        var xmlPreview;

        if (this.state.template === "") {
            xmlEditor = <div style={{padding: "5px 5px"}}>Please select a JVM, Web Server or Web Application and a resource</div>;
            xmlPreview = <div style={{padding: "5px 5px"}}>Please select a JVM, Web Server or Web Application and a resource</div>;
        } else {
            xmlEditor = <XmlEditor ref="xmlEditor"
                                   className="xml-editor-container"
                                   content={this.state.template}
                                   saveCallback={this.saveResourceTemplateContentCallback}
                                   deployCallback={this.deployResourceTemplateCallback}
				   uploadDialogCallback={this.props.uploadDialogCallback}/>;
            xmlPreview = <XmlPreview ref="xmlPreview" deployCallback={this.deployResourceTemplateCallback}/>
        }

        var xmlTabItems = [{title: "Template", content:xmlEditor},
                           {title: "Preview", content:xmlPreview}];

        return <RTabs ref="tabs" items={xmlTabItems} depth={2} onSelectTab={this.onSelectTab}
                      className="xml-editor-preview-tab-component"
                      contentClassName="xml-editor-preview-tab-component content" />
    },
    componentWillUpdate: function(nextProps, nextState) {
        this.refs.tabs.setState({activeHash: "#/Configuration/Resources/Template/"});
    },
    saveResourceTemplateContentCallback: function(data) {
         if (this.state.entity !== null && this.state.resourceName !== null) {
            if (this.state.entityType === "jvms") {
                this.props.jvmService.updateResourceTemplate(this.state.entity.jvmName,
                                                             this.state.resourceTemplateName,
                                                             data,
                                                             this.saveResourceTemplateContentSuccessCallback,
                                                             this.saveResourceTemplateContentErrorCallback);
            } else if (this.state.entityType === "webServers") {
                this.props.wsService.updateResourceTemplate(this.state.entity.name,
                                                            this.state.resourceTemplateName,
                                                            data,
                                                            this.saveResourceTemplateContentSuccessCallback,
                                                            this.saveResourceTemplateContentErrorCallback);
            } else if (this.state.entityType === "webApps") {
                this.props.webAppService.updateResourceTemplate(this.state.entity.name,
                                                                this.state.resourceTemplateName,
                                                                data,
                                                                this.saveResourceTemplateContentSuccessCallback,
                                                                this.saveResourceTemplateContentErrorCallback);
            }
        }
    },
    deployResourceTemplateCallback: function(ajaxProcessDoneCallback) {
        if (this.state.entity !== null && this.state.resourceName !== null) {
             if (this.state.entityType === "jvms") {
                this.props.jvmService.deployJvmConf(this.state.entity.jvmName,
                                                    this.state.resourceTemplateName,
                                                    this.deployResourceTemplateSuccessCallback.bind(this, ajaxProcessDoneCallback),
                                                    this.deployResourceTemplateFailCallback.bind(this, ajaxProcessDoneCallback));
             } else if (this.state.entityType === "webServers") {
                this.props.wsService.deployHttpdConf(this.state.entity.name,
                                                     this.deployResourceTemplateSuccessCallback.bind(this, ajaxProcessDoneCallback),
                                                     this.deployResourceTemplateFailCallback(this, ajaxProcessDoneCallback));
             } else if (this.state.entityType === "webApps") {
                this.props.webAppService.deployWebAppsConf(this.state.entity.name,
                                                           this.state.entity.group.name,
                                                           this.state.entityParent.jvmName,
                                                           this.state.resourceTemplateName,
                                                           this.deployWebAppResourceTemplateSuccessCallback.bind(this, ajaxProcessDoneCallback),
                                                           this.deployResourceTemplateFailCallback(this, ajaxProcessDoneCallback));
             }
        }
    },
    deployWebAppResourceTemplateSuccessCallback: function(response) {
        $.alert(response.applicationResponseContent, false);
    },
    deployResourceTemplateSuccessCallback: function(ajaxProcessDoneCallback, response) {
        if (ajaxProcessDoneCallback !== undefined) {
            ajaxProcessDoneCallback();
        }
        $.alert("Successfully deployed resource file(s)", false);
    },
    deployResourceTemplateFailCallback: function(ajaxProcessDoneCallback, errMsg) {
        if (ajaxProcessDoneCallback !== undefined) {
            ajaxProcessDoneCallback();
        }
        $.errorAlert(errMsg, "Deploy Resource Template Error", false);
    },
    saveResourceTemplateContentSuccessCallback: function(response) {
        this.showFadingStatus("Saved", this.refs.xmlEditor.getDOMNode());
        if (response.message === "SUCCESS") {
            this.setState({template:response.applicationResponseContent});
        } else {
            $.errorAlert(response.applicationResponseContent, "Save Resource Template Error", false);
        }
    },
    saveResourceTemplateContentErrorCallback: function(e) {
        $.errorAlert(e, "Save Resource Template Error", false);
    },
    reloadTemplate: function(data, resourceName) {
        var entityType = this.state.entityType;
        if (entityType !== null && resourceName !== null) {
            if (entityType === "jvms") {
                this.props.jvmService.getResourceTemplate(data.jvmName,
                                                          false,
                                                          resourceName,
                                                          this.reloadTemplateCallback.bind(this, data, resourceName));
            } else if (entityType === "webServers") {
                this.props.wsService.getResourceTemplate(data.name,
                                                         false,
                                                         resourceName,
                                                         this.reloadTemplateCallback.bind(this, data, resourceName));
            } else if (entityType === "webApps") {
                this.props.webAppService.getResourceTemplate(data.name,
                                                             null,
                                                             null,
                                                             false,
                                                             resourceName,
                                                             this.reloadTemplateCallback.bind(this, data, resourceName));
            }
        } else {
            this.setState({entityType: entityType,
                           entity: null,
                           entityParent: null,
                           resourceTemplateName: null,
                           template: ""});
        }
    },
    reloadTemplateCallback: function(entity, resourceTemplateName, response) {
        this.setState({entity: entity,
                       resourceTemplateName: resourceTemplateName,
                       template: response.applicationResponseContent});
    },
    onSelectTab: function(index) {
        if (this.state.entity !== null && this.state.resourceTemplateName !== null) {
            if (index === 1 ) {
                if (this.state.entityType === "jvms") {
                    this.props.jvmService.previewResourceFile(this.state.entity.jvmName,
                                                              this.state.entityParent.name,
                                                              this.refs.xmlEditor.getText(),
                                                              this.previewSuccessCallback,
                                                              this.previewErrorCallback);
                } else if (this.state.entityType === "webServers") {
                    this.props.wsService.previewResourceFile(this.state.entity.name,
                                                             this.state.entityParent.name,
                                                             this.refs.xmlEditor.getText(),
                                                             this.previewSuccessCallback,
                                                             this.previewErrorCallback);
                } else if (this.state.entityType === "webApps") {
                    this.props.webAppService.previewResourceFile(this.state.entity.name,
                                                                 this.state.entity.group.name,
                                                                 this.state.entityParent.jvmName,
                                                                 this.refs.xmlEditor.getText(),
                                                                 this.previewSuccessCallback,
                                                                 this.previewErrorCallback);
                }
            }
        }
    },
    previewSuccessCallback: function(response) {
        this.refs.xmlPreview.refresh(response.applicationResponseContent);
    },
    previewErrorCallback: function(errMsg) {
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
            entityName: ""
        };
    },
    render: function() {
        var entityLabel = "JVM";
        if ("webApps" === this.state.entityType){
            entityLabel = "App";
        } else if ("webServers" === this.state.entityType) {
            entityLabel = "Web Server"
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


