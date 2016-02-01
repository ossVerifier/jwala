
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
        this.props.service.getGroups(function(response){
                                        var theGroups = response.applicationResponseContent;
                                        var state = {};
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

                                        state["jvms"] = jvms;
                                        state["jvmArrayIdxMap"] = jvmArrayIdxMap;
                                        state["groupTableData"] = theGroups;
                                        state["groups"] = theGroups;
                                        self.setState(state);
                                     });
    },
    getUpdatedJvmData: function(groups) {
        return groupOperationsHelper.processJvmData(this.state.jvms,
                                                    groupOperationsHelper.extractJvmDataFromGroups(groups),
                                                    this.state.jvmStates,
                                                    []);
    },
    updateStateData: function(response) {
        if (this.pollError) {
            // updateStateData is called when there's no longer an error therefore we do error recovery here.
            this.fetchCurrentGroupStates();
            this.fetchCurrentWebServerStates();
            this.fetchCurrentJvmStates();
            this.pollError = false;
            return;
        }

        var newStates = response.applicationResponseContent;
        var groups = [];
        var webServers = [];
        var jvms = [];

        for (var i = 0; i < newStates.length; i++) {
            if (newStates[i].type === "JVM") {
                jvms.push(newStates[i]);
            } else if (newStates[i].type === "WEB_SERVER") {
                webServers.push(newStates[i]);
            } else if (newStates[i].type === "GROUP") {
                groups.push(newStates[i]);
            }
        }

        this.updateGroupsStateData(groups);
        this.updateWebServerStateData(webServers);
        this.updateJvmStateData(jvms);
    },
    statePollingErrorHandler: function(response) {
        if (!this.statePoller.isActive()) {
            return;
        }

        if (typeof response.responseText === "string" && response.responseText.indexOf("Login") > -1) {
            this.statePoller.stop();
            this.statePoller = null;
            alert("The session has expired! You will be redirected to the login page.");
            window.location.href = "login";
            return;
        }

        this.pollError = true;
        for (var key in GroupOperations.groupStatusWidgetMap) {
            var groupStatusWidget = GroupOperations.groupStatusWidgetMap[key];
            if (groupStatusWidget !== undefined) {
                // Can't afford to slip or else the polling stops
                try {
                    groupStatusWidget.setStatus(GroupOperations.POLL_ERR_STATE,  new Date(), response.responseJSON.applicationResponseContent);
                } catch (e) {
                    console.log(e);
                }
            }
            // update the Action and Events log
            // TODO this should get pulled from the history table
            var commandStatusWidget = this.commandStatusWidgetMap[GroupOperations.getExtDivCompId(key.replace("grp",""))];
            if (commandStatusWidget !== undefined) {
                var responseMessage = response.responseJSON.applicationResponseContent;
                commandStatusWidget.push({stateString: GroupOperations.FAILED,
                                          asOf: new Date(),
                                          message: responseMessage,
                                          from: "Web Server and JVM State Notification Service",
                                          userId: ""},
                                          "error-status-font");
            }
        }

        for (var key in GroupOperations.jvmStatusWidgetMap) {
            var jvmStatusWidget = GroupOperations.jvmStatusWidgetMap[key];
            if (jvmStatusWidget !== undefined) {
                // Can't afford to slip or else the polling stops
                try {
                    jvmStatusWidget.setStatus(GroupOperations.UNKNOWN_STATE,  new Date(), "");
                } catch (e) {
                    console.log(e);
                }
            }
        }

        for (var key in GroupOperations.webServerStatusWidgetMap) {
            var webServerStatusWidget = GroupOperations.webServerStatusWidgetMap[key];
            if (webServerStatusWidget !== undefined) {
                // Can't afford to slip or else the polling stops
                try {
                    webServerStatusWidget.setStatus(GroupOperations.UNKNOWN_STATE,  new Date(), "");
                } catch (e) {
                    console.log(e);
                }
            }
        }
    },
    updateGroupsStateData: function(newGroupStates) {
        var groupsToUpdate = groupOperationsHelper.getGroupStatesById(this.state.groups);

        if (newGroupStates && newGroupStates.length > 0) {
            groupsToUpdate.forEach(
                function(group) {
                    for (var i = 0; i < newGroupStates.length; i++) {
                        if (newGroupStates[i].id.id === group.groupId.id) {
                            GroupOperations.groupStatusWidgetMap["grp" + group.groupId.id].setStatus(newGroupStates[i].stateString,
                                                                                           newGroupStates[i].asOf,
                                                                                           newGroupStates[i].message);
                        }
                    }
                }
            )
        };
    },
    commandStatusWidgetMap: {} /* Since we can't create a React class object reference on mount, we need to save the references in a map for later access. */,
    updateWebServerStateData: function(newWebServerStates) {
        var webServersToUpdate = groupOperationsHelper.getWebServerStatesByGroupIdAndWebServerId(this.state.webServers);
        var self = this;

        if (newWebServerStates && newWebServerStates.length > 0) {
            webServersToUpdate.forEach(
                function(webServer) {
                    var webServerStatusWidget = GroupOperations.webServerStatusWidgetMap["grp" + webServer.groupId.id + "webServer" + webServer.webServerId.id];
                    if (webServerStatusWidget !== undefined) {
                        for (var i = 0; i < newWebServerStates.length; i++) {
                            if (newWebServerStates[i].id.id === webServer.webServerId.id) {
                                if (newWebServerStates[i].stateString === GroupOperations.FAILED || newWebServerStates[i].stateString === GroupOperations.START_SENT || newWebServerStates[i].stateString === GroupOperations.STOP_SENT) {
                                    if (newWebServerStates[i].stateString === GroupOperations.STARTING) {
                                        newWebServerStates[i].stateString = GroupOperations.START_SENT;
                                    }
                                    if (newWebServerStates[i].stateString === GroupOperations.STOPPING) {
                                        newWebServerStates[i].stateString = GroupOperations.STOP_SENT;
                                    }
                                    var commandStatusWidget = self.commandStatusWidgetMap[GroupOperations.getExtDivCompId(webServer.groupId.id)];
                                    if (commandStatusWidget !== undefined) {
                                        commandStatusWidget.push({stateString: newWebServerStates[i].stateString,
                                                                  asOf: newWebServerStates[i].asOf,
                                                                  message: newWebServerStates[i].message,
                                                                  from: "Web Server " + webServer.name, userId: newWebServerStates[i].userId},
                                                                  newWebServerStates[i].stateString === GroupOperations.FAILED ? "error-status-font" : "action-status-font");
                                    }


                                } else {
                                    var stateDetails = groupOperationsHelper.extractStateDetails(newWebServerStates[i]);
                                    webServerStatusWidget.setStatus(stateDetails.state, stateDetails.asOf, stateDetails.msg);
                                }
                            }
                        }
                    }
                }
            );
        }
    },
    updateJvmStateData: function(newJvmStates) {
        var self = this;
        var jvmsToUpdate = groupOperationsHelper.getJvmStatesByGroupIdAndJvmId(this.state.jvms);

        if (newJvmStates && newJvmStates.length > 0) {
            jvmsToUpdate.forEach(
                function(jvm) {
                    var jvmStatusWidget = GroupOperations.jvmStatusWidgetMap["grp" + jvm.groupId.id + "jvm" + jvm.jvmId.id];
                    if (jvmStatusWidget !== undefined) {
                        for (var i = 0; i < newJvmStates.length; i++) {
                            if (newJvmStates[i].id.id === jvm.jvmId.id) {
                                if (newJvmStates[i].stateString === GroupOperations.FAILED ||
                                    newJvmStates[i].stateString === GroupOperations.START_SENT ||
                                    newJvmStates[i].stateString === GroupOperations.STOP_SENT) {

                                    var commandStatusWidget = self.commandStatusWidgetMap[GroupOperations.getExtDivCompId(jvm.groupId.id)];
                                    if (commandStatusWidget !== undefined) {
                                        commandStatusWidget.push({stateString: newJvmStates[i].stateString,
                                                                  asOf: newJvmStates[i].asOf,
                                                                  message: newJvmStates[i].message,
                                                                  from: "JVM " + jvm.name,
                                                                  userId: newJvmStates[i].userId},
                                                                  newJvmStates[i].stateString === GroupOperations.FAILED ?
                                                                  "error-status-font" : "action-status-font");
                                    }

                                } else {
                                    var stateDetails = groupOperationsHelper.extractStateDetails(newJvmStates[i]);
                                    jvmStatusWidget.setStatus(stateDetails.state, stateDetails.asOf, stateDetails.msg);

                                    // Update the state of the jvm that is in a "react state" so that when the
                                    // state component is re rendered it is updated. JVMs are loaded together with the
                                    // group and not when the group is opened that is why we need this.
                                    self.refs.groupOperationsDataTable.state.currentJvmState[jvm.jvmId.id] = {stateLabel: newJvmStates[i].stateString,
                                                                                              errorStatus: ""};
                                }
                            }
                        }
                    }
                }
            );
        }
    },
    pollStates: function() {
        if (this.statePoller === null) {
            this.statePoller = new PollerForAPromise(GroupOperations.STATE_POLLER_INTERVAL, stateService.getNextStates,
                                                                this.updateStateData, this.statePollingErrorHandler);
        }

        this.statePoller.start();
    },
    fetchCurrentGroupStates: function() {
        var self = this;
        this.props.stateService.getCurrentGroupStates()
            .then(function(data) {self.updateGroupsStateData(data.applicationResponseContent);})
            .caught(function(e) {console.log(e);});
    },
    fetchCurrentWebServerStates: function() {
        var self = this;
        this.props.stateService.getCurrentWebServerStates()
            .then(function(data) {self.updateWebServerStateData(data.applicationResponseContent);})
            .caught(function(e) {console.log(e);});
    },
    fetchCurrentJvmStates: function() {
        var self = this;
        this.props.stateService.getCurrentJvmStates()
            .then(function(data) {self.updateJvmStateData(data.applicationResponseContent);})
            .caught(function(e) {console.log(e);});
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
        this.fetchCurrentGroupStates();
    },
    componentWillUnmount: function() {
        this.statePoller.stop();
    },
    updateWebServerDataCallback: function(webServerData) {
        this.setState(groupOperationsHelper.processWebServerData([],
                                                                 webServerData,
                                                                 this.state.webServerStates,
                                                                 []));
        this.updateWebServerStateData([]);
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