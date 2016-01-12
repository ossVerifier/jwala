/** @jsx React.DOM */

/**
 * The component that lets user edit resource files.
 *
 * TODO: Unit tests.
 */
var ResourceEditor = React.createClass({
    getInitialState: function() {
        return {
            groupData: null
        }
    },
    render: function() {
        if (this.state.groupData === null) {
            return <div>Loading data...</div>
        }

        var treeMetaData = {entity: "groups",
                            propKey: "name",
                            icon: "public-resources/img/icons/group.png",
                            children:[{entity: "webServerSection", propKey: "key" , label: "name", icon: "public-resources/img/icons/webserver.png",
                                       children:[{entity: "webServers", propKey: "name", selectable: true}]},
                                      {entity: "jvmSection", propKey: "key", label: "name", icon: "public-resources/img/icons/appserver.png",
                                       children:[{entity: "jvms", propKey: "jvmName", selectable: true,
                                                  children:[{entity: "webApps", propKey: "name", selectable: true,
                                                             icon: "public-resources/img/icons/webapp.png"}]}]}],
                            };

        var groupJvmTreeList = <RStaticDialog ref="groupsDlg"
                                              title="Topology"
                                              contentClassName="resource-static-dialog-content-1"
                                              className="resource-box">
                                   <RTreeList ref="treeList"
                                              data={this.state.groupData}
                                              treeMetaData={treeMetaData}
                                              expandIcon="public-resources/img/icons/plus.png"
                                              collapseIcon="public-resources/img/icons/minus.png"
                                              selectNodeCallback={this.selectNodeCallback} />
                               </RStaticDialog>

        var resourcesPane = <RStaticDialog title="Resources" contentClassName="resource-static-dialog-content-2"
                                           className="resource-box">
                                <ResourcePane ref="resourcePane"
                                              jvmService={this.props.jvmService}
                                              wsService={this.props.wsService}
                                              webAppService={this.props.webAppService}
                                              selectCallback={this.selectResourceCallback}/>
                            </RStaticDialog>

        var resourceAttrPane = <RStaticDialog title="Properties and Values" contentClassName="resource-static-dialog-content-3"
                                              className="resource-box">
                                   <ResourceAttrPane ref="resourceAttrPane" />
                               </RStaticDialog>

        var splitterComponents = [];
        splitterComponents.push(<div className="resource-pane">{groupJvmTreeList}</div>);
        splitterComponents.push(<div className="resource-pane">{resourcesPane}</div>);
        splitterComponents.push(<div className="resource-pane">{resourceAttrPane}</div>);

        return <RSplitter components={splitterComponents}
                          orientation={RSplitter.HORIZONTAL_ORIENTATION}
                          panelDimensions={[{width:"44%", height:"100%"},
                                            {width:"12%", height:"100%"},
                                            {width:"44%", height:"100%"}]} />
    },
    componentDidMount: function() {
        this.props.groupService.getGroups(this.getGroupDataCallback, "webServers=true");
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
            this.refs.resourceAttrPane.showAttributes(data);
            return true;
        }
        return false;
    },
    selectResourceCallback: function(value) {
        return this.props.selectResourceTemplateCallback(this.refs.treeList.getSelectedNodeData(), value);
    }
});