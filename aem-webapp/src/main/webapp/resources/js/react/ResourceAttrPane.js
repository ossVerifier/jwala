// Sample data
// var attrTemplateList = [{name:"name", required:true}, {name:"type", required:false}, {name:"url", required:false}];

var ResourceAttrEditor = React.createClass({

    getInitialState: function() {
        return {
            resource: null
        }
    },

    render: function() {
        var toolbar = React.createElement("div", {className:"resource-attr-toolbar"},
                                                          React.createElement(RButton, {title:"Delete attribute",
                                                                              className:"ui-state-default ui-corner-all default-icon-button-style",
                                                                              spanClassName:"ui-icon ui-icon-trash",
                                                                              onClick:this.onClickDel}),
                                                          React.createElement(RButton, {title:"Add attribute",
                                                                                        className:"ui-state-default ui-corner-all default-icon-button-style",
                                                                                        spanClassName:"ui-icon ui-icon-plus",
                                                                                        onClick:this.onClickAdd}),
                                                          React.createElement(RButton, {title:"Generate XML",
                                                                                        className:"ui-state-default ui-corner-all default-icon-button-style",
                                                                                        spanClassName:"ui-icon ui-icon-play",
                                                                                        onClick:this.onClickGenerateXml}));


        if (this.state.resource !== null) {
            var attrValTable = React.createElement(AttrValTable, {attributes:this.state.resource.attributes});
            return React.createElement("div", {className:"attr-values-container"}, toolbar, attrValTable);
        }
        return React.createElement("div", {className:"attr-values-container"}, toolbar, "Please select a resource to view it's attributes");
    },
    onClickDel: function() {
    },
    onClickAdd: function() {
    },
    onClickGenerateXml: function() {
    },
    refresh: function(resource) {
        this.setState({resource:resource});
    }
});

var AttrValTable = React.createClass({
    render: function() {
        var attrElements = [];

        for (key in this.props.attributes) {
            attrElements.push(React.createElement(AttrValRow, {key:key, attrName:key}));
        }

        return React.createElement("div", {className:"attr-val-table-container"},
                   React.createElement("table", {className:"attr-val-table"},
                       React.createElement("tbody", {}, attrElements)));
    }
});

var AttrValRow = React.createClass({
    render: function() {
        var checkBoxTd = React.createElement("td", {}, React.createElement("input", {type:"checkbox"}));
        var attrNameTd = React.createElement("td", {}, this.props.attrName);
        var attrValueInputTd = React.createElement("td", {}, React.createElement("input", {className:"val-text-field"}));
        return React.createElement("tr", {}, checkBoxTd, attrNameTd, attrValueInputTd);
    }
});