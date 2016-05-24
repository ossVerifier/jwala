/** @jsx React.DOM */

/**
 * The component that lets user edit resource files.
 *
 * TODO: Unit tests.
 */
var ResourceEditor = React.createClass({
    getInitialState: function() {
        return {
            groupData: null,
            resourceAttrData: null
        }
    },
    render: function() {
        if (this.state.groupData === null) {
            return <div>Loading data...</div>
        }

        var treeMetaData = {entity: "groups",
                            propKey: "name",
                            icon: "public-resources/img/icons/group.png",
                            children:[{entity: "webServerSection", propKey: "key" , label: "name", icon: "public-resources/img/icons/webserver.png", selectable: true,
                                       children:[{entity: "webServers", propKey: "name", selectable: true}]},
                                      {entity: "jvmSection", propKey: "key", label: "name", icon: "public-resources/img/icons/appserver.png", selectable: true,
                                       children:[{entity: "jvms", propKey: "jvmName", selectable: true,
                                                  children:[{entity: "webApps", propKey: "name", selectable: true}]}]},
                                       {entity: "webAppSection", propKey: "key", label: "name", icon: "public-resources/img/icons/webapp.png", selectable: false,
                                        children:[{entity: "webApps", propKey: "name", selectable: true}]}]
                            };

        var groupJvmTreeList = <RStaticDialog ref="groupsDlg" title="Topology" defaultContentHeight="283px">
                                   <RTreeList ref="treeList"
                                              data={this.state.groupData}
                                              treeMetaData={treeMetaData}
                                              expandIcon="public-resources/img/icons/plus.png"
                                              collapseIcon="public-resources/img/icons/minus.png"
                                              selectNodeCallback={this.selectNodeCallback}
                                              collapsedByDefault={true}/>
                               </RStaticDialog>

        var resourcesPane = <RStaticDialog ref="resourceFileDlg" title="Resources" defaultContentHeight="283px">
                                <ResourcePane ref="resourcePane"
                                              jvmService={this.props.jvmService}
                                              wsService={this.props.wsService}
                                              webAppService={this.props.webAppService}
                                              groupService={this.props.groupService}
                                              selectCallback={this.selectResourceCallback}
                                              createResourceCallback={this.props.createResourceCallback}
                                              deleteResourceCallback={this.props.deleteResourceCallback}/>
                            </RStaticDialog>

        var resourceAttrPane = <RStaticDialog ref="resourceAttrDlg" title="Properties and Values" defaultContentHeight="283px">
                                   <ResourceAttrPane ref="resourceAttrPane" />
                               </RStaticDialog>

        var splitterComponents = [];
        splitterComponents.push(groupJvmTreeList);
        splitterComponents.push(resourcesPane);
        splitterComponents.push(resourceAttrPane);

        return <RSplitter ref="mainSplitter"
                          components={splitterComponents}
                          orientation={RSplitter.HORIZONTAL_ORIENTATION}
                          onSplitterChange={this.onChildSplitterChangeCallback}
                          panelDimensions={[{width:"44%", height:"100%"},
                                            {width:"12%", height:"100%"},
                                            {width:"44%", height:"100%"}]} />
    },
    componentDidMount: function() {
        this.props.resourceService.getResourceTopology().then(this.getGroupDataCallback);
    },

    dataRetrievalCount: 1 /* We can't make this into a state since it's being used by asynchronous callbacks! */,
    groupData: null,

    getGroupDataCallback: function(response) {
        var self = this;
        this.dataRetrievalCount = 0; // set to zero since group data retrieval is done at this point

        if (response.applicationResponseContent !== undefined) {
            this.groupData = response.applicationResponseContent.groups;
        }

        console.log(this.groupData);

        if (this.dataRetrievalCount === 0) {
            // Add the sections e.g. jvmSection
            this.setGroupData(this.groupData);
        }
    },
    setGroupData: function(groupData) {
        // Transform group data to contain a jvm and a web server section so that jvms and web server data will show up
        // under the said sections.
        groupData.forEach(function(theGroupData) {
            theGroupData["jvmSection"] = [{key: theGroupData.name + "JVMs", name: "JVMs", jvms: theGroupData.jvms}];
            theGroupData["webServerSection"] = [{key: theGroupData.name + "WebServers", name: "Web Servers",
                webServers: theGroupData.webServers}];
            theGroupData["webAppSection"] = [{key: theGroupData.name + "WebApps", name: "Web Apps", webApps: theGroupData.applications}];
        });

        this.setState({groupData:groupData});
    },
    selectNodeCallback: function(data, entityName, parent) {
        if (this.props.selectEntityCallback(data, null)) {
            this.refs.resourcePane.getData(data);
            this.refs.resourceAttrPane.setCurrentlySelectedEntityData(data, entityName, parent);
            return true;
        }
        return false;
    },
    selectResourceCallback: function(value, groupJvmEntityType) {
        return this.props.selectResourceTemplateCallback(this.refs.treeList.getSelectedNodeData(), value, groupJvmEntityType);
    },
    onParentSplitterChange: function(dimensions) {
        this.refs.groupsDlg.recomputeContentContainerSize(dimensions[0]);
        this.refs.resourceFileDlg.recomputeContentContainerSize(dimensions[0]);
        this.refs.resourceAttrDlg.recomputeContentContainerSize(dimensions[0]);
    },
    onChildSplitterChangeCallback: function(dimensions) {
        if (dimensions[0]) {
            this.refs.groupsDlg.recomputeContentContainerSize(dimensions[0]);
        }

        if (dimensions[1]) {
            this.refs.resourceFileDlg.recomputeContentContainerSize(dimensions[1]);
        }

        if (dimensions[2]) {
            this.refs.resourceAttrDlg.recomputeContentContainerSize(dimensions[2]);
        }
    }
});