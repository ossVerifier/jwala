/** @jsx React.DOM */
var ResourcesConfig = React.createClass({
    render: function() {
        var splitterComponents = [];

        splitterComponents.push(<div><ResourceEditor generateXmlSnippetCallback={this.generateXmlSnippetCallback}/></div>);
        splitterComponents.push(<div><XmlTabs ref="xmlTabs"/></div>);

        var splitter = <RSplitter components={splitterComponents} orientation={RSplitter.VERTICAL_ORIENTATION}/>

        return <div className="resource-container">{splitter}</div>
    },
    generateXmlSnippetCallback: function(xml) {
        this.refs.xmlTabs.refresh(xml);
    }
})

var XmlTabs = React.createClass({
    getInitialState: function() {
        return {
            tokenizedXml: "",
            untokenizedXml: ""
        };
    },
    render: function() {
        var xmlTabItems = [{title: "Tokenized", content:<RXmlEditor ref="tokenizedXmlPreview" content={this.state.tokenizedXml}/>},
                           {title: "Untokenized", content:<RXmlEditor content={this.state.untokenizedXml}/>}];
        return <Tabs theme="default" items={xmlTabItems} depth="0"/>
    },
    refresh: function(xmlSnippet) {
        this.refs.tokenizedXmlPreview.refresh(xmlSnippet);
    }
});
