/** @jsx React.DOM */
var ResourcesConfig = React.createClass({
    render: function() {
        var splitterComponents = [];

        splitterComponents.push(<div><ResourceEditor resourceService={this.props.resourceService}
                                                     groupService={this.props.groupService}
                                                     generateXmlSnippetCallback={this.generateXmlSnippetCallback}
                                                     getTemplateCallback={this.getTemplateCallback}/></div>);
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
        var xmlTabItems = [{title: "Tokenized", content:<XmlPreview ref="tokenizedXmlPreview">{this.state.xml}</XmlPreview>},
                           {title: "Untokenized", content:<XmlPreview ref="untokenizedXmlPreview">{this.state.template}</XmlPreview>}];

        return <Tabs theme="default" items={xmlTabItems} depth="0" onSelectTab={this.onSelectTab}/>
    },
    refreshXmlDisplay: function(xmlSnippet) {
        if (this.refs.tokenizedXmlPreview !== undefined) {
            this.refs.tokenizedXmlPreview.refresh(xmlSnippet, xmlSnippet.length - 1);
        }
    },
    refreshTemplateDisplay: function(template) {
        if (this.refs.untokenizedXmlPreview !== undefined) {
            this.refs.untokenizedXmlPreview.refresh(template, template.length - 1);
        }
        this.setState({template: template});
    },
    onSelectTab: function(index) {
        if (index === 1) {
            this.refs.untokenizedXmlPreview.refresh(this.state.template, this.state.template.length - 1);
        }
    }
});
