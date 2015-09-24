/** @jsx React.DOM */
var XmlPreview = React.createClass({
    getInitialState: function() {
        return {content: this.props.children};
    },
    render: function() {
        var metaData = [{icon: "ui-icon-refresh", title: "Deploy", onClickCallback: this.deployCallback}];

        return <div className="xml-preview-container">
                   <RToolbar className="toolbar-container" btnClassName="ui-button-text-only ui-button-height"
                             busyBtnClassName="busy-button" metaData={metaData}/>
                   <div ref="theXmlContainer">
                       <pre className="theXml"/>
                   </div>
               </div>
    },
    componentDidUpdate: function() {
        if (this.state.content !== undefined) {
            // The code replace(/\n/g, "</li><li>") fixes bug wherein regex replace for \n doesn't work for snippet for
            // IE8.
            // TODO: Put the fix in snippet and submit as a bug fix for the said open source plugin.
            var escaped = this.state.content.replace(/</g, "&lt").replace(/>/g, "&gt").replace(/ /g, "&nbsp;").replace(/\n/g, "</li><li>");
            $(this.refs.theXmlContainer.getDOMNode()).html('<pre class="theXml"></pre>');
            $("pre.theXml").html(escaped);
            var style = "";
            if (this.props.isPlainText !== "true") {
                style = "ide-eclipse";
            }
            $("pre.theXml").snippet("xml",{style:style, showNum:true, menu: false});
        }
    },
    deployCallback: function(ajaxProcessDoneCallback) {
        this.props.deployCallback(ajaxProcessDoneCallback);
    },
    refresh: function(content) {
        this.setState({content:vkbeautify.xml(content, "    ")});
    }
})