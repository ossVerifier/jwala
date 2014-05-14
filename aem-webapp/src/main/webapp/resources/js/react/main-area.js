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
                                <div id="loading" style={{display:"none"}}>
                                    <br/>
                                    <img src="public-resources/img/gears-3d.gif"/>
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
        return <img src="public-resources/img/toc-banner-960px.jpg"/>
    }
});

var MainTabs = React.createClass({
    getInitialState:function() {
        items = [{title:"Groups", content:<GroupOperations className="group-config"
                                           service={ServiceFactory.getGroupService()}/>},
                 {title:"Web Servers", content:<WebServerOperations className="webserver-config"
                                                service={ServiceFactory.getWebServerService()}/>},
                 {title: "Configure", content:<ConfigureTabs/>}];
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
    /**
     * This shows a busy status when there is an on-going ajax process.
     */
    $.ajaxSetup({
        beforeSend: function () {
            _loading();
        },
        complete: function () {
        }
    });

    function _loading(){
        var loading = $("#loading");
        loading.dialog({
            modal:true,
            resizable:false,
            draggable:false,
            width:"auto",
            height:"auto"
        });
        loading.parents(".ui-dialog")
            .css("border", "0 none")
            .css("background", "transparent")
            .find(".ui-dialog-titlebar").remove();
        setTimeout(function(){loading.dialog("close")},500);
    }

    React.renderComponent(<MainArea className="main-area"/>, document.body);
});