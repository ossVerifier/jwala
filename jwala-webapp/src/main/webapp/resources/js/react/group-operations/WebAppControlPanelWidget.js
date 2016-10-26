/**
 * A panel widget for web app buttons
 */
var WebAppControlPanelWidget = React.createClass({
    doneCallback: {},
    render: function() {
        return <div className="WebAppControlPanelWidget">
                   <RButton className="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-only ui-button-height"
                            spanClassName="ui-icon ui-icon-gear-custom"
                            onClick={this.generateConf}
                            title="Generate the httpd.conf and deploy as a service"
                            disabled = {!MainArea.isAdminRole}
                            disabledTitle="Resource generation is disabled for this version"
                            busyClassName="busy-button"/>
               </div>
    },
    generateConf: function(doneCallback) {
        var self = this;
        this.doneCallback[this.props.data.name] = doneCallback;
        this.props.webAppService.deployConf(this.props.data.name)
            .then(this.generateConfSuccessCallback)
            .caught(function(response){
                self.generateConfErrorCallback(JSON.parse(response.responseText).applicationResponseContent, doneCallback);
            });
    },
    generateConfSuccessCallback: function(response) {
        this.doneCallback[response.applicationResponseContent.name]();
        $.alert(this.props.data.name + " resource files deployed successfully", false);
    },
    generateConfErrorCallback: function(applicationResponseContent, doneCallback) {
        this.doneCallback[this.props.data.name]();
        $.errorAlert(applicationResponseContent, "Deploy " + this.props.data.name +  "", false);
    },
    statics: {
        getReactId: function(dom) {
            return $(dom).attr("data-reactid");
        }
    }
});
