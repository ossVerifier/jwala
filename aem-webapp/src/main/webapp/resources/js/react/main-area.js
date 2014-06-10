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
        return <div>
                    <div className="logout">
                        <a href="#" onClick={this.handleLogoutClick}>Logout</a>
                    </div>
                    <img src="public-resources/img/toc-banner-960px.jpg"/>
               </div>
    },
    handleLogoutClick: function() {
        ServiceFactory.getUserService().logout();
    }
});

var MainTabs = React.createClass({
    getInitialState:function() {
        items = [{title:"Groups", content:<GroupOperations className="group-config"
                                           service={ServiceFactory.getGroupService()}
                                           jvmStateService={ServiceFactory.getJvmStateService()}/>},
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
// Initialize a namespace
    var Toc = Toc || { Timer : {} }; var Timer = Toc.Timer || {}; Toc.Timer = Timer;

    /**
     * This shows a busy status when there is an on-going ajax process.
     */
//    $.ajaxSetup({
//        beforeSend: function () {
//            Toc.Timer._startLoading();
//        },
//        complete: function () {
//          Toc.Timer._clearLoading();
//        }
//    });

    Toc.Timer.timeout = undefined;
    Toc.Timer.loading = undefined;
    Toc.Timer._startLoading = function() {
      if(this.timeout === undefined) {
        this.timeout = setTimeout(this._loading,100);
      }
    }.bind(Toc.Timer);
    Toc.Timer._clearLoading = function() {
      if(this.timeout !== undefined) {
        var temp = this.timeout;
        this.timeout= undefined;
        clearTimeout(temp);
        this.timeout = undefined;
        if(this.loading !== undefined) {
          var temp = this.loading;
          this.loading = undefined;
          temp.dialog("close");
        }
      }
    }.bind(Toc.Timer);
    Toc.Timer._loading = function() {
        this.loading = $("#loading");
        this.loading.dialog({
            modal:true,
            resizable:false,
            draggable:false,
            width:"auto",
            height:"auto"
        });
        this.loading.parents(".ui-dialog")
            .css("border", "0 none")
            .css("background", "transparent")
            .find(".ui-dialog-titlebar").remove();
        this.timeout = setTimeout(this._clearLoading,500);
    }.bind(Toc.Timer);

    React.renderComponent(<MainArea className="main-area"/>, document.body);
});