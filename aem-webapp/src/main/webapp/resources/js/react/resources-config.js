/** @jsx React.DOM */
var ResourcesConfig = React.createClass({

    getInitialState: function() {
        return {
            groupJvmData: null
        }
    },

    selectNodeCallback: function() {
    },

    render: function() {

        if (this.state.groupJvmData === null) {
            return <div>Loading groups and JVMs...</div>
        }

        var treeMetaData = [{propKey: "name", selectable: true}, {entity: "jvms", propKey: "jvmName", selectable: true}];
        var groupJvmTreeList = <RStaticDialog title="JVMs" className="group-jvms-tree-list-dialog">
                                   <RTreeList data={this.state.groupJvmData}
                                              treeMetaData={treeMetaData}
                                              expandIcon="public-resources/img/icons/plus.png"
                                              collapseIcon="public-resources/img/icons/minus.png"
                                              selectNodeCallback={this.selectNodeCallback} />
                               </RStaticDialog>

        var resourceTypes = [{name: "Datasource"}, {name: "JMS Connection Factory"}, {name: "JMS Queue"}, {name: "JMS Topic"}];

        var resourcesPane = <RStaticDialog title="Resources" className="">
                                <AddEditDeleteResources resourceTypes={resourceTypes} resourceList={[]} />
                            </RStaticDialog>

        var horzComponents = [];
        horzComponents.push(<div className="group-jvms-tree-list-container">{groupJvmTreeList}</div>);
        horzComponents.push(<div className="">{resourcesPane}</div>);
        horzComponents.push(<div className=""></div>);

        var horzSplitter = <RSplitter components={horzComponents}
                                      orientation={RSplitter.HORIZONTAL_ORIENTATION}
                                      panelDimensions={[{width:"33.33%", height:"100%"},
                                                        {width:"33.33%", height:"100%"},
                                                        {width:"33.33%", height:"100%"}]} />

        var vertComponents = [];

        vertComponents.push(<div className="group-jvms-tree-list-container">{horzSplitter}</div>);
        vertComponents.push(<div className="group-jvms-tree-list-container"><XmlTabs/></div>);

        var vertSplitter = <RSplitter components={vertComponents} orientation={RSplitter.VERTICAL_ORIENTATION}/>

        return <div className="resource-container">{vertSplitter}</div>
    },

    componentDidMount: function() {
        ServiceFactory.getGroupService().getGroups(this.getGroupJvmCallback);
    },

    getGroupJvmCallback: function(response) {
        this.setState({groupJvmData:response.applicationResponseContent});
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
