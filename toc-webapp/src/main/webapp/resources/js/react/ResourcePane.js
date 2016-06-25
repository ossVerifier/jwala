/** @jsx React.DOM */

/**
 * Displays the available resources.
 *
 * TODO: Unit tests.
 */
var ResourcePane = React.createClass({
    getInitialState: function() {
        return {resourceOptions: [], showModalResourceTemplateMetaData: false, data: null, rightClickedItem: null}
    },
    render: function() {
        var metaData = [{icon: "ui-icon-plusthick", title: "create", onClickCallback: this.createResource},
        			     {icon: "ui-icon-trash", title: "delete", onClickCallback: this.deleteResource}];
        var toolbar = <RToolbar className="resourcePane toolbarContainer" btnClassName="ui-button-text-only ui-button-height" metaData={metaData}/>;
        if (this.state.resourceOptions.length > 0) {
            return <div className="ResourcePane">
                       {toolbar}
                       <RListBox ref="listBox" options={this.state.resourceOptions} selectCallback={this.selectCallback}
                                 multiSelect={true} onContextMenu={this.onContextMenu} />

                       <RMenu ref="groupLevelWebAppsResourceMenu"
                              menuItems={[{key: "deploy", label: "deploy", menuItems: [{key: "deployToAllHosts", label: "all hosts"},
                                                                                       {key: "deployToAHosts", label: "a host"}]}]}
                              onItemClick = {this.onGroupLevelWebAppsResourceContextMenuItemClick}/>

                       <RMenu ref="deployResourceMenu" menuItems={[{key: "deploy", label: "deploy"}]}
                              onItemClick ={this.onDeployResourceContextMenuItemClick}/>

                       <ModalDialogBox ref="confirmDeployResourceDlg"
                                       okLabel="Yes"
                                       okCallback={this.deployResourceCallback}
                                       cancelLabel="No"
                                       position="fixed" />

                   </div>
        }

        return <div className="ResourcePane">
                   {toolbar}
                   <span>{this.state.data === null ? "Please select a JVM, Web Server or Web Application..." : "No resources found..."}</span>
               </div>
    },
    getData: function(data) {
        this.state.data = data; // We don't want the component to render that's why we just assign data via '='
        if (data !== null) {
            if (data.rtreeListMetaData.entity === "jvms") {
                this.props.jvmService.getResources(data.jvmName, this.getDataCallback);
            } else if (data.rtreeListMetaData.entity === "webServers") {
                this.props.wsService.getResources(data.name, this.getDataCallback);
            } else if (data.rtreeListMetaData.entity === "webApps" && data.rtreeListMetaData.parent.rtreeListMetaData.entity === "jvms") {
                this.props.webAppService.getResources(data.name, data.rtreeListMetaData.parent.jvmName, this.getDataCallback);
            } else if (data.rtreeListMetaData.entity === "webApps" && data.rtreeListMetaData.parent.rtreeListMetaData.entity === "webAppSection") {
                ServiceFactory.getResourceService().getAppResources(data.group.name, data.name, this.getDataCallback);
            } else if (data.rtreeListMetaData.entity === "webServerSection") {
                this.props.groupService.getGroupWebServerResources(data.rtreeListMetaData.parent.name, this.getDataCallback);
            } else if (data.rtreeListMetaData.entity === "jvmSection") {
                this.props.groupService.getGroupJvmResources(data.rtreeListMetaData.parent.name, this.getDataCallback);
            }
        }
    },
    getDataCallback: function(response) {
        var options = [];
        response.applicationResponseContent.forEach(function(resourceName){
            if (resourceName.entityType && resourceName.resourceName) {
                options.push({value:resourceName.resourceName, label:resourceName.resourceName, entityType: resourceName.entityType})
            } else {
                options.push({value: resourceName, label: resourceName});
            }
        });
        this.setState({resourceOptions: options});
    },
    selectCallback: function(value) {
         var groupJvmEntityType;
         this.state.resourceOptions.some(function(resource){
            if(resource.value && resource.value === value) {
                groupJvmEntityType = resource.entityType;
                return true;
            }
         });
         return this.props.selectCallback(value, groupJvmEntityType);
    },
    getSelectedValue: function() {
        if (this.refs.listBox !== undefined) {
            return this.refs.listBox.getSelectedValue();
        }
        return null;
    },
    createResource: function() {
        this.props.createResourceCallback(this.state.data);
    },
    deleteResource: function() {
        var resourceName = this.getSelectedValue();
        if (resourceName !== null) {
            this.props.deleteResourceCallback(resourceName);
        }
    },
    // Right click a resource is called onContextMenu event in js.
    onContextMenu: function(e, val) {
        this.state["rightClickedItem"] = val;
        if (this.state.data.rtreeListMetaData.entity === "webApps" && this.state.data.rtreeListMetaData.parent.rtreeListMetaData.entity === "webAppSection") {
            this.refs.groupLevelWebAppsResourceMenu.show((e.clientY - 5) + "px", (e.clientX - 5) + "px");
        } else {
            this.refs.deployResourceMenu.show((e.clientY - 5) + "px", (e.clientX - 5) + "px");
        }
    },
    onGroupLevelWebAppsResourceContextMenuItemClick: function(val) {
        console.log(val);
    },
    onDeployResourceContextMenuItemClick: function(val) {
        var name = this.state.data.name ? this.state.data.name : this.state.data.jvmName;
        var msg = 'Are you sure you want to deploy "' + this.state.rightClickedItem + '" to "' + name + '" ?';

        this.refs.confirmDeployResourceDlg.show("Deploy resource confirmation", msg);
    },
    deployResourceCallback: function() {
        console.log("deploy the resource!");
    }
});


