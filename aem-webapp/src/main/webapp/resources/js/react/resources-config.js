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

        var treeMetaData = [{propKey: "name"}, {entity: "jvms", propKey: "jvmName", selectable: true}];
        var groupJvmTreeList = <RStaticDialog title="JVMs" className="group-jvms-tree-list-dialog">
                                   <RTreeList data={this.state.groupJvmData}
                                              treeMetaData={treeMetaData}
                                              expandIcon="public-resources/img/icons/plus.png"
                                              collapseIcon="public-resources/img/icons/minus.png"
                                              selectNodeCallback={this.selectNodeCallback} />
                               </RStaticDialog>

        var horzComponents = [];
        horzComponents.push(<div className="group-jvms-tree-list-container">{groupJvmTreeList}</div>);
        horzComponents.push(<div className="group-jvms-tree-list-container"></div>);
        horzComponents.push(<div className="group-jvms-tree-list-container"></div>);

        var horzSplitter = <RSplitter components={horzComponents}
                                      orientation={RSplitter.HORIZONTAL_ORIENTATION}
                                      panelDimensions={[{width:"46%", height:"100%"},
                                                        {width:"27%", height:"100%"},
                                                        {width:"27%", height:"100%"}]} />

        var vertComponents = [];

        vertComponents.push(<div className="group-jvms-tree-list-container">{horzSplitter}</div>);
        vertComponents.push(<div className="group-jvms-tree-list-container"></div>);

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