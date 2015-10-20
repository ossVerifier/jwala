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
                if ( entityType === "webServers" || entityType === "jvms") {
                    var jvmArray = [];
                    var webApps = {};
                    for (var jvmIdx = 0; jvmIdx < this.state.attributes[attr].parent.jvms.length; jvmIdx++) {
                        var jvm = this.state.attributes[attr].parent.jvms[jvmIdx];

                        jvmArray.push(jvm);
                        if (jvm.webApps !== undefined && jvm.webApps.length > 0) {
                            jvm.webApps.forEach(function(webApp) {
                                webApps[webApp.name] = webApp;
                            });
                        }
                    }

                    reactAttributeElements.push(<tr><td colSpan="2"><JvmTable attributes={jvmArray}/></td></tr>);
                    if (entityType === "webServers") {
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

            if (typeof(this.state.attributes[attr]) !== "object") {
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
    showAttributes: function(data) {
        this.setState({attributes: data});
    }
})

var Attribute = React.createClass({
    render: function() {
        return <tr><td>{"${" + this.props.entity + "." + this.props.property + "}"}</td><td>{this.props.value.toString()}</td></tr>;
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
            return <div style={{cursor: "pointer"}} onClick={this.onClick}>{"+ ${apps}"}</div>
        }

        var reactAttributeElements = [];

        var webAppIdx = 0;
        for (key in this.props.attributes) {
            for (attr in this.props.attributes[key]) {
                if (typeof(this.props.attributes[key][attr]) !== "object") {
                    reactAttributeElements.push(React.createElement(Attribute,
                                                        {entity: "apps[" + webAppIdx + "]", key: attr + webAppIdx, property: attr, value: this.props.attributes[key][attr]}));
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
