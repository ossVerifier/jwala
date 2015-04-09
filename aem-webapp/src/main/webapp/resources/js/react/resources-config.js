/** @jsx React.DOM */
var ResourcesConfig = React.createClass({
    render: function() {
        var splitterComponents = [];

        splitterComponents.push(<div><ResourceEditor/></div>);
        splitterComponents.push(<div><XmlTabs/></div>);

        var splitter = <RSplitter components={splitterComponents} orientation={RSplitter.VERTICAL_ORIENTATION}/>

        return <div className="resource-container">{splitter}</div>
    }
})

var XmlTabs = React.createClass({
    getInitialState: function() {
        return {
            tokenizedXml: '<Resource name="jdbc/toc-xa" auth="Container" type="com.atomikos.jdbc.AtomikosDataSourceBean" factory="com.siemens.cto.infrastructure.atomikos.EnhancedTomcatAtomikosBeanFactory"/>',
            untokenizedXml: '<Resource name="${name}" auth="${contaner}" type="${type}" factory="${factory}"/>'
        };
    },
    render: function() {
        var xmlTabItems = [{title: "Tokenized", content:<RXmlEditor content={this.state.tokenizedXml}/>},
                           {title: "Untokenized", content:<RXmlEditor content={this.state.untokenizedXml}/>}];
        return <Tabs theme="default" items={xmlTabItems} depth="0"/>
    }
});
