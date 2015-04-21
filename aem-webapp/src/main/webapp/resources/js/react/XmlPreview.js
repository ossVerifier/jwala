/** @jsx React.DOM */
var XmlPreview = React.createClass({
    getInitialState: function() {
        return {content: this.props.children};
    },
    render: function() {
        return <div ref="theXmlContainer" style={{width:"1070px", minHeight:"100%"}}><pre className="theXml"></pre></div>
    },
    componentDidUpdate: function() {
        var escaped = this.state.content.replace(/</g, "&lt").replace(/>/g, "&gt");
        $(this.refs.theXmlContainer.getDOMNode()).html('<pre class="theXml"></pre>');
        $("pre.theXml").html(escaped);
        $("pre.theXml").snippet("xml",{style:"acid"});
    },
    refresh: function(content) {
        this.setState({content:vkbeautify.xml(content)});
    }
})