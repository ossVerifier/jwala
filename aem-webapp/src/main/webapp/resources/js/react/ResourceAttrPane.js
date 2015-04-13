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
    updateCallback: function(attrKey, attribute) {
        // We have to transform the attributes first to a format that the Rest API understands.
        // We might need to change the REST API to do away with this! This would do for now.
        var tempAttrArray = [];
        for (key in this.props.resourceData.attributes) {
            var attributesForRestConsumption = {};
            if (key === attrKey) {
                attributesForRestConsumption["key"] = attribute["attrName"];
                attributesForRestConsumption["value"] = attribute["attrValue"];
            } else {
                attributesForRestConsumption["key"] = key;
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
        return {
            attrName:this.props.attrName,
            attrValue:this.props.attrValue
        };
    },
    mixins: [React.addons.LinkedStateMixin],
    render: function() {
        var checkBoxTd = React.createElement("td", {}, React.createElement("input", {type:"checkbox"}));
        var attrNameTd = React.createElement("td", {}, React.createElement("input", {className:"name-text-field",
                                                                                     valueLink:this.linkState("attrName"),
                                                                                     onBlur:this.onAttrNameTextBoxBlur}));
        var attrValueInputTd = React.createElement("td", {},
            React.createElement("input", {className:"val-text-field",
                                          valueLink:this.linkState("attrValue"),
                                          onBlur:this.onAttrTextBoxBlur}));
        return React.createElement("tr", {}, checkBoxTd, attrNameTd, attrValueInputTd);
    },
    onAttrNameTextBoxBlur: function() {
        this.props.updateCallback(this.props.attrName, this.state);
    },
    onAttrTextBoxBlur: function() {
        this.props.updateCallback(this.props.attrName, this.state);
    },
    componentWillReceiveProps: function(nextProps) {
        this.setState({attrName:nextProps.attrName, attrValue:nextProps.attrValue});
    }
});