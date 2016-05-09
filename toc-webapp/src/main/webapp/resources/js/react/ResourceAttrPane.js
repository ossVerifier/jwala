/** @jsx React.DOM */

/**
 * Shows the properties and values of a JVM, Web Server or Web Application.
 *
 * TODO: Unit tests.
 */
var ResourceAttrPane = React.createClass({
    getInitialState: function() {
        return {attributes: null};
    },

    render: function() {
        var attributes = this.state.attributes;
        if (attributes === null) {
            return <div>Loading attributes...</div>;
        }
        var reactAttributeElements = [];
        for (var key in attributes) {
            reactAttributeElements.push(<Attribute entity={key} value={attributes[key]}/>);
        }
        return <RJsonDataTreeDisplay ref="attrTree" data={attributes} onShowToolTipCallback={this.onShowAttrTreeToolTipCallback}/>
    },
    onShowAttrTreeToolTipCallback: function(hierarchy) {
        return <div><span style={{display: "inline-block"}} className="ui-icon ui-icon-clipboard" /><span>{"${" + hierarchy + "}"}</span></div>;
    },
    componentDidMount: function() {
        // TODO: Decide whether we should call the service directly or pass it as a property.
        var self = this;
        resourceService.getResourceAttrData()
            .then(function(response) {
                self.setState({attributes: response.applicationResponseContent});
            })
            .caught(function(e){
                alert(e);
            });
    },

    // TODO: Remove when new code that replaces this is complete.
    render_: function() {
        if (this.state.attributes === null ) {
            return <div className="attr-list ui-widget-content" style={{padding: "2px 2px"}}><span>Please select a JVM, Web Server or Web Application...</span></div>;
        }

        var entity = this.state.attributes.rtreeListMetaData.entity ;

        // By design the entity name ends with 's' as in JVMs, webServers or webApps.
        // We need to prefix the attributes with the entity name e.g. jvm.name so we need to strip the 's'.
        entity = entity.substring(0, entity.length - 1);

        var reactAttributeElements = [];
        for (attr in this.state.attributes) {

            var children;
            if (attr === "rtreeListMetaData") {
                var entityType = this.state.attributes[attr].entity;
                if ( entityType === "webServers" || entityType === "jvms" || entityType === "webServerSection" || entityType === "jvmSection") {
                    var wsArray = [];
                    var jvmArray = [];
                    var webApps = {};

                    // Web Servers
                    if (this.state.attributes[attr].parent.webServers !== undefined) {
                        for (var wsIdx = 0; wsIdx < this.state.attributes[attr].parent.webServers.length; wsIdx++) {
                            var webServer = this.state.attributes[attr].parent.webServers[wsIdx];
                            wsArray.push(webServer);
                        }
                    }

                    // JVMs
                    if (this.state.attributes[attr].parent.jvms !== undefined) {
                        for (var jvmIdx = 0; jvmIdx < this.state.attributes[attr].parent.jvms.length; jvmIdx++) {
                            var jvm = this.state.attributes[attr].parent.jvms[jvmIdx];

                            jvmArray.push(jvm);

                            // Web Applications
                            if (jvm.webApps !== undefined && jvm.webApps.length > 0) {
                                jvm.webApps.forEach(function(webApp) {
                                    webApps[webApp.name] = webApp;
                                });
                            }
                        }
                    }

                    if (wsArray.length > 0) {
                        reactAttributeElements.push(<tr><td colSpan="2"><WebServerTable attributes={wsArray}/></td></tr>);
                    }

                    if (jvmArray.length > 0) {
                        reactAttributeElements.push(<tr><td colSpan="2"><JvmTable attributes={jvmArray}/></td></tr>);
                    }

                    if ((entityType === "webServers" || entityType === "webServerSection" || entityType === "jvmSection") && Object.keys(webApps).length > 0) {
                        reactAttributeElements.push(<tr><td colSpan="2"><WebAppTable attributes={webApps} /></td></tr>);
                    }
                } else if (entityType === "webApps") {
                    console.log(this.state.attributes[attr]);
                    console.log(this.state.attributes[attr].parent);


                    for (parentAttr in this.state.attributes[attr].parent) {
                        if (typeof(this.state.attributes[attr].parent[parentAttr]) !== "object") {
                            reactAttributeElements.push(<tr><td>{"${webApp.jvm." + parentAttr + "}"}</td>
                                                            <td>{this.state.attributes[attr].parent[parentAttr]}</td></tr>);
                        }
                    }



                }
            }

            if (typeof(this.state.attributes[attr]) !== "object" && entity !== "webServerSectio" && entity !== "jvmSectio") {
                reactAttributeElements.push(React.createElement(Attribute,
                                                    {entity: entity, key: attr, property: attr, value: this.state.attributes[attr]}));
            }
        }

        return <div className="attr-list ui-widget-content">
                    <table className="attr-table">
                        <thead>
                            <th>Property</th><th>Value</th>
                        </thead>
                        <tbody>{reactAttributeElements}</tbody>
                    </table>
               </div>;
    },
    setCurrentlySelectedEntityData: function(data) {
        if (this.state.attributes) {
            this.state.attributes["this"] = data;
            this.refs.attrTree.refresh(this.state.attributes);
        }
    }
})

var Attribute = React.createClass({
    render: function() {
        if (this.props.property === undefined) {
            return <tr><td>{"+" + this.props.entity}</td><td>{"Array[" + this.props.value.length + "]"} </td></tr>;
        }
        return <tr><td>{"${" + this.props.entity + "." + this.props.property + "}"}</td><td>{this.props.value.toString()}</td></tr>;
    }
});

var WebServerTable = React.createClass({
    getInitialState: function() {
        return {isCollapsed: true};
    },
    render: function() {
        if (this.state.isCollapsed) {
            return <div style={{cursor: "pointer"}} onClick={this.onClick}>{"+ ${webServers}"}</div>
        }

        var reactAttributeElements = [];
        for (var i = 0; i < this.props.attributes.length; i++) {
            for (attr in this.props.attributes[i]) {
                if (typeof(this.props.attributes[i][attr]) !== "object") {
                    reactAttributeElements.push(React.createElement(Attribute,
                                                        {entity: "webServers[" + i + "]", key: attr + i, property: attr,
                                                         value: this.props.attributes[i][attr]}));
                }
            }
        }

        return <div className="ws-table-container">
                   <div style={{cursor: "pointer"}} onClick={this.onClick}>{"- ${webServers}"}</div>
                   <table className="ws-table">
                      <tbody>
                          {reactAttributeElements}
                      </tbody>
                  </table>
               </div>

    },
    onClick: function() {
        this.setState({isCollapsed: !this.state.isCollapsed});
    }
});

var JvmTable = React.createClass({
    getInitialState: function() {
        return {isCollapsed: true};
    },
    render: function() {
        if (this.state.isCollapsed) {
            return <div style={{cursor: "pointer"}} onClick={this.onClick}>{"+ ${jvms}"}</div>
        }

        var reactAttributeElements = [];
        for (var i = 0; i < this.props.attributes.length; i++) {
            for (attr in this.props.attributes[i]) {
                if (typeof(this.props.attributes[i][attr]) !== "object") {
                    reactAttributeElements.push(React.createElement(Attribute,
                                                        {entity: "jvms[" + i + "]", key: attr + i, property: attr,
                                                         value: this.props.attributes[i][attr]}));
                }
            }
        }

        return <div className="jvm-table-container">
                   <div style={{cursor: "pointer"}} onClick={this.onClick}>{"- ${jvms}"}</div>
                   <table className="jvm-table">
                      <tbody>
                          {reactAttributeElements}
                      </tbody>
                  </table>
               </div>

    },
    onClick: function() {
        this.setState({isCollapsed: !this.state.isCollapsed});
    }
});

var WebAppTable = React.createClass({
    getInitialState: function() {
        return {isCollapsed: true};
    },
    render: function() {
        if (this.state.isCollapsed) {
            return <div style={{cursor: "pointer"}} onClick={this.onClick}>{"+ ${webApps}"}</div>
        }

        var reactAttributeElements = [];

        var webAppIdx = 0;
        for (key in this.props.attributes) {
            for (attr in this.props.attributes[key]) {
                if (typeof(this.props.attributes[key][attr]) !== "object") {
                    reactAttributeElements.push(React.createElement(Attribute,
                                                        {entity: "webApps[" + webAppIdx + "]", key: attr + webAppIdx, property: attr, value: this.props.attributes[key][attr]}));
                }
            }
            webAppIdx++;
        }

        return <div className="webapp-table-container">
                   <div style={{cursor: "pointer"}} onClick={this.onClick}>{"- ${webApps}"}</div>
                   <table className="webapp-table">
                       <tbody>
                           {reactAttributeElements}
                       </tbody>
                   </table>
               </div>

    },
    onClick: function() {
        this.setState({isCollapsed: !this.state.isCollapsed});
    }
});