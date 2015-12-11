/**
 * Display command action and error status as a result of the said command action.
 */
var CommandStatusWidget = React.createClass({
    getInitialState: function() {
        return {statusRows: [], isOpen: true};
    },
    render: function() {
        var self = this;

        var openCloseBtnClassName = "ui-icon-triangle-1-e";
        var content = null;
        if (this.state.isOpen) {
            openCloseBtnClassName = "ui-icon-triangle-1-s";
            content = <div ref="content" className="ui-dialog-content ui-widget-content command-status-background command-status-content">
                          <table>
                              {this.state.statusRows}
                          </table>
                      </div>;
        }

        return  <div ref="commandStatusContainer" className="ui-dialog ui-widget ui-widget-content ui-front command-status-container">
                    <div className="ui-dialog-titlebar ui-widget-header ui-helper-clearfix command-status-header">
                        <span className={"ui-accordion-header-icon ui-icon " + openCloseBtnClassName} style={{display:"inline-block"}} onClick={this.clickOpenCloseWindowHandler}></span>
                        <span className="ui-dialog-title" style={{display:"inline-block", float:"none", width:"auto"}}>Action and Event Logs</span>
                    </div>
                    {content}
                </div>;

    },
    componentDidMount: function() {
        var self = this;
        historyService.read(this.props.groupName).then(
            function(data) {
                 for (var i = 0; i < data.length; i++) {
                    var status = {};
                    status["from"] = data[i].serverName;
                    status["userId"] = data[i].createBy;
                    status["asOf"] = data[i].createDate;
                    status["message"] = data[i].event;
                    self.push(status, data[i].eventType === "USER_ACTION" ? "action-status-font" : "error-status-font",
                        (i === data.length - 1));
                }
            }).caught(function(response) {console.log(response)});
    },
    componentDidUpdate: function(prevProps, prevState) {
        if (this.refs.content) {
            this.refs.content.getDOMNode().scrollTop = this.refs.content.getDOMNode().scrollHeight;
        }
    },
    clickOpenCloseWindowHandler: function() {
        this.setState({isOpen: !this.state.isOpen});
    },
    showDetails: function(msg) {
        var myWindow = window.open("", "Error Details", "width=500, height=500");
        myWindow.document.write(msg);
    },
    onXBtnClick: function() {
        this.props.closeCallback();
    },
    onXBtnMouseOver: function() {
        this.setState({xBtnHover: true});
    },
    onXBtnMouseOut: function() {
        this.setState({xBtnHover: false});
    },
    push: function(status, fontClassName, forceUpdate) {
        var errMsg = status.message === "" ? [status.stateString] : groupOperationsHelper.splitErrorMsgIntoShortMsgAndStackTrace(status.message);

        // Do simple cleanup when status array reaches 200 items.
        if (this.state.statusRows.length >= 200) {
            this.state.statusRows.splice(0, 50); // remove first 50 items
        }

        if (errMsg[1] && errMsg[1].trim() !== "") {
            this.state.statusRows.push(<tr className={fontClassName}>
                                           <td>{moment(status.asOf).format("MM/DD/YYYY hh:mm:ss")}</td>
                                           <td className="command-status-td">{status.from}</td>
                                           <td>{status.userId}</td>
                                           <td className="command-status-td" style={{textDecoration: "underline", cursor: "pointer"}} onClick={this.showDetails.bind(this, errMsg[1])}>{errMsg[0]}</td>
                                       </tr>);
        } else {
            this.state.statusRows.push(<tr className={fontClassName}>
                                           <td>{moment(status.asOf).format("MM/DD/YYYY hh:mm:ss")}</td>
                                           <td className="command-status-td">{status.from}</td>
                                           <td>{status.userId}</td>
                                           <td>{errMsg[0]}</td>
                                       </tr>);
        }

        if (forceUpdate === undefined || forceUpdate === true) {
            this.forceUpdate();
        }
    }
});