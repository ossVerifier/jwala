/** @jsx React.DOM */
var ResourcesConfig = React.createClass({

    getInitialState: function() {
        return {
            groupData: null,
            currentGroupName: null
        }
    },
    selectNodeCallback: function(group) {
        this.refs.resourceEditor.refreshResourceList(group.name);
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
                                <ResourceEditor ref="resourceEditor"
                                                selectResourceCallback={this.selectResourceCallback}
                                                refreshResourceListResponseCallback={this.refreshResourceListResponseCallback} />
                            </RStaticDialog>

        var resourceAttrPane = <RStaticDialog title="Attributes and Values" className="splitter-top-component-child">
                                   <ResourceAttrEditor ref="resourceAttrEditor" />
                               </RStaticDialog>

        var horzComponents = [];
        horzComponents.push(<div className="group-jvms-tree-list-container">{groupJvmTreeList}</div>);
        horzComponents.push(<div className="">{resourcesPane}</div>);
        horzComponents.push(<div className="">{resourceAttrPane}</div>);

        var horzSplitter = <RSplitter components={horzComponents}
                                      orientation={RSplitter.HORIZONTAL_ORIENTATION}
                                      panelDimensions={[{width:"33.33%", height:"100%"},
                                                        {width:"33.33%", height:"100%"},
                                                        {width:"33.33%", height:"100%"}]} />

        var vertComponents = [];

        vertComponents.push(<div>{horzSplitter}</div>);
        vertComponents.push(<div><XmlTabs/></div>);

        var vertSplitter = <RSplitter components={vertComponents} orientation={RSplitter.VERTICAL_ORIENTATION}/>

        return <div className="resource-container">{vertSplitter}</div>
    },

    selectResourceCallback: function(resource) {
        this.refs.resourceAttrEditor.refresh(resource);
    },

    componentDidMount: function() {
        ServiceFactory.getGroupService().getGroups(this.getGroupDataCallback);
    },
    getGroupDataCallback: function(response) {
        this.setState({groupData:response.applicationResponseContent});
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
    }
})

var XmlTabs = React.createClass({
    getInitialState: function() {
        return {
            tokenizedXml: '<Resource name="jdbc/toc-xa" auth="Container" type="com.atomikos.jdbc.AtomikosDataSourceBean" factory="com.siemens.cto.infrastructure.atomikos.EnhancedTomcatAtomikosBeanFactory"/>',
            untokenizedXml: '<Resource name="${name}" auth="${contaner}" type="${type}" factory="${factory}"/>'
        };
    },
    render: function() {
        var xmlTabItems = [{title: "Tokenized", content:<RXmlEditor content={this.state.tokenizedXml}/>},
                           {title: "Untokenized", content:<RXmlEditor content={this.state.untokenizedXml}/>}];
        return <Tabs theme="default" items={xmlTabItems} depth="0"/>
    }
});
