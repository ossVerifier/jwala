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
            selectedTreeListNodeMetaData: null
        }
    },
    render: function() {
        if (this.state.groupData === null) {
            return <div>Loading data...</div>
        }

        var treeMetaData = {propKey: "name",
                            children:[{entity: "webServers", propKey: "name", selectable: true},
                                      {entity: "jvms", propKey: "jvmName", selectable: true,
                                            children:[{entity: "webApps", propKey: "name", selectable: true}]}]};

        var groupJvmTreeList = <RStaticDialog ref="groupsDlg"
                                              title="Groups"
                                              contentClassName="resource-static-dialog-content">
                                   <RTreeList ref="treeList"
                                              data={this.state.groupData}
                                              treeMetaData={treeMetaData}
                                              expandIcon="public-resources/img/icons/plus.png"
                                              collapseIcon="public-resources/img/icons/minus.png"
                                              selectNodeCallback={this.selectNodeCallback} />
                               </RStaticDialog>

        var resourcesPane = <RStaticDialog title="Resources" contentClassName="resource-static-dialog-content">
                                <ResourcePane ref="resourcePane"
                                              jvmService={this.props.jvmService}
                                              wsService={this.props.wsService}
                                              webAppService={this.props.webAppService}/>
                            </RStaticDialog>

        var resourceAttrPane = <RStaticDialog title="Properties and Values" contentClassName="resource-static-dialog-content">
                                   <ResourceAttrPane ref="resourceAttrPane" />
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
            this.setState({groupData:this.groupData});
        }
    },
    getJvmDataCallback: function(jvm, applicationResponseContent) {
        this.dataRetrievalCount--;
        jvm["webApps"] = applicationResponseContent;
        if (this.dataRetrievalCount === 0) {
            this.setState({groupData:this.groupData});
        }
    },
    getResourceTypesCallback: function(response) {
        this.setState({resourceTypes:response.applicationResponseContent});
    },
    selectNodeCallback: function(data) {
        this.refs.resourcePane.getData(data);
        this.refs.resourceAttrPane.showAttributes(data);
    },
});