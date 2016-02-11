/**
 * A panel widget for jvm buttons.
 */
var JvmControlPanelWidget = React.createClass({
    doneCallback: {},
    render: function() {
        return <div className="jvm-control-panel-widget">
                    <RButton ref="stopBtn"
                             className="zero-padding ui-widget ui-state-default ui-corner-all ui-button-text-only ui-button-height"
                             spanClassName="ui-icon ui-icon-stop"
                             onClick={this.stop}
                             title="Stop"/>

                    <RButton ref="startBtn"
                             className="zero-padding ui-widget ui-state-default ui-corner-all ui-button-text-only ui-button-height"
                             spanClassName="ui-icon ui-icon-play"
                             onClick={this.start}
                             title="Start"/>

                    <RButton ref="generateConfigBtn"
                             className="zero-padding ui-widget ui-state-default ui-corner-all ui-button-text-only ui-button-height"
                             spanClassName="ui-icon ui-icon-gear-custom"
                             onClick={this.generateConfig}
                             title="Generate JVM resources files"/>

                   <RButton ref="heapDumpBtn"
                            className="zero-padding ui-widget ui-state-default ui-corner-all ui-button-text-only ui-button-height"
                            spanClassName="ui-icon ui-icon-heap-dump"
                            onClick={this.doHeapDump}
                            title="Heap Dump"/>

                   <RButton ref="threadDumpBtn"
                            className="zero-padding ui-widget ui-state-default ui-corner-all ui-button-text-only ui-button-height"
                            spanClassName="ui-icon ui-icon-thread-dump"
                            onClick={this.doThreadDump}
                            title="Thread Dump"/>

                   <RButton ref="diagnoseBtn"
                            className="zero-padding ui-widget ui-state-default ui-corner-all ui-button-text-only ui-button-height"
                            spanClassName="ui-icon ui-icon-wrench"
                            onClick={this.diagnose}
                            title="Diagnose and resolve state"/>

                   <RButton ref="managerBtn"
                            className="zero-padding ui-widget ui-state-default ui-corner-all ui-button-text-only ui-button-height"
                            spanClassName="ui-icon ui-icon-mgr"
                            onClick={this.showMgr}
                            title="Manager"/>
               </div>
    },
    stop: function() {
        this.showFadingStatusClickedLabel("Stopping...", this.refs.stopBtn.getDOMNode(), this.props.data.id.id);
        this.props.jvmStopCallback(this.props.data, this.refs.stopBtn.getDOMNode(), function() { /* cancel callback */ });
    },
    start: function() {
        this.showFadingStatusClickedLabel("Starting...", this.refs.startBtn.getDOMNode(), this.props.data.id.id);
        this.props.jvmStartCallback(this.props.data, this.refs.startBtn.getDOMNode(), function() { /* cancel callback */ });
    },
    generateConfig: function(doneCallback) {
        this.doneCallback[this.props.data.name] = doneCallback;
        this.props.jvmGenerateConfigCallback(this.props.data, this.refs.generateConfigBtn.getDOMNode());
    },
    doHeapDump: function() {
        this.props.jvmHeapDumpCallback(this.props.data.id, this.refs.heapDumpBtn.getDOMNode(), this.props.data.hostName);
    },
    doThreadDump: function() {
        var url = "jvmCommand?jvmId=" + this.props.data.id.id + "&operation=threadDump";
        window.open(url)
    },
    diagnose: function() {
        this.props.jvmDiagnoseCallback(this.props.data, this.refs.diagnoseBtn, function(){});
    },
    showMgr: function() {
        var url = "idp?saml_redirectUrl=" +
                   window.location.protocol + "//" +
                   this.props.data.hostName + ":" +
                   (window.location.protocol.toUpperCase() === "HTTPS:" ? this.props.data.httpsPort : this.props.data.httpPort) + "/manager/";
        window.open(url);
    },

    /**
     * Uses jquery to take advantage of the fade out effect and to reuse the old code...for now.
     */
    showFadingStatusClickedLabel: function(msg, btnDom, webServerId) {
        var tooTipId = "tooltip" +  WebServerControlPanelWidget.getReactId(btnDom).replace(/\./g, "-") + webServerId;
        if (msg !== undefined && $("#" + tooTipId).length === 0) {
            var top = $(btnDom).position().top - $(btnDom).height()/2;
            var left = $(btnDom).position().left + $(btnDom).width()/2;
            $(btnDom).parent().append("<div id='" + tooTipId +
                "' role='tooltip' class='ui-tooltip ui-widget ui-corner-all ui-widget-content' " +
                "style='top:" + top + "px;left:" + left + "px'>" + msg + "</div>");

            $("#" + tooTipId).fadeOut(3000, function() {
                $("#" + tooTipId).remove();
            });

        }
    },

    statics: {
        getReactId: function(dom) {
            return $(dom).attr("data-reactid");
        }
    }

});