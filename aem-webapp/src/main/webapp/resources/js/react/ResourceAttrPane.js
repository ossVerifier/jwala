var ResourceAttrPane = React.createClass({
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


        if (this.props.resourceData !== null) {
            var attrValTable = React.createElement(AttrValTable, {attributes:this.props.resourceData.attributes,
                                                                  updateCallback:this.updateCallback});
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
    },
    updateCallback: function(attribute) {
        // We have to transform the attributes first to a format that the Rest API understands.
        // We might need to change the REST API to do away with this! This would do for now.
        var tempAttrArray = [];
        for (key in this.props.resourceData.attributes) {
            var attributesForRestConsumption = {};
            attributesForRestConsumption["key"] = key;
            if (attribute[key] !== undefined) {
                attributesForRestConsumption["value"] = attribute[key];
            } else {
                attributesForRestConsumption["value"] = this.props.resourceData.attributes[key];
            }
            tempAttrArray.push(attributesForRestConsumption);
        }

        var resourceData = {resourceTypeName:this.props.resourceData.resourceTypeName,
                            groupName:this.props.resourceData.group.name,
                            name:this.props.resourceData.name,
                            attributes:tempAttrArray};

        ServiceFactory.getResourceService().updateResourceAttributes(this.props.resourceData.name,
                                                                     this.props.resourceData.group.name,
                                                                     resourceData,
                                                                     this.updateAttrSuccessCallback,
                                                                     this.updateAttrErrorCallback);

    },
    updateAttrSuccessCallback: function() {
        this.props.updateCallback();
    },
    updateAttrErrorCallback: function(errMsg) {
        $.errorAlert(errMsg, "Error");
    }
 });

 var AttrValTable = React.createClass({
    render: function() {
        var attrElements = [];

        for (key in this.props.attributes) {
            attrElements.push(React.createElement(AttrValRow, {key:key, attrName:key,
                                                               attrValue:this.props.attributes[key],
                                                               updateCallback:this.props.updateCallback}));
        }

        return React.createElement("div", {className:"attr-val-table-container"},
                   React.createElement("table", {className:"attr-val-table"},
                       React.createElement("tbody", {}, attrElements)));
    }
});

var AttrValRow = React.createClass({
    getInitialState:function() {
        var states = {};
        states[this.props.attrName] = this.props.attrValue;
        return states;
    },
    render: function() {
        var checkBoxTd = React.createElement("td", {}, React.createElement("input", {type:"checkbox"}));
        var attrNameTd = React.createElement("td", {}, this.props.attrName);
        var attrValueInputTd = React.createElement("td", {},
            React.createElement("input", {ref:"attrTextBox",
                                          className:"val-text-field",
                                          onChange:this.onAttrValChange,
                                          onBlur:this.onAttrTextBoxBlur,
                                          value:this.state[this.props.attrName]}));
        return React.createElement("tr", {}, checkBoxTd, attrNameTd, attrValueInputTd);
    },
    onAttrValChange: function() {
        var theState = {};
        theState[this.props.attrName] = $(this.refs.attrTextBox.getDOMNode()).val();
        this.setState(theState);
    },
    onAttrTextBoxBlur: function() {
        this.props.updateCallback(this.state);
    },
    componentWillReceiveProps: function(nextProps) {
        var theState = {};
        theState[this.props.attrName] = nextProps.attrValue;
        this.setState(theState);
    }
});