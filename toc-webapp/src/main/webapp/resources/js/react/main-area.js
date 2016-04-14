/** @jsx React.DOM */
var MainArea = React.createClass({
     render: function() {
        return <div className={this.props.className}>
                    <div id="txt"></div>
                    <table className="main-area-table">
                        <tr>
                            <td><Banner/><br/><br/></td>
                        </tr>
                        <tr>
                            <td>
                                <div id="loading" style={{display:"none"}}>
                                    <br/>
                                    <img src="public-resources/img/gears-2d.gif"/>
                                </div>
                                <MainTabs/>
                            </td>
                        </tr>
                    </table>
                    <div className="preload">
                        {/* This fixes icon loading issues when opening a row with many items e.g. jvms, web servers and apps */}
                        <img src="public-resources/img/react/gear-icon.png" />
                        <img src="public-resources/img/icons/heap-dump.png" />
                        <img src="public-resources/img/icons/thread-dump.png" />
                        <img src="public-resources/img/icons/mgr.png" />
                        <img src="public-resources/img/blue-and-light-blue-gears.gif"/>
                    </div>
               </div>
    },
    statics: {
        unsavedChanges: false
    }
});

var Banner = React.createClass({
    render: function() {
        return <div className="banner-container ui-widget-header">
                    <img src="public-resources/img/aem-gears-fade-right.png"/>
                    <span className="banner-logout"><a href="#" onClick={this.handleLogoutClick}>Logout</a></span>
                    <div className="banner-title">Tomcat Operations Center</div>
               </div>
    },
    handleLogoutClick: function() {
        ServiceFactory.getUserService().logout();
    }
});

var MainTabs = React.createClass({
    getInitialState:function() {
        return {items: []};
    },
    render: function() {
        if (this.state.items.length > 0) {
            return <Tabs theme="default" items={this.state.items} depth="0"/>
        }
        return <div>Loading tab items...</div>
    },
    componentDidMount: function() {
        var items = [{title:"Operations", content:<GroupOperations className="group-config"
                                                                   service={ServiceFactory.getGroupService()}
                                                                   stateService={ServiceFactory.getStateService()}
                                                                   statePollTimeout={tocVars.statePollTimeout}/>},
                     {title: "Configuration", content:<ConfigureTabs/>},
                     {title: "Admin", content:<AdminTab/>}]
        this.setState({items: items});
    }
});

var ConfigureTabs = React.createClass({
    getInitialState:function() {
        return {items: []};
    },
    render: function() {
        if (this.state.items.length > 0) {
            return <Tabs theme="default" items={this.state.items} depth="1"/>
        }
        return <div>Loading tab items...</div>
    },
    componentDidMount: function() {
        this.state.items.push({title:"JVM", content:<JvmConfig className="jvm-config" service={ServiceFactory.getJvmService()}/>});
        this.state.items.push({title:"Web Servers", content:<WebServerConfig className="webserver-config"
                                         service={ServiceFactory.getWebServerService()}/>});
        this.state.items.push({title: "Web Apps", content:<WebAppConfig className="webapp-config"
                                       service={ServiceFactory.getWebAppService()}
                                       groupService={ServiceFactory.getGroupService()}/>});

        if (tocVars["resourcesEnabled"] === "true") {
            this.state.items.push({title: "Resources", content:<ResourcesConfig resourceService={ServiceFactory.getResourceService()}
                                                                     groupService={ServiceFactory.getGroupService()}
                                                                     jvmService={ServiceFactory.getJvmService()}
                                                                     wsService={ServiceFactory.getWebServerService()}
                                                                     webAppService={ServiceFactory.getWebAppService()}/>});
        }

        this.state.items.push({title: "Group", content:<GroupConfig service={ServiceFactory.getGroupService()}/>});
        this.forceUpdate();
    }
});

$(document).ready(function(){

    if (typeof console == "undefined") {
        window.console = {
            log: function() {}
        };
    }

    $.validator.addMethod("notEqualTo", function(v, e, p) {
      return this.optional(e) || v != p;
    }, "Please specify a different value");

    $.validator.addMethod("pathCheck", function(value, element) {
        var exp = /\/.*/;
        return exp.test(value);
    }, "The field must be a valid, absolute path.");

    $.validator.addMethod("hostNameCheck", function(value, element) {
        var exp = /^[a-zA-Z0-9-.]+$/i;
        return this.optional(element) || exp.test(value);
    }, "The field must only contain letters, numbers, dashes and-or periods.");

    $.validator.addMethod("nameCheck", function(value, element) {
        var exp = /^[a-zA-Z0-9-_.\s]+$/i;
        return this.optional(element) || exp.test(value);
    }, "The field must only contain letters, numbers, dashes, underscores, periods and-or spaces.");

    $.validator.addMethod("xmlFileNameCheck", function(value, element) {
        var exp = /^.*\.xml$/i;
        return this.optional(element) || exp.test(value);
    }, "The field must only contain letters, numbers, underscores, dashes and-or periods.");

    React.renderComponent(<MainArea className="main-area"/>, document.body);
});