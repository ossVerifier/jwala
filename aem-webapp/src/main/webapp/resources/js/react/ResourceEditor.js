/** @jsx React.DOM */
var ResourceEditor = React.createClass({
    getInitialState: function() {
        return {
            groupData: null,
            currentGroupName: null
        }
    },
    render: function() {
        if (this.state.groupData === null) {
            return <div>Loading group data...</div>
        }

        var treeMetaData = [{propKey: "name", selectable: true}];
        var groupJvmTreeList = <RStaticDialog ref="groupsDlg"
                                              title="Groups"
                                              className="splitter-top-component-child">
                                   <RTreeList ref="treeList"
                                              data={this.state.groupData}
                                              treeMetaData={treeMetaData}
                                              expandIcon="public-resources/img/icons/plus.png"
                                              collapseIcon="public-resources/img/icons/minus.png"
                                              selectNodeCallback={this.selectNodeCallback} />
                               </RStaticDialog>

        var resourcesPane = <RStaticDialog title="Resources" className="splitter-top-component-child">
                                <ResourcePane ref="resourceEditor"
                                                selectResourceCallback={this.selectResourceCallback}
                                                refreshResourceListResponseCallback={this.refreshResourceListResponseCallback} />
                            </RStaticDialog>

        var resourceAttrPane = <RStaticDialog title="Attributes and Values" className="splitter-top-component-child">
                                   <ResourceAttrPane ref="resourceAttrEditor" />
                               </RStaticDialog>

        var splitterComponents = [];
        splitterComponents.push(<div className="group-jvms-tree-list-container">{groupJvmTreeList}</div>);
        splitterComponents.push(<div className="">{resourcesPane}</div>);
        splitterComponents.push(<div className="">{resourceAttrPane}</div>);

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
        this.refs.resourceAttrEditor.refresh(resource);
    },
    componentDidUpdate: function() {
        if (this.state.currentGroupName === null && this.state.groupData !== null && this.state.groupData.length > 0) {
            // Select the first group
            this.refs.treeList.selectNode(this.state.groupData[0].name);
            this.setState({currentGroupName:this.state.groupData[0].name});
            ServiceFactory.getResourceService().getResources(this.state.groupData[0].name, this.getResourcesCallback);
        }
    },
    refreshResourceListResponseCallback: function(resource) {
    },
    selectNodeCallback: function(group) {
        this.refs.resourceEditor.refreshResourceList(group.name);
    }
});