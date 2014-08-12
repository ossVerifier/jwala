/** @jsx React.DOM */
var MainArea = React.createClass({
     render: function() {
        return <div className={this.props.className}>
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
               </div>
    }
});

var Banner = React.createClass({
    render: function() {
        return <div className="banner-container">
                    <img src="public-resources/img/toc-logo-siemens.jpg"/>
                    <span className="banner-logout"><a href="#" onClick={this.handleLogoutClick}>Logout</a></span>
                    <span className="banner-title">Tomcat Operations Center</span>
               </div>
    },
    handleLogoutClick: function() {
        ServiceFactory.getUserService().logout();
    }
});

var MainTabs = React.createClass({
    getInitialState:function() {
        items = [{title:"Operations", content:<GroupOperations className="group-config"
                                           service={ServiceFactory.getGroupService()}
                                           stateService={ServiceFactory.getStateService()}
                                           statePollTimeout={tocVars.statePollTimeout}/>},
                 {title: "Configuration", content:<ConfigureTabs/>},
                 {title: "Admin", content:<AdminTab/>}];
        return null;
    },
    render: function() {
        return <Tabs theme="default" items={items} depth="0"/>
    }
});

var ConfigureTabs = React.createClass({
    getInitialState:function() {
        items = [{title:"JVM", content:<JvmConfig className="jvm-config"
                                                  service={ServiceFactory.getJvmService()}/>},
                 {title:"Web Servers", content:<WebServerConfig className="webserver-config"
                                                                service={ServiceFactory.getWebServerService()}/>},
                 {title: "Web Apps", content:<WebAppConfig className="group-config"
                                                                service={ServiceFactory.getWebAppService()}
                                                                groupService={ServiceFactory.getGroupService()}/>},
                 {title: "Resources", content:""},
                 {title: "Group", content:<GroupConfig className="group-config"
                                                       service={ServiceFactory.getGroupService()}/>}];
        return null;
    },
    render: function() {
        return <Tabs theme="default" items={items} depth="1"/>
    }
});

$(document).ready(function(){
    React.renderComponent(<MainArea className="main-area"/>, document.body);
});