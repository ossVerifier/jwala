/** @jsx React.DOM */
var ResourcesConfig = React.createClass({
    render: function() {
        var splitterComponents = [];

        splitterComponents.push(<div><ResourceEditor generateXmlSnippetCallback={this.generateXmlSnippetCallback} getTemplateCallback={this.getTemplateCallback}/></div>);
        splitterComponents.push(<div><XmlTabs ref="xmlTabs"/></div>);

        var splitter = <RSplitter components={splitterComponents} orientation={RSplitter.VERTICAL_ORIENTATION}/>

        return <div className="resource-container">{splitter}</div>
    },
    generateXmlSnippetCallback: function(xml) {
        this.refs.xmlTabs.refreshXmlDisplay(xml);
    },
    getTemplateCallback: function(template) {
        this.refs.xmlTabs.refreshTemplateDisplay(template);
    }
})

var XmlTabs = React.createClass({
    getInitialState: function() {
        return {template: ""}
    },
    render: function() {
        var xmlTabItems = [{title: "Tokenized", content:<RXmlEditor ref="tokenizedXmlPreview" content=""/>},
                           {title: "Untokenized", content:<RXmlEditor ref="untokenizedXmlPreview" content={this.state.template}/>}];
        return <Tabs theme="default" items={xmlTabItems} depth="0" onSelectTab={this.onSelectTab}/>
    },
    refreshXmlDisplay: function(xmlSnippet) {
        if (this.refs.tokenizedXmlPreview !== undefined) {
            this.refs.tokenizedXmlPreview.refresh(xmlSnippet);
        }
    },
    refreshTemplateDisplay: function(template) {
        if (this.refs.untokenizedXmlPreview !== undefined) {
            this.refs.untokenizedXmlPreview.refresh(template);
        }
        this.setState({template: template});
    },
    onSelectTab: function(index) {
        if (index === 1) {
            this.refs.untokenizedXmlPreview.refresh(this.state.template);
        }
    }
});
