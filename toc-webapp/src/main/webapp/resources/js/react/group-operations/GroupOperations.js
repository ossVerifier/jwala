
/** @jsx React.DOM */
var GroupOperations = React.createClass({
    pollError: false,
    getInitialState: function() {
        selectedGroup = null;

        // What does the code below do ?
        this.allJvmData = { jvms: [],
                            jvmStates: []};
        return {
            // Rationalize/unify all the groups/jvms/webapps/groupTableData/etc. stuff so it's coherent
            groupFormData: {},
            groupTableData: [],
            groups: [],
            groupStates: [],
            webServers: [],
            webServerStates: [],
            jvms: [],
            jvmStates: []
        };
    },
    render: function() {
        var btnDivClassName = this.props.className + "-btn-div";

        return  <div className={this.props.className}>
                    <div ref="stompMsgDiv"/>
                    <table style={{width:"1084px"}}>
                        <tr>
                            <td>
                                <div>
                                    <GroupOperationsDataTable ref="groupOperationsDataTable"
                                                              data={this.state.groupTableData}
                                                              selectItemCallback={this.selectItemCallback}
                                                              groups={this.state.groups}
                                                              groupsById={groupOperationsHelper.keyGroupsById(this.state.groups)}
                                                              webServers={this.state.webServers}
                                                              jvms={this.state.jvms}
                                                              updateWebServerDataCallback={this.updateWebServerDataCallback}
                                                              stateService={this.props.stateService}
                                                              commandStatusWidgetMap={this.commandStatusWidgetMap}
                                                              parent={this}/>
                                </div>
                            </td>
                        </tr>
                   </table>
               </div>
    },
    retrieveData: function() {
        var self = this;
        this.props.service.getGroups().then(this.retrieveGroupDataHandler).then(this.props.service.getChildrenInfo)
            .then(this.retrieveChildrenInfoHandler).then(function(){self.forceUpdate()});
    },
    /**
     * Group data retrieval handler.
     */
    retrieveGroupDataHandler: function(response) {
        var theGroups = response.applicationResponseContent;
        var jvms = [];
        var jvmArrayIdxMap = {}; // We need a map to see if the jvm is already in jvms then use it later to update jvm states.

        // Put all the jvms of all the groups in an array.
        // If the jvm is already in the said array, do not put it in anymore.
        theGroups.forEach(function(group){
            for (var i = 0; i < group.jvms.length; i++) {
                if (jvmArrayIdxMap[group.jvms[i].id.id] == undefined) {
                    jvms.push(group.jvms[i]);
                    jvmArrayIdxMap[group.jvms[i].id.id] = i;
                }
            }
        });

        this.state["jvms"] = jvms;
        this.state["jvmArrayIdxMap"] = jvmArrayIdxMap;
        this.state["groupTableData"] = theGroups;
        this.state["groups"] = theGroups;
    },
    /**
     * Children info retrieval handler.
     */
    retrieveChildrenInfoHandler: function(childrenInfo) {
        // Put the count data in the group's current state count properties.
        this.state.groups.forEach(function(group){
            childrenInfo.forEach(function(info){
                if (group.name === info.groupName) {
                    group.currentState.jvmCount = info.jvmCount;
                    group.currentState.jvmStartedCount = info.jvmStartedCount;
                    group.currentState.webServerCount = info.webServerCount;
                    group.currentState.webServerStartedCount = info.webServerStartedCount;
                }
            });
        });
    },
    getUpdatedJvmData: function(groups) {
        return groupOperationsHelper.processJvmData(this.state.jvms,
                                                    groupOperationsHelper.extractJvmDataFromGroups(groups),
                                                    this.state.jvmStates,
                                                    []);
    },
    msgHandler: function(msg) {
        if (msg.type === "JVM") {
            this.updateJvmStateData(msg);
        } else if (msg.type === "WEB_SERVER") {
            this.updateWebServerStateData(msg);
        } else if (msg.type === "GROUP") {
            this.updateGroupsStateData(msg);
        }
    },

// TODO: Remove when STOMP has been verified to work in a browser that does not support web sockets.
//    updateStateData: function(response) {
//        if (this.pollError) {
//            this.pollError = false;
//            return;
//        }
//
//        var newStates = response.applicationResponseContent;
//        var groups = [];
//        var webServers = [];
//        var jvms = [];
//
//        for (var i = 0; i < newStates.length; i++) {
//            if (newStates[i].type === "JVM") {
//                jvms.push(newStates[i]);
//            } else if (newStates[i].type === "WEB_SERVER") {
//                webServers.push(newStates[i]);
//            } else if (newStates[i].type === "GROUP") {
//                groups.push(newStates[i]);
//            }
//        }
//
//        this.updateGroupsStateData(groups);
//        this.updateWebServerStateData(webServers);
//        this.updateJvmStateData(jvms);
//    },

    /**
     * Check if the session is expired.
     */
    isSessionExpired: function(response) {
        if (typeof response.responseText === "string" && response.responseText.indexOf("Login") > -1) {
            return true;
        }
        return false;
    },
    /**
     * State polling error handler.
     */
    statePollingErrorHandler: function(response) {
        if (this.statePoller.isActive) {
            try {
                if (!this.isSessionExpired(response)) {
                    this.setGroupStatesToPollingError();
                    this.setJvmStatesToPollingError();
                    this.setWebServerStatesToPollingError();
                } else {
                    this.statePoller.stop();
                    this.statePoller = null;
                    alert("The session has expired! You will be redirected to the login page.");
                    window.location.href = "login";
                }
            } finally {
                this.pollError = true;
            }
        }
    },
    /**
     * Set group states to polling error.
     */
    setGroupStatesToPollingError: function() {
        for (var key in GroupOperations.groupStatusWidgetMap) {
        var groupStatusWidget = GroupOperations.groupStatusWidgetMap[key];
            if (groupStatusWidget !== undefined) {
                groupStatusWidget.setStatus(GroupOperations.POLL_ERR_STATE,  new Date(), response.responseJSON.applicationResponseContent);
            }
        }
    },
    /**
     * Set JVM states to polling error.
     */
    setJvmStatesToPollingError: function() {
        for (var key in GroupOperations.jvmStatusWidgetMap) {
            var jvmStatusWidget = GroupOperations.jvmStatusWidgetMap[key];
            if (jvmStatusWidget !== undefined) {
                jvmStatusWidget.setStatus(GroupOperations.POLL_ERR_STATE,  new Date(), "");
            }
        }
    },
    /**
     * Set web server states to polling error.
     */
    setWebServerStatesToPollingError: function() {
        for (var key in GroupOperations.webServerStatusWidgetMap) {
            var webServerStatusWidget = GroupOperations.webServerStatusWidgetMap[key];
            if (webServerStatusWidget !== undefined) {
                webServerStatusWidget.setStatus(GroupOperations.POLL_ERR_STATE,  new Date(), "");
            }
        }
    },
    updateGroupsStateData: function(newGroupState) {
        var groupsToUpdate = groupOperationsHelper.getGroupStatesById(this.state.groups);

        if (newGroupState) {
            for (var i = 0; i < groupsToUpdate.length; i++) {
                var group = groupsToUpdate[i];
                if (newGroupState.id.id === group.groupId.id) {
                    // For the group it's a bit different, we need to show the number of started servers
                    // over the total number of servers. Since we reused the existing current state
                    // infrastructure, we have to put the said info in the stateString property.
                    var serverCount = newGroupState.webServerCount + newGroupState.jvmCount;
                    var serverStartedCount = newGroupState.webServerStartedCount + newGroupState.jvmStartedCount;
                    newGroupState.stateString = "Started: " + serverStartedCount + "/" + serverCount;
                    GroupOperations.groupStatusWidgetMap["grp" + group.groupId.id].setStatus(newGroupState.stateString,
                        newGroupState.asOf, newGroupState.message);
                    break;
                }
            }
        };
    },
    commandStatusWidgetMap: {} /* Since we can't create a React class object reference on mount, we need to save the references in a map for later access. */,
    updateWebServerStateData: function(newWebServerState) {
        var webServersToUpdate = groupOperationsHelper.getWebServerStatesByGroupIdAndWebServerId(this.state.webServers);
        var self = this;

        if (newWebServerState !== null) {
            for (var i = 0; i < webServersToUpdate.length; i++) {
                var webServer = webServersToUpdate[i];
                var webServerStatusWidget = GroupOperations.webServerStatusWidgetMap["grp" + webServer.groupId.id + "webServer" + webServer.webServerId.id];
                if (webServerStatusWidget !== undefined) {
                    // for (var i = 0; i < newWebServerStates.length; i++) {
                        if (newWebServerState.id.id === webServer.webServerId.id) {
                            if (newWebServerState.stateString === GroupOperations.FAILED || newWebServerState.stateString === GroupOperations.START_SENT || newWebServerState.stateString === GroupOperations.STOP_SENT) {
                                if (newWebServerState.stateString === GroupOperations.STARTING) {
                                    newWebServerState.stateString = GroupOperations.START_SENT;
                                }
                                if (newWebServerState.stateString === GroupOperations.STOPPING) {
                                    newWebServerState.stateString = GroupOperations.STOP_SENT;
                                }
                                var commandStatusWidget = self.commandStatusWidgetMap[GroupOperations.getExtDivCompId(webServer.groupId.id)];
                                if (commandStatusWidget !== undefined) {
                                    commandStatusWidget.push({stateString: newWebServerState.stateString,
                                                              asOf: newWebServerState.asOf,
                                                              message: newWebServerState.message,
                                                              from: "Web Server " + webServer.name, userId: newWebServerStateuserId},
                                                              newWebServerState.stateString === GroupOperations.FAILED ? "error-status-font" : "action-status-font");
                                }


                            } else {
                                var stateDetails = groupOperationsHelper.extractStateDetails(newWebServerState);
                                webServerStatusWidget.setStatus(stateDetails.state, stateDetails.asOf, stateDetails.msg);
                            }
                            break;
                        }
                    // }
                }
            }
        }
    },
    updateJvmStateData: function(newJvmState) {
        var self = this;
        var jvmsToUpdate = groupOperationsHelper.getJvmStatesByGroupIdAndJvmId(this.state.jvms);

        if (newJvmState) {
            for (var i = 0; i < jvmsToUpdate.length; i++) {
                var jvm = jvmsToUpdate[i];
                var jvmStatusWidget = GroupOperations.jvmStatusWidgetMap["grp" + jvm.groupId.id + "jvm" + jvm.jvmId.id];
                if (jvmStatusWidget !== undefined) {
                    // for (var i = 0; i < newJvmStates.length; i++) {
                        if (newJvmState.id.id === jvm.jvmId.id) {
                            if (newJvmState.stateString === GroupOperations.FAILED ||
                                newJvmState.stateString === GroupOperations.START_SENT ||
                                newJvmState.stateString === GroupOperations.STOP_SENT) {

                                var commandStatusWidget = self.commandStatusWidgetMap[GroupOperations.getExtDivCompId(jvm.groupId.id)];
                                if (commandStatusWidget !== undefined) {
                                    commandStatusWidget.push({stateString: newJvmState.stateString,
                                                              asOf: newJvmState.asOf,
                                                              message: newJvmState.message,
                                                              from: "JVM " + jvm.name,
                                                              userId: newJvmState.userId},
                                                              newJvmState.stateString === GroupOperations.FAILED ?
                                                              "error-status-font" : "action-status-font");
                                }

                            } else {
                                var stateDetails = groupOperationsHelper.extractStateDetails(newJvmState);
                                jvmStatusWidget.setStatus(stateDetails.state, stateDetails.asOf, stateDetails.msg);

                                // Update the state of the jvm that is in a "react state" so that when the
                                // state component is re rendered it is updated. JVMs are loaded together with the
                                // group and not when the group is opened that is why we need this.
                                self.refs.groupOperationsDataTable.state.currentJvmState[jvm.jvmId.id] = {stateLabel: newJvmState.stateString,
                                                                                          errorStatus: ""};
                            }
                            break;
                        }
                    // }
                }
            }
        }
    },
    pollStates: function() {
// TODO: Remove when STOMP has been verified to work in a browser that does not support web sockets.
//        if (this.statePoller === null) {
//            this.statePoller = new PollerForAPromise(GroupOperations.STATE_POLLER_INTERVAL, stateService.getNextStates,
//                                                                this.updateStateData, this.statePollingErrorHandler);
//        }
//
//        this.statePoller.start();
        React.renderComponent(<span>Connecting to a web socket...</span>, this.refs.stompMsgDiv.getDOMNode());
        ServiceFactory.getServerStateWebSocketService().connect(this.msgHandler, this.stompConnectedCallback, this.stompConnectErrorHandler);
    },
    stompConnectedCallback: function(frame) {
        React.unmountComponentAtNode(this.refs.stompMsgDiv.getDOMNode());
    },
    stompConnectErrorHandler: function(e) {
        React.renderComponent(<span>Connecting to a web socket...</span>, this.refs.stompMsgDiv.getDOMNode());

        // try to connect again...
        ServiceFactory.getServerStateWebSocketService().connect(this.msgHandler, this.stompConnectedCallback, this.stompConnectErrorHandler);
    },
    markGroupExpanded: function(groupId, isExpanded) {
        this.setState(groupOperationsHelper.markGroupExpanded(this.state.groups,
                                                              groupId,
                                                              isExpanded));
    },
    markJvmExpanded: function(jvmId, isExpanded) {
        this.setState(groupOperationsHelper.markJvmExpanded(this.state.jvms,
                                                            jvmId,
                                                            isExpanded));
    },
    componentDidMount: function() {
        this.retrieveData();
        this.pollStates();
    },
    componentWillUnmount: function() {
// TODO: Remove when STOMP has been verified to work in a browser that does not support web sockets.
//        this.statePoller.stop();
        ServiceFactory.getServerStateWebSocketService().disconnect();
    },
    updateWebServerDataCallback: function(webServerData) {
        this.setState(groupOperationsHelper.processWebServerData([],
                                                                 webServerData,
                                                                 this.state.webServerStates,
                                                                 []));
        this.updateWebServerStateData(null);
    },
    statePoller: null,
    statics: {
        // Used in place of ref since ref will not work without a React wrapper (in the form a data table)
        groupStatusWidgetMap: {},
        webServerStatusWidgetMap: {},
        jvmStatusWidgetMap: {},
        FAILED: "FAILED",
        START_SENT: "START SENT",
        STOP_SENT: "STOP SENT",
        getExtDivCompId: function(groupId) {
            return "ext-comp-div-group-operations-table_" + groupId;
        },
        UNKNOWN_STATE: "",
        POLL_ERR_STATE: "POLLING ERROR!",
        STATE_POLLER_INTERVAL: 1
    }
});