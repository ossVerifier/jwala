/** @jsx React.DOM */
var XmlPreview = React.createClass({
    getInitialState: function() {
        return {content: this.props.children};
    },
    render: function() {
        return <div ref="theXmlContainer" className="xml-preview-container"><pre className="theXml"></pre></div>
    },
    componentDidUpdate: function() {
        // The code replace(/\n/g, "</li><li>") fixes bug wherein regex replace for \n doesn't work for snippet for
        // IE8.
        // TODO: Put the fix in snippet and submit as a bug fix for the said open source plugin.
        var escaped = this.state.content.replace(/</g, "&lt").replace(/>/g, "&gt").replace(/ /g, "&nbsp;").replace(/\n/g, "</li><li>");
        $(this.refs.theXmlContainer.getDOMNode()).html('<pre class="theXml"></pre>');
        $("pre.theXml").html(escaped);
        var style = "";
        if (this.props.isPlainText !== "true") {
            style = "matlab";
        }
        $("pre.theXml").snippet("xml",{style:style, showNum:false});
    },
    refresh: function(content) {
        this.setState({content:vkbeautify.xml(content, "    ")});
    }
})