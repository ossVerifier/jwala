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
                                                  children:[{entity: "webApps", propKey: "name", selectable: true,
                                                             icon: "public-resources/img/icons/webapp.png"}]}]}],
                            };

        var groupJvmTreeList = <RStaticDialog ref="groupsDlg" title="Topology" defaultContentHeight="283px">
                                   <RTreeList ref="treeList"
                                              data={this.state.groupData}
                                              treeMetaData={treeMetaData}
                                              expandIcon="public-resources/img/icons/plus.png"
                                              collapseIcon="public-resources/img/icons/minus.png"
                                              selectNodeCallback={this.selectNodeCallback} />
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
        this.props.groupService.getGroups("webServers=true").then(this.getGroupDataCallback);
        this.props.resourceService.getResourceTypes(this.getResourceTypesCallback);
    },

    dataRetrievalCount: 1 /* We can't make this into a state since it's being used by asynchronous callbacks! */,
    groupData: null,

    getGroupDataCallback: function(response) {
        var self = this;
        this.dataRetrievalCount = 0; // set to zero since group data retrieval is done at this point

        if (response.applicationResponseContent !== undefined) {
            this.groupData = response.applicationResponseContent;
            this.groupData.forEach(function(group) {
                group.jvms.forEach(function(jvm){
                    self.dataRetrievalCount++;
                    self.props.webAppService.getWebAppsByJvm(jvm.id.id, function(response){
                        self.getJvmDataCallback(jvm, response.applicationResponseContent);
                    });
                });
            });
        }

        if (this.dataRetrievalCount === 0) {
            this.setGroupData(this.groupData);
        }
    },
    getJvmDataCallback: function(jvm, applicationResponseContent) {
        this.dataRetrievalCount--;
        jvm["webApps"] = applicationResponseContent;
        if (this.dataRetrievalCount === 0) {
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
        });

        this.setState({groupData:groupData});
    },
    getResourceTypesCallback: function(response) {
        this.setState({resourceTypes:response.applicationResponseContent});
    },
    selectNodeCallback: function(data) {
        if (this.props.selectEntityCallback(data, null)) {
            this.refs.resourcePane.getData(data);
            this.refs.resourceAttrPane.setCurrentlySelectedEntityData(data);
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