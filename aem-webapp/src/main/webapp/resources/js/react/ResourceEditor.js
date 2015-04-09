/** @jsx React.DOM */
var ResourceEditor = React.createClass({
    getInitialState: function() {
        return {
            groupData: null,
            currentGroupName: null,
            resourceData:null,
            currentResourceName: null,
            resourceEditMode: false
        }
    },
    render: function() {
        if (this.state.groupData === null) {
            return <div>Loading group data...</div>
        }

        var treeMetaData = [{propKey: "name", selectable: true}];
        var groupJvmTreeList = <RStaticDialog ref="groupsDlg"
                                              title="Groups"
                                              contentClassName="resource-static-dialog-content">
                                   <RTreeList ref="treeList"
                                              data={this.state.groupData}
                                              treeMetaData={treeMetaData}
                                              expandIcon="public-resources/img/icons/plus.png"
                                              collapseIcon="public-resources/img/icons/minus.png"
                                              selectNodeCallback={this.selectGroupNodeCallback} />
                               </RStaticDialog>

        var resourcesPane = <RStaticDialog title="Resources" contentClassName="resource-static-dialog-content">
                                <ResourcePane groupName={this.state.currentGroupName}
                                              data={this.state.resourceData}
                                              insertNewResourceCallback={this.insertNewResourceCallback}
                                              deleteResourcesCallback={this.deleteResourcesCallback}
                                              updateResourceCallback={this.updateResourceCallback}
                                              currentResourceName={this.state.currentResourceName}
                                              editMode={this.state.resourceEditMode}
                                              selectResourceCallback={this.selectResourceCallback}/>
                            </RStaticDialog>

        var resourceAttrPane = <RStaticDialog title="Attributes and Values" contentClassName="resource-static-dialog-content">
                                   <ResourceAttrPane ref="resourceAttrEditor" />
                               </RStaticDialog>

        var splitterComponents = [];
        splitterComponents.push(<div className="resource-pane">{groupJvmTreeList}</div>);
        splitterComponents.push(<div className="resource-pane">{resourcesPane}</div>);
        splitterComponents.push(<div className="resource-pane">{resourceAttrPane}</div>);

        return <RSplitter components={splitterComponents}
                          orientation={RSplitter.HORIZONTAL_ORIENTATION}
                          panelDimensions={[{width:"33.33%", height:"100%"},
                                            {width:"33.33%", height:"100%"},
                                            {width:"33.33%", height:"100%"}]} />
    },
    componentDidMount: function() {
        ServiceFactory.getGroupService().getGroups(this.getGroupDataCallback);
    },
    getGroupDataCallback: function(response) {
        this.setState({groupData:response.applicationResponseContent});
    },
    selectResourceCallback: function(resource) {
        // this.refs.resourceAttrEditor.refresh(resource);
    },
    componentDidUpdate: function() {
        if (this.state.currentGroupName === null && this.state.groupData !== null && this.state.groupData.length > 0) {
            // Select the first group
            this.refs.treeList.selectNode(this.state.groupData[0].name);
            this.setState({currentGroupName:this.state.groupData[0].name});
        }
    },
    refreshResourceListResponseCallback: function(resource) {
    },
    selectGroupNodeCallback: function(group) {
        this.setState({currentGroupName:group.name});
    },
    componentWillUpdate: function(nextProps, nextState) {
        if (this.state.currentGroupName != nextState.currentGroupName) {
            // Retrieve resources
            ServiceFactory.getResourceService().getResources(nextState.currentGroupName, this.getResourceDataCallback);
        }
    },
    getResourceDataCallback: function(response) {
        this.setState({resourceData:response.applicationResponseContent});
    },
    insertNewResourceCallback: function(resourceName) {
        ServiceFactory.getResourceService().getResources(this.state.currentGroupName,
            this.getResourceDataCallbackExt.bind(this, resourceName, true));
    },
    deleteResourcesCallback: function() {
        ServiceFactory.getResourceService().getResources(this.state.currentGroupName, this.getResourceDataCallback);
    },
    updateResourceCallback: function(newResourceName) {
        ServiceFactory.getResourceService().getResources(this.state.currentGroupName,
            this.getResourceDataCallbackExt.bind(this, newResourceName, false));
    },
    getResourceDataCallbackExt: function(resourceName, editMode, response) {
        this.setState({currentResourceName:resourceName,
                       resourceEditMode:editMode,
                       resourceData:response.applicationResponseContent});
    },
    selectResourceCallback: function(resource) {
        this.setState({currentResourceName:resource.name});
    }
});