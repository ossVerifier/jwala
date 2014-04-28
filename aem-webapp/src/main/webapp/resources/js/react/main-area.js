/** @jsx React.DOM */
var MainArea = React.createClass({
     render: function() {
        return <div className={this.props.className}>
                    <table>
                        <tr>
                            <td><Banner/><br/><br/></td>
                        </tr>
                        <tr>
                            <td>
                                <MainTabs/>
                            </td>
                        </tr>
                    </table>
               </div>
    }
});

var Banner = React.createClass({
    render: function() {
        return <img src="public-resources/img/toc-banner-960px.jpg"/>
    }
});

var MainTabs = React.createClass({
    getInitialState:function() {
        items = [{title:"Groups", content:""},
                 {title:"Web Servers", content:""},
                 {title: "Configure", content:<ConfigureTabs/>}];
        return null;
    },
    render: function() {
        return <Tabs theme="default" items={items} depth="0"/>
    }
});

// TODO: Refactor jvm config area once it is "Reactified" already
var ConfigureTabs = React.createClass({
    getInitialState:function() {
        items = [{title:"JVM", content:<JvmConfig className="jvm-config"
                                                  service={ServiceFactory.getJvmService()}/>},
                 {title:"Web Servers", content:<WebServerConfig className="webserver-config"
                                                                service={ServiceFactory.getWebServerService()}/>},
                 {title: "Web Apps", content:<WebAppConfig className="group-config"
                                                                service={ServiceFactory.getWebAppService()}/>},
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