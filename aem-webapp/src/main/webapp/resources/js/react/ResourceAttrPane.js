/** @jsx React.DOM */
var ResourceAttrPane = React.createClass({
    getInitialState: function() {
        return {showDeleteConfirmDialog:false};
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

        var confirmationDlg = React.createElement(ModalDialogBox, {title:"Confirmation Dialog Box",
                                                                   show:this.state.showDeleteConfirmDialog,
                                                                   okCallback:this.confirmDeleteCallback,
                                                                   cancelCallback:this.cancelDeleteCallback,
                                                                   content:<div className="text-align-center"><br/><b>Are you sure you want to delete the selected item ?</b><br/><br/></div>,
                                                                   okLabel:"Yes",
                                                                   cancelLabel:"No"});

        if (this.props.resourceData !== null) {
            var attrValTable = React.createElement(AttrValTable, {ref:"attrTable",
                                                                  attributes:this.props.resourceData.attributes,
                                                                  updateCallback:this.updateCallback});
            return React.createElement("div", {className:"attr-values-container"}, toolbar, attrValTable, confirmationDlg);
        }

        return React.createElement("div", {className:"attr-values-container"},
                                   "Please select a resource to view it's attributes");
    },
    confirmDeleteCallback: function() {
        var selectedAttributes = this.refs.attrTable.getSelectedAttributes();
        var tempAttrArray = [];
        for (key in this.props.resourceData.attributes) {
            if (selectedAttributes.indexOf(key) === -1) {
                var attributesForRestConsumption = {};
                attributesForRestConsumption["key"] = key;
                attributesForRestConsumption["value"] = this.props.resourceData.attributes[key];
                tempAttrArray.push(attributesForRestConsumption);
           }
        }

        this.setState({showDeleteConfirmDialog:false});

        this.updateAttributes(tempAttrArray);
    },
    cancelDeleteCallback: function() {
        this.setState({showDeleteConfirmDialog:false});
    },
    onClickDel: function() {
        if (this.refs["attrTable"] !== undefined) {
            var selectedAttributes = this.refs.attrTable.getSelectedAttributes();
            if (selectedAttributes.length > 0) {
                this.setState({showDeleteConfirmDialog:true});
            }
        }
    },
    onClickAdd: function() {
        // the required attribute data transformation process to pass to the rest service
        var tempAttrArray = [];
        var largestNumber = 0;

        for (key in this.props.resourceData.attributes) {
            var attributesForRestConsumption = {};
            attributesForRestConsumption["key"] = key;
            attributesForRestConsumption["value"] = this.props.resourceData.attributes[key];
            tempAttrArray.push(attributesForRestConsumption);

            var num = parseInt(key.substring("key".length + 1), 10);
            if (!isNaN(num)) {
                largestNumber = (largestNumber < num) ? num : largestNumber;
            };
        }

        // the new attribute
        tempAttrArray.push({key:"key-" + ++largestNumber, value:null});
        this.updateAttributes(tempAttrArray);
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

        this.updateAttributes(tempAttrArray);
    },
    updateAttrSuccessCallback: function() {
        this.props.updateCallback();
    },
    updateAttrErrorCallback: function(errMsg) {
        $.errorAlert(errMsg, "Error");
    },
    updateAttributes: function(attrArray) {
        var resourceData = {resourceTypeName:this.props.resourceData.resourceTypeName,
                                    groupName:this.props.resourceData.group.name,
                                    name:this.props.resourceData.name,
                                    attributes:attrArray};

        ServiceFactory.getResourceService().updateResourceAttributes(this.props.resourceData.name,
                                                                     this.props.resourceData.group.name,
                                                                     resourceData,
                                                                     this.updateAttrSuccessCallback,
                                                                     this.updateAttrErrorCallback);
    }
 });

 var AttrValTable = React.createClass({
    render: function() {
        var attrElements = [];

        for (key in this.props.attributes) {
            attrElements.push(React.createElement(AttrValRow, {key:key,
                                                               ref:key,
                                                               attrName:key,
                                                               attrValue:this.props.attributes[key],
                                                               updateCallback:this.props.updateCallback}));
        }

        return React.createElement("div", {className:"attr-val-table-container"},
                   React.createElement("table", {className:"attr-val-table"},
                       React.createElement("tbody", {}, attrElements)));
    },
    getSelectedAttributes: function() {
        var selectedAttributes = [];
        for (key in this.props.attributes) {
            if (this.refs[key].isSelected()) {
                selectedAttributes.push(key);
            }
        }
        return selectedAttributes;
    }
});

var AttrValRow = React.createClass({
    getInitialState:function() {
        return {
            selected:false,
            attrName:this.props.attrName,
            attrValue:this.props.attrValue
        };
    },
    mixins: [React.addons.LinkedStateMixin],
    render: function() {
        var checkBoxTd = React.createElement("td", {}, React.createElement("input", {type:"checkbox",
                                                                                     onChange:this.onCheckboxChange,
                                                                                     checked:this.state.selected}));
        var attrNameTd = React.createElement("td", {}, React.createElement("input", {ref:"attrNameTextField",
                                                                                     className:"name-text-field",
                                                                                     valueLink:this.linkState("attrName"),
                                                                                     onBlur:this.onAttrNameTextBoxBlur,
                                                                                     onKeyDown:this.onAttrNameTextFieldKeyDown}));
        var attrValueInputTd = React.createElement("td", {},
            React.createElement("input", {ref:"attrValTextField",
                                          className:"val-text-field",
                                          valueLink:this.linkState("attrValue"),
                                          onBlur:this.onAttrTextBoxBlur,
                                          onKeyDown:this.onAttrValTextFieldKeyDown}));
        return React.createElement("tr", {}, checkBoxTd, attrNameTd, attrValueInputTd);
    },
    onAttrNameTextFieldKeyDown: function(e) {
        if (e.key === ResourceItem.ENTER_KEY /* && AttrValRow.isValidResourceName(this.state.attrName) */) {
            $(this.refs.attrNameTextField.getDOMNode()).blur();
        } else if (e.key === ResourceItem.ESCAPE_KEY) {
            // this.setState({resourceName:this.state.resourceNameCopy, isValidResourceName:true});
        }
    },
    onAttrValTextFieldKeyDown: function(e) {
        if (e.key === ResourceItem.ENTER_KEY /* && AttrValRow.isValidResourceName(this.state.attrValue) */) {
            $(this.refs.attrValTextField.getDOMNode()).blur();
        } else if (e.key === ResourceItem.ESCAPE_KEY) {
            // this.setState({resourceName:this.state.resourceNameCopy, isValidResourceName:true});
        }
    },
    onCheckboxChange: function() {
        this.setState({"selected":this.state.selected ? false : true});
    },
    onAttrNameTextBoxBlur: function() {
        this.props.updateCallback(this.props.attrName, this.getAttributeState());
    },
    onAttrTextBoxBlur: function() {
        this.props.updateCallback(this.props.attrName, this.getAttributeState());
    },
    componentWillReceiveProps: function(nextProps) {
        this.setState({attrName:nextProps.attrName, attrValue:nextProps.attrValue});
    },
    getAttributeState: function() {
        return {attrName:this.state.attrName, attrValue:this.state.attrValue};
    },
    isSelected: function() {
        return this.state.selected;
    },
    statics: {
        ENTER_KEY: "Enter",
        ESCAPE_KEY: "Escape"
    }
});