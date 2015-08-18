/** @jsx React.DOM */
var ResourcesConfig = React.createClass({
    render: function() {
        var splitterComponents = [];

        splitterComponents.push(<div><ResourceEditor resourceService={this.props.resourceService}
                                                     groupService={this.props.groupService}
                                                     webAppService={this.props.webAppService}
                                                     generateXmlSnippetCallback={this.generateXmlSnippetCallback}
                                                     getTemplateCallback={this.getTemplateCallback}/></div>);
        splitterComponents.push(<XmlTabs ref="xmlTabs"/>);

        var splitter = <RSplitter disabled={true} components={splitterComponents} orientation={RSplitter.VERTICAL_ORIENTATION}/>

        return <div>
                    <div className="resource-dev-warning">
                        <img className="resource-dev-warning-img" src="public-resources/img/icons/ICCWARN.GIF"/>
                        WARNING: The following content is under development and is provided for demonstration purposes.
                        <img className="resource-dev-warning-img" src="public-resources/img/icons/ICCWARN.GIF"/>
                    </div>
                    <div className="resource-container">{splitter}</div>
                </div>
    },
    generateXmlSnippetCallback: function(resourceName, groupName) {
        this.props.resourceService.getXmlSnippet(resourceName, groupName, this.generateXmlSnippetResponseCallback);
    },
    generateXmlSnippetResponseCallback: function(response) {
        this.refs.xmlTabs.refreshXmlDisplay(response.applicationResponseContent);
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
                           {title: "Untokenized", content:<XmlPreview ref="untokenizedXmlPreview" isPlainText="true">{this.state.template}</XmlPreview>}];

        return <Tabs theme="xml-preview" items={xmlTabItems} depth="0" onSelectTab={this.onSelectTab}/>
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
