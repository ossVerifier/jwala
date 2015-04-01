// Sample data
var attrTemplateList = [{name:"name", required:true}, {name:"type", required:false}, {name:"url", required:false}];

var ResourceAttrEditor = React.createClass({
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
                                                                                        onClick:this.onClickGenerateXml})
        );

        var attrValTable = React.createElement(AttrValTable, {attrTemplateList:attrTemplateList});

        return React.createElement("div", {className:"attr-values-container"}, toolbar, attrValTable);
    },
    onClickDel: function() {
    },
    onClickAdd: function() {
    },
    onClickGenerateXml: function() {
    }
});

var AttrValTable = React.createClass({
    render: function() {
        var attrElements = [];
        this.props.attrTemplateList.forEach(function(attrMeta){
            attrElements.push(React.createElement(AttrValRow, {key:attrMeta.name, attrName:attrMeta.name}));
        });

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